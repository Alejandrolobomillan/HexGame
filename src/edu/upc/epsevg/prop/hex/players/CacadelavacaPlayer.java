package edu.upc.epsevg.prop.hex.players;

import edu.upc.epsevg.prop.hex.*;

import java.awt.Point;
import java.util.Comparator;
import java.util.PriorityQueue;

public class CacadelavacaPlayer implements IPlayer, IAuto {

    private final String nom;
    private boolean timeOut = false; 
    private final int mes_infinit = Integer.MAX_VALUE;
    private final int menys_infinit = Integer.MIN_VALUE;
    private long heuristicasCalculadas = 0;

    public CacadelavacaPlayer(String name) {
        nom = "Cacadelavaca";
    }

    @Override
    public void timeout() {
        timeOut = !timeOut;
    }

    @Override
    public PlayerMove move(HexGameStatus s) {
        heuristicasCalculadas = 0;
        PlayerType player = s.getCurrentPlayer();
        int profunditat = 1;
        Point millorMoviment =  null;
        int alpha = menys_infinit;
        while (!timeOut) {
            for(MoveNode move : s.getMoves()) {
                if (millorMoviment == null) millorMoviment = move.getPoint();
                HexGameStatus copiaStatus = new HexGameStatus(s);  
                copiaStatus.placeStone(move.getPoint()); 
                if (copiaStatus.isGameOver()) {  
                    return new PlayerMove(move.getPoint(), heuristicasCalculadas, profunditat, SearchType.MINIMAX_IDS);  
                }
                int valor = MinValor(copiaStatus, profunditat, menys_infinit, mes_infinit);  

                if (alpha < valor) {
                    alpha = valor;
                    millorMoviment = move.getPoint(); 
                }
            }
            profunditat++;
        }
        if(timeOut) timeout();
        return new PlayerMove(millorMoviment, heuristicasCalculadas, profunditat, SearchType.MINIMAX_IDS);
    }

    private int MinValor(HexGameStatus s, int profunditat, int alfa, int beta) {
        if (profunditat == 0 || timeOut) return heuristica(s);

        int valor = mes_infinit;
        for(MoveNode move : s.getMoves()) {
            HexGameStatus copiaStatus = new HexGameStatus(s);  
            copiaStatus.placeStone(move.getPoint()); 
            if (copiaStatus.isGameOver()) {  
                return menys_infinit; 
            }
            valor = Math.min(valor, MaxValor(copiaStatus, profunditat - 1, alfa, beta));
            if (valor <= alfa) return valor;  
            beta = Math.min(valor, beta); 
        }
        return valor;
    }

    private int MaxValor(HexGameStatus s, int profunditat, int alfa, int beta) {
        if (profunditat == 0 || timeOut) return heuristica(s);

        int valor = menys_infinit;
        for(MoveNode move : s.getMoves()) {
            HexGameStatus copiaStatus = new HexGameStatus(s);  
            copiaStatus.placeStone(move.getPoint()); 
            if (copiaStatus.isGameOver()) {  
                return mes_infinit; 
            }
            valor = Math.max(valor, MinValor(copiaStatus, profunditat - 1, alfa, beta));
            if (valor >= beta) return valor;  
            alfa = Math.max(valor, alfa);
        }
        return valor;
    }
    
    private int heuristica(HexGameStatus s) {
        heuristicasCalculadas++;
        return 0;
    }
    
    @Override
    public String getName() {
        return nom;
    }
}
