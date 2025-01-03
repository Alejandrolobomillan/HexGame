/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.upc.epsevg.prop.hex;

import java.awt.Point;
import edu.upc.epsevg.prop.hex.HexGameStatus;
import edu.upc.epsevg.prop.hex.PlayerType;
import edu.upc.epsevg.prop.hex.players.CacadelavacaPlayer;
import edu.upc.epsevg.prop.hex.players.ProfeGameStatus2;
import edu.upc.epsevg.prop.hex.players.ProfeGameStatus3;
import edu.upc.epsevg.prop.hex.players.ProfeGameStatus3.Result;
/**
 *
 * @author bernat
 */
public class UnitTesting {
    
    
    
    public static void main(String[] args) {
    
        
        byte[][] board = {
        //X    0  1  2  3  4  5  6  7  8  9 10
            { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1},                   // 0   Y
              { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},                 // 1
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0},               // 2
                  { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},             // 3
                    { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},           // 4
                      { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},         // 5
                        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},       // 6
                          { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},     // 7
                            { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},   // 8
                              { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // 9
                                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}  // 10   Y         
        };



        HexGameStatus gs = new HexGameStatus(board, PlayerType.PLAYER1);        
        int resultat = CacadelavacaPlayer.dijkstra(gs, PlayerType.PLAYER2);
        System.out.println("*" + resultat);
    }
    
}
