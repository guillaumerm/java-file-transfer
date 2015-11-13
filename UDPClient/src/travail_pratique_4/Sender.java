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
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;

/**
 *
 * @author Guillaume Rochefort-Mathieu
 */
public class Sender extends Observable {

    private byte[] bytes;
    private DatagramSocket clientSocket;
    private InetAddress addressDestination;
    private byte numeroSeq = 0;
    private final static char END_OF_TRANSMISSION = ((char) 37);
    private final static char DATA_LINK_ESCAPE = ((char) 16);
    private boolean running = true;
    private int numeroTrameErreur = -1;
    private int nombreTrame = 0;
    private static final int TIMEOUT = 1000;

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
    }

    public void start(int numErreurTrame) {

        numeroTrameErreur = numErreurTrame;

        obtenirContenurPourTransfere();

        running = true;

        new Thread(new TaskSendData(), "Sender").start();
    }

    public void stop() {
        running = false;
        clientSocket.close();
    }

    private void obtenirContenurPourTransfere() {
        nombreTrame++;
        setChanged();
        notifyObservers();
    }

    private void incrementerSeq() {
        numeroSeq = (byte) (++numeroSeq % 2);
    }

    public void envoyerTrameSeq() throws IOException {
        byte numeroTrameTemp = (numeroSeq == 0) ? Trame.TRAME_NUM0 : Trame.TRAME_NUM1;

        Trame trame = new Trame(Trame.TRAME_ENVOIE, numeroTrameTemp, bytes);

        //Pour raison d'affichage je dois remonter la trame pour que le controlleur aille accès au numero
        setChanged();
        notifyObservers(trame);

        if (numeroTrameErreur != nombreTrame) {
            final byte[] sendTrame = trame.toBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendTrame, sendTrame.length, addressDestination, 9786);

            clientSocket.send(sendPacket);
        } else {
            nombreTrame++;
        }
    }

    /**
     *
     * @param data
     */
    public void OnReceiveData() throws IOException {
        clientSocket.setSoTimeout(TIMEOUT);
        while (running) {

            final byte[] receiveData = new byte[1024];

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            try {
                clientSocket.receive(receivePacket);

                Trame trameAccuse = new Trame(receivePacket.getData());

                //TODO voir si seq perdu 2 fois
                if (bytes[0] == END_OF_TRANSMISSION && bytes[1] == DATA_LINK_ESCAPE) {
                    running = false;
                } else {

                    byte numeroSeqTemp = (numeroSeq == 0) ? Trame.TRAME_NUM0 : Trame.TRAME_NUM1;

                    if (trameAccuse.numero != numeroSeqTemp) {

                        incrementerSeq();

                        obtenirContenurPourTransfere();

                    }

                    envoyerTrameSeq();
                }
            } catch (SocketTimeoutException ex) {
                envoyerTrameSeq();
            }
        }
    }

    public void setBuffer(byte[] bytes) {
        if (bytes != null) {
            this.bytes = bytes;
        }
    }

    /**
     * @author Guillaume Rochefort-Mathieu & Terry Turcotte
     */
    private class TaskSendData implements Runnable {

        @Override
        public void run() {
            try {
                envoyerTrameSeq();
                OnReceiveData();
            } catch (IOException ex) {
                running = false;
            }
        }
    }
}
