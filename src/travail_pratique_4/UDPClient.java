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
   
    DatagramSocket clientSocket;
    InetAddress addressDestination;
    
    /**
     * 
     * @param ipAddress 
     */
    public UDPClient(byte[] ipAddress){
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
    public void sendData(byte[] data){
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, addressDestination, 9786);
        try {
            clientSocket.send(sendPacket);
        } catch (IOException ex) {
            Logger.getLogger(UDPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
