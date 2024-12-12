package edu.upc.epsevg.prop.hex.players;

import edu.upc.epsevg.prop.hex.HexGameStatus;
import edu.upc.epsevg.prop.hex.IAuto;
import edu.upc.epsevg.prop.hex.IPlayer;
import edu.upc.epsevg.prop.hex.PlayerMove;
import edu.upc.epsevg.prop.hex.SearchType;
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
        timeOut = true;
    }

    @Override
    public PlayerMove move(HexGameStatus s) {
        if (timeOut) {
            return null;  
        }

        int millorMoviment = -1;
        int alpha = menys_infinit;
        for (int i = 0; i < s.getSize(); i++) {
            for (int j = 0; j < s.getSize(); j++) {
                if (s.getPos(i, j) == 0) { 
                    if (millorMoviment == -1) millorMoviment = i * s.getSize() + j;  
                    HexGameStatus copiaTauler = new HexGameStatus(s);  
                    copiaTauler.placeStone(new Point(i, j)); 
                    if (copiaTauler.isGameOver() && copiaTauler.GetWinner() == s.getCurrentPlayer()) {  
                        return new PlayerMove(new Point(i, j), 0L, 0, SearchType.RANDOM);  
                    }
                    int valor = MinValor(copiaTauler, 3, menys_infinit, mes_infinit, s.getCurrentPlayerColor());  
                    if (alpha < valor) {
                        alpha = valor;
                        millorMoviment = i * s.getSize() + j;  
                    }
                }
            }
        }
        return new PlayerMove(new Point(millorMoviment / s.getSize(), millorMoviment % s.getSize()), 0L, 0, SearchType.RANDOM);
    }

    private int MinValor(HexGameStatus s, int profunditat, int alfa, int beta, int color) {
        if (profunditat == 0 || s.isGameOver()) {  
            return 0;  
        }
        int valor = mes_infinit;
        for (int i = 0; i < s.getSize(); i++) {
            for (int j = 0; j < s.getSize(); j++) {
                if (s.getPos(i, j) == 0) { 
                    HexGameStatus copiaTauler = new HexGameStatus(s); 
                    copiaTauler.placeStone(new Point(i, j)); 
                    if (copiaTauler.isGameOver() && copiaTauler.GetWinner() != s.getCurrentPlayer()) {  
                        return menys_infinit;
                    }
                    valor = Math.min(valor, MaxValor(copiaTauler, profunditat - 1, alfa, beta, color));
                    if (valor <= alfa) return valor;  
                    if (valor < beta) beta = valor;  
                }
            }
        }
        return valor;
    }

    private int MaxValor(HexGameStatus s, int profunditat, int alfa, int beta, int color) {
        if (profunditat == 0 || s.isGameOver()) {  
            return 0;  
        }
        int valor = menys_infinit;
        for (int i = 0; i < s.getSize(); i++) {
            for (int j = 0; j < s.getSize(); j++) {
                if (s.getPos(i, j) == 0) {  
                    HexGameStatus copiaTauler = new HexGameStatus(s);  
                    copiaTauler.placeStone(new Point(i, j));  
                    if (copiaTauler.isGameOver() && copiaTauler.GetWinner() != s.getCurrentPlayer()) {  
                        return mes_infinit;
                    }
                    valor = Math.max(valor, MinValor(copiaTauler, profunditat - 1, alfa, beta, color));
                    if (valor >= beta) return valor;  
                    if (valor > alfa) alfa = valor;  
                }
            }
        }
        return valor;
    }

    @Override
    public String getName() {
        return nom;
    }
}
