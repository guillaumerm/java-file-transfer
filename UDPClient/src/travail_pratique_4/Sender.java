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
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Guillaume Rochefort-Mathieu
 */
public class Sender extends Observable implements Observer {

    private byte[] bytes;
    private DatagramSocket clientSocket;
    private InetAddress addressDestination;
    private byte numeroSeq = 0;
    private final static char END_OF_TRANSMISSION = ((char) 37);
    private FxTimer timer;
    private boolean running = true;
    public final static Object flag = new Object();

    /**
     *
     * @param ipAddress
     */
    public Sender(byte[] ipAddress) {
        try {
            clientSocket = new DatagramSocket();
        } catch (SocketException ex) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            addressDestination = InetAddress.getByAddress(ipAddress);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
        }

        timer = new FxTimer(1000);
    }

    public void start() {

        obtenirMessage();

        timer.start();

        new Thread(() -> {
            envoyerTrameSeq();
            OnReceiveData();
        }, "Sender Client").start();

    }

    private void obtenirMessage() {
        setChanged();
        notifyObservers();
    }

    private void incrementerSeq() {
        numeroSeq = (byte) (++numeroSeq % 2);
    }

    public void envoyerTrameSeq() {
        byte numeroTrameTemp = (numeroSeq == 0) ? Trame.TRAME_NUM0 : Trame.TRAME_NUM1;

        Trame trame = new Trame(Trame.TRAME_ENVOIE, numeroTrameTemp, bytes);

        final byte[] sendTrame = trame.toBytes();

        DatagramPacket sendPacket = new DatagramPacket(sendTrame, sendTrame.length, addressDestination, 9786);

        try {
            clientSocket.send(sendPacket);
        } catch (IOException ex) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Trame receptionTrameAck() {
        final byte[] receiveData = new byte[1024];

        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        try {
            clientSocket.receive(receivePacket);
        } catch (IOException ex) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new Trame(receivePacket.getData());
    }

    /**
     *
     * @param data
     */
    public void OnReceiveData() {
        while (running) {

            Trame trameAccuse = receptionTrameAck();

            //TODO voir si seq perdu 2 fois
            if (bytes[0] == END_OF_TRANSMISSION) {
                running = false;
            }

            timer.cancel();

            byte numeroSeqTemp = (numeroSeq == 0) ? Trame.TRAME_NUM0 : Trame.TRAME_NUM1;

            if (trameAccuse.numero != numeroSeqTemp) {

                incrementerSeq();

                obtenirMessage();

            }

            envoyerTrameSeq();

        }

    }

    public void setBuffer(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof FxTimer) {
            ((FxTimer) o).cancel();
            envoyerTrameSeq();
        }
    }
}
