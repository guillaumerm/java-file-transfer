
package travail_pratique_4;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe qui s'occupe de la reception de fichier envoyé par le "Sender".
 * 
 * @author Guillaume Rochefort-Mathieu & Terry Turcotte
 */
public class Receiver extends Observable {

    private final int TAILLE_BUFFER_MAX = 1400;
    private DatagramSocket serverSocket;
    private boolean running = true;
    private byte numeroAck = 0;
    private InetAddress addressDestination;
    private int portDestination;

    public Receiver() {
        try {
            this.serverSocket = new DatagramSocket(9786);
        } catch (SocketException ex) {
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void start() {
        new Thread(new TaskReceiveData(), "Receiver").start();
    }

    public void stop() {
        running = false;
        this.serverSocket.close();
    }

    private void incrementerAck() {
        numeroAck = (byte) (++numeroAck % 2);
    }

    private Trame receptionTrameSeq() throws IOException {
        byte[] receiveData = new byte[TAILLE_BUFFER_MAX];

        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        serverSocket.receive(receivePacket);

        addressDestination = receivePacket.getAddress();
        portDestination = receivePacket.getPort();

        int index = receivePacket.getData().length - 1;
        byte aByte = receivePacket.getData()[index];
        int nombreByteNull = 0;

        while (aByte != Trame.END_OF_TEXT) {
            nombreByteNull++;
            aByte = receivePacket.getData()[--index];
        }

        byte[] trameBytes = new byte[receivePacket.getData().length - nombreByteNull];

        System.arraycopy(receivePacket.getData(), 0, trameBytes, 0, trameBytes.length);

        return new Trame(trameBytes);
    }

    private void envoyerTrameAck() {

        String accusee = "ACK " + numeroAck;

        byte numeroAckTemp = (numeroAck == 0) ? (byte) Trame.TRAME_NUM0 : (byte) Trame.TRAME_NUM1;

        Trame trameAccuse = new Trame(Trame.TRAME_ACK, numeroAckTemp, accusee.getBytes());

        //Remonte la trame pour ajouter ca trace dans l'historique
        setChanged();
        notifyObservers(trameAccuse);

        DatagramPacket sendPacket = new DatagramPacket(trameAccuse.toBytes(), trameAccuse.toBytes().length, addressDestination, portDestination);

        try {
            serverSocket.send(sendPacket);
        } catch (IOException ex) {
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     *
     * @throws java.net.SocketException
     */
    public void OnReceiveData() throws IOException {
        while (running) {

            Trame trameEnvoie = receptionTrameSeq();

            byte numeroAckTemp = (numeroAck == 0) ? (byte) Trame.TRAME_NUM0 : (byte) Trame.TRAME_NUM1;

            if (trameEnvoie.numero == numeroAckTemp) {

                incrementerAck();

                //Remontre trame
                setChanged();
                notifyObservers(trameEnvoie);

            }

            envoyerTrameAck();
        }
    }

    /**
     *
     */
    private class TaskReceiveData implements Runnable {

        @Override
        public void run() {
            try {
                OnReceiveData();
            } catch (IOException ex) {
                serverSocket = null;
            }
        }
    }
}
