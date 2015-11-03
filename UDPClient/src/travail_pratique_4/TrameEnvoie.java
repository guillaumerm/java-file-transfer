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
public class TrameEnvoie extends Trame {

    private static final long serialVersionUID = 1L;
    
    public TrameEnvoie(String pTrame) {
        super(pTrame);
    }
    
    public TrameEnvoie(Type pTypeTrame, int pNumero, byte[] pMessage){
        super(pTypeTrame, pNumero, pMessage);
    }
}
