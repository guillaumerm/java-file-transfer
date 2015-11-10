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
            clientSocket = new DatagramSocket();
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

    public void start(File fichier) {
        this.fichier = fichier;

        new Thread(() -> {
            readFile();
        }).start();

        synchronized (flag) {
            try {
                flag.wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        timer.start();

        envoyerTrameSeq(bufferTransmission.get(numeroSeq));
    }

    private void incrementerSeq() {
        numeroSeq = (byte) (++numeroSeq % 2);
    }

    public void envoyerTrameSeq(byte[] data) {
        Trame trame = new Trame(Trame.TRAME_ENVOIE, numeroSeq, data);

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

            timer.cancel();

            if (trameAccuse.numero != numeroSeq) {

                bufferTransmission.set(numeroSeq, null);

                incrementerSeq();

                synchronized (flag) {
                    flag.notifyAll();
                }

            }

            if (bufferTransmission.get(0) == null && bufferTransmission.get(1) == null) {
                envoyerTrameSeq(new byte[]{END_OF_TRANSMISSION});
            } else {
                envoyerTrameSeq(bufferTransmission.get(numeroSeq));
            }

        }

    }

    private void readFile() {

        InputStream fis = null;

        try {
            fis = new FileInputStream(fichier);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }

        BufferedInputStream bis = new BufferedInputStream(fis);

        byte[] data = new byte[1024];
        int content;
        int bytesRead = 0;

        try {
            while ((content = fis.read()) != -1) {
                if (bufferTransmission.contains(null)) {
                    data[bytesRead] = (byte) content;
                    bytesRead++;

                    if (bytesRead == 1024) {
                        bufferTransmission.set(numeroSeq % 2, data.clone());

                        synchronized (flag) {
                            flag.notifyAll();
                        }

                        data = new byte[1024];
                        bytesRead = 0;
                    }
                } else {
                    synchronized (flag) {
                        try {
                            flag.wait();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof FxTimer) {
            ((FxTimer) o).cancel();
            envoyerTrameSeq(bufferTransmission.get(numeroSeq));
        }
    }
}
