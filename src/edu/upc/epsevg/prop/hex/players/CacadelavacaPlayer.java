package edu.upc.epsevg.prop.hex.players;

import edu.upc.epsevg.prop.hex.*;
import java.awt.Point;

public class CacadelavacaPlayer implements IPlayer, IAuto {

    private final String nom;
    private boolean timeOut = false; 
    private final int mes_infinit = Integer.MAX_VALUE;
    private final int menys_infinit = Integer.MIN_VALUE;

    public CacadelavacaPlayer(String name) {
        nom = "Cacadelavaca";
    }

    @Override
    public void timeout() {
        timeOut = !timeOut;
    }

    @Override
    public PlayerMove move(HexGameStatus s) {
        int maxProfunditat = 8;
        int profunditat = 2;
        Point millorMoviment = null;  
        int alpha = menys_infinit;
        while (!timeOut && profunditat <= maxProfunditat) {
            for(MoveNode move : s.getMoves()) {
                if (millorMoviment == null) millorMoviment = move.getPoint();
                HexGameStatus copiaStatus = new HexGameStatus(s);  
                copiaStatus.placeStone(move.getPoint()); 
                if (copiaStatus.isGameOver() && copiaStatus.GetWinner() == s.getCurrentPlayer()) {  
                    return new PlayerMove(millorMoviment, 0, 0, SearchType.MINIMAX_IDS);  
                }
                int valor = MinValor(copiaStatus, profunditat, menys_infinit, mes_infinit, s.getCurrentPlayerColor());  

                if (alpha < valor) {
                    alpha = valor;
                    millorMoviment = move.getPoint(); 
                }
            }
            profunditat++;
        }
        timeout();
        return new PlayerMove(millorMoviment, 0, 0, SearchType.MINIMAX_IDS);
    }

    private int MinValor(HexGameStatus s, int profunditat, int alfa, int beta, int color) {
        if (timeOut) return 0; 
        if (profunditat == 0 || s.isGameOver()) return 0;

        int valor = mes_infinit;
        for(MoveNode move : s.getMoves()) {
            HexGameStatus copiaStatus = new HexGameStatus(s);  
            copiaStatus.placeStone(move.getPoint()); 
            if (copiaStatus.isGameOver() && copiaStatus.GetWinner() != s.getCurrentPlayer()) {  
                return menys_infinit; 
            }
            valor = Math.min(valor, MaxValor(copiaStatus, profunditat - 1, alfa, beta, color));
            if (valor <= alfa) return valor;  
            if (valor < beta) beta = valor;  
        }
        return valor;
    }

    private int MaxValor(HexGameStatus s, int profunditat, int alfa, int beta, int color) {
        if (timeOut) return 0; 
        if (profunditat == 0 || s.isGameOver()) return 0;

        int valor = menys_infinit;
        for(MoveNode move : s.getMoves()) {
            HexGameStatus copiaStatus = new HexGameStatus(s);  
            copiaStatus.placeStone(move.getPoint()); 
            if (copiaStatus.isGameOver() && copiaStatus.GetWinner() == s.getCurrentPlayer()) {  
                return mes_infinit; 
            }
            valor = Math.min(valor, MaxValor(copiaStatus, profunditat - 1, alfa, beta, color));
                if (valor >= beta) return valor;  
                if (valor > alfa) alfa = valor; 
        }
        return valor;
    }

    @Override
    public String getName() {
        return nom;
    }
}
                   