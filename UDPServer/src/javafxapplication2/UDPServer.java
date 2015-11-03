/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javafxapplication2;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Guillaume
 */
public class UDPServer {
    
    DatagramSocket serverSocket;

    public UDPServer() {
        try {
            this.serverSocket = new DatagramSocket(9786);
        } catch (SocketException ex) {
            Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        new Thread(new TaskReceiveData()).start();
    }
    
    public void receiveData(){
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        
        try {
            serverSocket.receive(receivePacket);
        } catch (IOException ex) {
            Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        File file = new File("C:/Users/" + System.getProperty("user.name") + "/Downloads/text.txt");
        
        try {
            FileOutputStream out = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(out);
            try {
                out.write(receiveData);
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private class TaskReceiveData implements Runnable{

        @Override
        public void run() {
            receiveData();
        }
    
    }
    
    public static void main(String args[]){
        UDPServer udpServer = new UDPServer();
    }
}
