/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package travail_pratique_4;

/**
 *
 * @author Gus
 */
public class Trame {

    public final static byte TRAME_ENVOIE = ((char) 83);

    public final static byte TRAME_ACK = ((char) 65);

    public final static byte TRAME_NUM0 = ((char) 48);

    public final static byte TRAME_NUM1 = ((char) 49);

    public final byte[] message;

    public final byte type;

    public final byte numero;

    public final static char START_OF_TEXT = ((char) 02);

    public final static char END_OF_TEXT = ((char) 03);

    public Trame(byte[] trameByte) {
        byte[] tampon = new byte[trameByte.length - 2];
        int charDansTampon = 0;

        for (int i = 0; i < trameByte.length; i++) {

            if ((i != 0) && (i != (trameByte.length - 1))) {
                tampon[charDansTampon++] = trameByte[i];
            }
        }

        type = tampon[0];
        numero = tampon[1];

        message = new byte[tampon.length - 2];

        for (int j = 0; j < message.length; j++) {
            message[j] = tampon[j + 2];
        }
    }

    public byte[] toBytes() {
        byte[] trame = new byte[(4 + message.length)];

        //Header de trame
        trame[0] = START_OF_TEXT;
        
        //Type de trame
        trame[1] = type;
        
        //Numero de trame
        trame[2] = numero;
        
        //Ajout du message
        System.arraycopy(message, 0, trame, 3, message.length);
        
        //Tail de trame
        trame[trame.length - 1] = END_OF_TEXT;
        
        return trame;
    }

    public Trame(byte pTypeTrame, byte pNumero, byte[] pMessage) {
        this.message = pMessage;
        this.type = pTypeTrame;
        this.numero = pNumero;
    }

    public String toString() {
        return type + " " + numero;
    }

}
