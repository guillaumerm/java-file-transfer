/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package travail_pratique_4;

import java.io.Serializable;

/**
 *
 * @author Gus
 */
public abstract class Trame implements Serializable {
    
    public byte[] message;
    
    public final Type type;
    
    public final int numero;
    
    private final static char START_OF_TEXT = ((char)02);
    
    private final static char START_OF_HEADING = ((char)01);
    
    private final static char END_OF_TRANSMISSION = ((char)37);
    
    private final static char END_OF_TEXT = ((char)03);
    
    
    public enum Type{
        SEQ, ACK
    }
    
    public Trame(String pTrame){
        char[] tampon = new char[pTrame.length()];
        int charDansTampon = 0;
        
        for(int i = 0; i < (pTrame.length() - 1);i++){
            char aChar = pTrame.charAt(i);
            
            if(aChar != START_OF_HEADING || aChar != START_OF_TEXT || aChar != END_OF_TEXT || aChar != END_OF_TRANSMISSION ){
                tampon[charDansTampon++] = aChar;
            }
        }
        
        type = Type.valueOf(Character.toString(tampon[0]));
        numero = (int) tampon[1];
        
        String messageTemp = "";
        
        for(int i = 0; i < tampon.length - 1; i++){
            messageTemp += Character.toString(tampon[i]);
        }
        
        message = messageTemp.getBytes();
    }
    
    
    public Trame(Type pTypeTrame, int pNumero, byte[] pMessage){
        this.message = pMessage;
        this.type = pTypeTrame;
        this.numero = pNumero;
    }
    
    public String toString(){
        return Character.toString(START_OF_HEADING) + type + numero + Character.toString(START_OF_TEXT) + message.toString() + Character.toString(END_OF_TEXT) + Character.toString(END_OF_TRANSMISSION); 
    }
    
}
