/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javafxapplication2;

/**
 *
 * @author Gus
 */
public class TrameEnvoie extends Trame {

    
    public TrameEnvoie(String pTrame) {
        super(pTrame);
    }
    
    public TrameEnvoie(Type pTypeTrame, int pNumero, byte[] pMessage){
        super(pTypeTrame, pNumero, pMessage);
    }
}
