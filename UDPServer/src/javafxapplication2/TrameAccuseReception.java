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
public class TrameAccuseReception extends Trame {

    private static final long serialVersionUID = 1L;
    
    public TrameAccuseReception(String pTrame) {
        super(pTrame);
    }

    public TrameAccuseReception(Type pTypeTrame, int pNumero, byte[] pMessage){
        super(pTypeTrame, pNumero, pMessage);
    }
}
