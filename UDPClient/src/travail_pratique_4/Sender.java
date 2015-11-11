/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package travail_pratique_4;

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
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Guillaume Rochefort-Mathieu
 */
public class Sender implements Observer {

    public byte[] bytes;
    private InputStream fis = null;
    private DatagramSocket clientSocket;
    private InetAddress addressDestination;
    private byte numeroSeq = 0;
    private final static char END_OF_TRANSMISSION = ((char) 37);
    private final static char DATA_LINK_ESCAPE = ((char) 16);
    private FxTimer timer;
    private boolean running = true;
    private boolean isClosed = true;
    private File fichier;

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

        bytes = new byte[1024];

        timer = new FxTimer(1000);
    }

    public void start(File fichier) {
        this.fichier = fichier;

        timer.start();
        bytes = readFile();

        new Thread(() -> {
            envoyerTrameSeq();
            OnReceiveData();
        }, "Sender Client").start();

    }

    private void incrementerSeq() {
        numeroSeq = (byte) (++numeroSeq % 2);
    }

    public void envoyerTrameSeq() {
        byte numeroTrameTemp = (numeroSeq == 0) ? Trame.TRAME_NUM0 : Trame.TRAME_NUM1;

        Trame trame = new Trame(Trame.TRAME_ENVOIE, numeroTrameTemp, bytes);

        String trameMessageTemp = new String(trame.message);
        System.out.println("Envoie: (" + trame.toString()+") " + trameMessageTemp);

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

                bytes = readFile();

                incrementerSeq();

            }

            envoyerTrameSeq();

        }

    }

    private byte[] readFile() {

        byte[] data = new byte[1024];
        int content = -1;
        int bytesRead = 0;

        if (fis == null) {
            try {
                fis = new FileInputStream(fichier);
                //bis = new BufferedInputStream(fis);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            isClosed = false;
        }

        try {
            if (!isClosed) {
                while (((content = fis.read()) != -1) && bytesRead < 1024) {
                    data[bytesRead] = (byte) content;
                    bytesRead++;

                }
            }

            if (isClosed || (content == -1 && bytesRead == 0)) {
                if (!isClosed) {
                    fis.close();
                    isClosed = true;
                }

                data[0] = END_OF_TRANSMISSION;
                data[1] = DATA_LINK_ESCAPE;
            }

        } catch (IOException ex) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof FxTimer) {
            ((FxTimer) o).cancel();
            envoyerTrameSeq();
        }
    }
}
