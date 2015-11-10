/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package travail_pratique_4;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
 * @author Guillaume
 */
public class NetworkModule extends Observable implements Observer {

    private DatagramSocket serverSocket;
    private boolean running = true;
    private byte numeroAck = 0;
    private InetAddress addressDestination;
    private DatagramSocket clientSocket;
    private byte numeroSeq = 0;
    private final static char END_OF_TRANSMISSION = ((char) 37);
    private FxTimer timer;
    private List<byte[]> bufferTransmission;
    private File fichier;
    private final Object flag = new Object();

    public NetworkModule(int port) {

        try {
            //TODO merge clientSocket receiving ACK and SEQ
            clientSocket = new DatagramSocket();
        } catch (SocketException ex) {
            Logger.getLogger(NetworkModule.class.getName()).log(Level.SEVERE, null, ex);
        }

        bufferTransmission = new LinkedList();

        for (int i = 0; i < 2; i++) {
            bufferTransmission.add(null);
        }

        timer = new FxTimer(1000);

        try {
            this.serverSocket = new DatagramSocket(port);
        } catch (SocketException ex) {
            Logger.getLogger(NetworkModule.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setAddress(byte[] ipAddress) {
        try {
            addressDestination = InetAddress.getByAddress(ipAddress);
        } catch (UnknownHostException ex) {
            Logger.getLogger(NetworkModule.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void startServer() {
        new Thread(new TaskReceiveData()).start();
    }

    private void incrementerAck() {
        numeroAck = (byte) (++numeroAck % 2);
    }

    private Trame receptionTrame() {
        byte[] receiveData = new byte[1024];

        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        try {
            serverSocket.receive(receivePacket);
        } catch (IOException ex) {
            Logger.getLogger(NetworkModule.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(NetworkModule.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     *
     */
    public void OnReceiveData() {
        while (running) {

            Trame trame = receptionTrame();

            if (trame.type == Trame.TRAME_ACK) {
                timer.cancel();

                if (trame.numero != numeroSeq) {

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
            } else if (trame.type == Trame.TRAME_ENVOIE) {
                if (trame.numero == numeroAck) {

                    incrementerAck();

                    //Remontre trame
                    setChanged();
                    notifyObservers(trame.message);

                }

                envoyerTrameAck();
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(NetworkModule.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void startClient(File fichier) {
        this.fichier = fichier;

        new Thread(() -> {
            readFile();
        }).start();

        synchronized (flag) {
            try {
                flag.wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(NetworkModule.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        timer.start();

        envoyerTrameSeq(bufferTransmission.get(numeroSeq));
    }

    public void envoyerTrameSeq(byte[] data) {
        Trame trame = new Trame(Trame.TRAME_ENVOIE, numeroSeq, data);

        final byte[] sendTrame = trame.toBytes();

        DatagramPacket sendPacket = new DatagramPacket(sendTrame, sendTrame.length, addressDestination, 9786);

        try {
            clientSocket.send(sendPacket);
        } catch (IOException ex) {
            Logger.getLogger(NetworkModule.class.getName()).log(Level.SEVERE, null, ex);
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
                data[bytesRead] = (byte) content;
                bytesRead++;

                if (bytesRead == 1024) {
                    if (bufferTransmission.contains(null)) {

                        bufferTransmission.set(numeroSeq % 2, data.clone());

                        synchronized (flag) {
                            flag.notifyAll();
                        }
                    } else {
                        synchronized (flag) {
                            try {
                                flag.wait();
                            } catch (InterruptedException ex) {
                                Logger.getLogger(NetworkModule.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }

                    data = new byte[1024];
                    bytesRead = 0;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void incrementerSeq() {
        numeroSeq = (byte) (++numeroSeq % 2);
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

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof FxTimer) {
            ((FxTimer) o).cancel();
            envoyerTrameSeq(bufferTransmission.get(numeroSeq));
        }
    }
}
