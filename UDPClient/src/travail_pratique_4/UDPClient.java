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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Guillaume Rochefort-Mathieu
 */
public class UDPClient {

    private DatagramSocket clientSocket;
    private InetAddress addressDestination;
    private byte numeroSeq = 0;
    private final static char END_OF_TRANSMISSION = ((char) 37);

    /**
     *
     * @param ipAddress
     */
    public UDPClient(byte[] ipAddress) {
        try {
            clientSocket = new DatagramSocket();
        } catch (SocketException ex) {
            Logger.getLogger(UDPClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            addressDestination = InetAddress.getByAddress(ipAddress);
        } catch (UnknownHostException ex) {
            Logger.getLogger(UDPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param data
     */
    public void sendData(byte[] data) {
        Trame trame = new Trame(Trame.TRAME_ENVOIE, numeroSeq, data);

        final byte[] sendTrame = trame.toBytes();

        DatagramPacket sendPacket = new DatagramPacket(sendTrame, sendTrame.length, addressDestination, 9786);

        try {
            clientSocket.send(sendPacket);
        } catch (IOException ex) {
            Logger.getLogger(UDPClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        final byte[] receiveData = new byte[1024];

        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        try {
            clientSocket.receive(sendPacket);
        } catch (IOException ex) {
            Logger.getLogger(UDPClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        numeroSeq = (byte) (++numeroSeq % 2);
        
        Trame trameAccuse = new Trame(sendPacket.getData());

        if (trameAccuse.numero == numeroSeq) {
            Trame trameEND = new Trame(Trame.TRAME_ENVOIE, numeroSeq, new byte[]{END_OF_TRANSMISSION});

            final byte[] sendTrame2 = trameEND.toBytes();

            DatagramPacket sendPacket2 = new DatagramPacket(sendTrame2, sendTrame2.length, addressDestination, 9786);
            
            try {
                clientSocket.send(sendPacket2);
            } catch (IOException ex) {
                Logger.getLogger(UDPClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}
