/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package travail_pratique_4;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
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

    private void incrementerAck() {
        numeroAck = (byte) (++numeroAck % 2);
    }

    private Trame receptionTrameSeq() {
        byte[] receiveData = new byte[1024];

        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        try {
            serverSocket.receive(receivePacket);
        } catch (IOException ex) {
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
        }

        addressDestination = receivePacket.getAddress();

        return new Trame(receivePacket.getData());
    }

    private void envoyerTrameAck() {

        String accusee = "ACK " + numeroAck;

        Trame trameAccuse = new Trame(Trame.TRAME_ACK, numeroAck, accusee.getBytes());

        DatagramPacket sendPacket = new DatagramPacket(trameAccuse.toBytes(), trameAccuse.toBytes().length, addressDestination, 9786);
        
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

            if (trameEnvoie.numero == numeroAck) {

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
