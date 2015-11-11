/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package travail_pratique_4;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Guillaume
 */
public class Receiver extends Observable {

    private DatagramSocket serverSocket;
    private boolean running = true;
    private byte numeroAck = 0;
    private InetAddress addressDestination;
    private int portDestination;

    public Receiver() {
        try {
            this.serverSocket = new DatagramSocket(9786);
        } catch (SocketException ex) {
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void start() {
        new Thread(new TaskReceiveData()).start();
    }

    public void stop() {
        this.serverSocket.close();
    }

    private void incrementerAck() {
        numeroAck = (byte) (++numeroAck % 2);
    }

    private Trame receptionTrameSeq() {
        byte[] receiveData = new byte[1028];

        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        try {
            serverSocket.receive(receivePacket);
        } catch (SocketException sEx) {
            receivePacket = null;
        } catch (IOException ex) {
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
        }

        addressDestination = receivePacket.getAddress();
        portDestination = receivePacket.getPort();

        int index = receivePacket.getData().length - 1;
        byte aByte = receivePacket.getData()[index];
        int nombreByteNull = 0;

        while (aByte != Trame.END_OF_TEXT) {
            nombreByteNull++;
            aByte = receivePacket.getData()[--index];
        }
        
        byte[] trameBytes = new byte[receivePacket.getData().length - nombreByteNull];
        
        System.arraycopy(receivePacket.getData(), 0, trameBytes, 0, trameBytes.length);

        return new Trame(trameBytes);
    }

    private void envoyerTrameAck() {

        String accusee = "ACK " + numeroAck;

        byte numeroAckTemp = (numeroAck == 0) ? (byte) Trame.TRAME_NUM0 : (byte) Trame.TRAME_NUM1;

        Trame trameAccuse = new Trame(Trame.TRAME_ACK, numeroAckTemp, accusee.getBytes());

        DatagramPacket sendPacket = new DatagramPacket(trameAccuse.toBytes(), trameAccuse.toBytes().length, addressDestination, portDestination);

        try {
            serverSocket.send(sendPacket);
        } catch (IOException ex) {
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     *
     */
    public void OnReceiveData() {
        while (running) {

            Trame trameEnvoie = receptionTrameSeq();

            byte numeroAckTemp = (numeroAck == 0) ? (byte) Trame.TRAME_NUM0 : (byte) Trame.TRAME_NUM1;

            if (trameEnvoie.numero == numeroAckTemp) {

                incrementerAck();

                //Remontre trame
                setChanged();
                notifyObservers(trameEnvoie.message);

            }

            envoyerTrameAck();

            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     *
     */
    private class TaskReceiveData implements Runnable {

        @Override
        public void run() {
            OnReceiveData();
        }
    }
}
