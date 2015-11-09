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

    public byte[] message;

    public final byte type;

    public final byte numero;

    private final static char START_OF_TEXT = ((char) 02);

    private final static char END_OF_TEXT = ((char) 03);

    public Trame(byte[] trameByte) {
        byte[] tampon = new byte[trameByte.length - 2];
        int charDansTampon = 0;

        for (int i = 0; i < (trameByte.length - 1); i++) {

            if (trameByte[i] != ((byte) START_OF_TEXT) && trameByte[i] != ((byte) END_OF_TEXT)) {
                tampon[charDansTampon++] = trameByte[i];
            }
        }

        type = tampon[0];
        numero = tampon[1];

        message = new byte[tampon.length - 2];

        for (int j = 0; j < message.length - 1; j++) {
            message[j] = tampon[2 + j];
        }
    }

    public byte[] toBytes() {
        byte[] trame = new byte[ 4 + message.length];
        for (int i = 0; i < trame.length - 1; i++) {
            if (i == 0) {
                trame[i] = (byte) START_OF_TEXT;
            } else if (i == 1) {
                trame[i] = type;
            } else if (i == 2) {
                trame[i] = numero;
            } else if (i == 3) {
                for (int k = 0; k < message.length; k++) {
                    trame[i + k] = message[k];
                }
            } else if (i == trame.length - 1) {
                trame[i] = (byte) END_OF_TEXT;
            }
        }

        return trame;
    }

    public Trame(byte pTypeTrame, byte pNumero, byte[] pMessage) {
        this.message = pMessage;
        this.type = pTypeTrame;
        this.numero = pNumero;
    }

    public String toString() {
        return Character.toString(START_OF_TEXT) + type + numero + message.toString() + Character.toString(END_OF_TEXT);
    }

}
