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
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Guillaume
 */
public class UDPServer {

    private DatagramSocket serverSocket;
    private boolean running = true;
    private byte numeroAck = 0;
    File file = new File("C:/Users/" + System.getProperty("user.name") + "/Downloads/text.txt");
    private final static char END_OF_TRANSMISSION = ((char) 37);
    private FileOutputStream out;
    private BufferedOutputStream bos;

    public UDPServer() {
        try {
            this.serverSocket = new DatagramSocket(9786);
        } catch (SocketException ex) {
            Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            out = new FileOutputStream(file);
            bos = new BufferedOutputStream(out);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void start() {
        new Thread(new TaskReceiveData()).start();
    }

    public void writeToFile(byte[] message) {
        try {
            if (message != null) {
                if (message[0] == END_OF_TRANSMISSION) {
                    out.flush();
                    out.close();
                } else {
                    out.write(message);
                }
            } else {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void receiveData() {
        while (running) {
            byte[] receiveData = new byte[20];

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            try {
                serverSocket.receive(receivePacket);
            } catch (IOException ex) {
                Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
            }

            Trame trameEnvoie = new Trame(receivePacket.getData());

            if (trameEnvoie.numero == numeroAck) {
                writeToFile(trameEnvoie.message);

                String accusee = "ACK pour SEQ " + trameEnvoie.numero;

                numeroAck = (byte) (++numeroAck % 2);

                Trame trameAccuse = new Trame(Trame.TRAME_ACK, numeroAck, accusee.getBytes());

                DatagramPacket sendPacket = new DatagramPacket(trameAccuse.toBytes(), trameAccuse.toBytes().length, receivePacket.getAddress(), receivePacket.getPort());

                try {
                    serverSocket.send(sendPacket);
                } catch (IOException ex) {
                    Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
    }

    private class TaskReceiveData implements Runnable {

        @Override
        public void run() {
            receiveData();
        }
    }
}
