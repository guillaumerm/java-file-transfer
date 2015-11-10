/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package travail_pratique_4;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Guillaume Rochefort-Mathieu
 */
public class Sender implements Observer {

    private DatagramSocket clientSocket;
    private InetAddress addressDestination;
    private byte numeroSeq = 0;
    private final static char END_OF_TRANSMISSION = ((char) 37);
    private FxTimer timer;
    private boolean running = true;
    private List<byte[]> bufferTransmission;
    private File fichier;
    private final Object flag = new Object();

    /**
     *
     * @param ipAddress
     */
    public Sender(byte[] ipAddress) {
        try {
            //TODO merge clientSocket receiving ACK and SEQ
            clientSocket = new DatagramSocket(9786);
        } catch (SocketException ex) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            addressDestination = InetAddress.getByAddress(ipAddress);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
        }

        bufferTransmission = new LinkedList();

        for (int i = 0; i < 2; i++) {
            bufferTransmission.add(null);
        }

        timer = new FxTimer(1000);
    }





    

    

    /**
     *
     * @param data
     */
    public void OnReceiveData() {
        while (running) {

            Trame trameAccuse = receptionTrameAck();

            

        }

    }

    

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof FxTimer) {
            ((FxTimer) o).cancel();
            envoyerTrameSeq(bufferTransmission.get(numeroSeq));
        }
    }
    
    private class TaskReceiveData implements Runnable{

        @Override
        public void run() {
            OnReceiveData();
        }
    }
}
