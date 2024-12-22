package edu.upc.epsevg.prop.hex.players;

import edu.upc.epsevg.prop.hex.*;
import static edu.upc.epsevg.prop.hex.PlayerType.*;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class CacadelavacaPlayer implements IPlayer, IAuto {

    private final String nom;
    private boolean timeOut = false; 
    private PlayerType jugadorActual = null;
    private int colorActual = -2;
    private PlayerType jugadorRival = null;
    private int colorRival = -2;
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
        jugadorActual = s.getCurrentPlayer();
        colorActual = getColor(jugadorActual);
        jugadorRival = opposite(jugadorActual);
        colorRival = getColor(jugadorRival);
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
        int puntuacioActual = dijkstra(s, jugadorActual);
        int puntuacioRival = dijkstra(s, jugadorRival);
        return (mes_infinit - puntuacioActual) - (mes_infinit - puntuacioRival);
    }
    
    private int dijkstra(HexGameStatus s, PlayerType player) {
        int n = s.getSize();
        int[][] distancies = new int[n][n];
        PriorityQueue<Node> cola = new PriorityQueue<>(Comparator.comparingInt(node -> node.distancia));
        if (player == PLAYER1) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if(s.getPos(1,j) == colorRival) {
                        distancies[i][j] = menys_infinit;
                    } else if (i == 1 && j == 1 || j == 2 || j == 3 || j == 4 || j == 5){
                        distancies[i][j] = 0;
                        cola.add(new Node(new Point(i, j), 0));
                    } else {
                        distancies[i][j] = mes_infinit;
                    }
                }
            }
            while (!cola.isEmpty()) {
                Node actual = cola.poll();
                Point puntActual = actual.punt;
                if(puntActual.x == n - 1){
                    return actual.distancia;
                }
                
                ArrayList<Point> veins = s.getNeigh(puntActual);

                for (Point vei : veins) {
                    int nouCost = actual.distancia + 1; 

                    if (nouCost < distancies[vei.x][vei.y]) {
                        distancies[vei.x][vei.y] = nouCost;
                        cola.add(new Node(new Point(vei.x, vei.y), nouCost));
                    }
                }
            }        
        } else {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if(s.getPos(i,j) == colorRival) {
                        distancies[i][j] = mes_infinit;
                    } else if (j == 1 && i == 1 || i == 2 || i == 3 || i == 4 || i == 5){
                        distancies[n][n] = 0;
                        cola.add(new Node(new Point(i, j), 0));
                    } else {
                        distancies[n][n] = 1;
                    }
                }
            }
            while (!cola.isEmpty()) {
                Node actual = cola.poll();
                Point puntActual = actual.punt;
                if(puntActual.y == n - 1){
                    return actual.distancia;
                }
                
                ArrayList<Point> veins = s.getNeigh(puntActual);

                for (Point vei : veins) {
                    int nouCost = actual.distancia + 1; 

                    if (nouCost < distancies[vei.x][vei.y]) {
                        distancies[vei.x][vei.y] = nouCost;
                        cola.add(new Node(new Point(vei.x, vei.y), nouCost));
                    }
                }
            }        
        }
        return mes_infinit;
    }
    
    private static class Node {
        Point punt;
        int distancia;

        public Node(Point punt, int distancia) {
            this.punt = punt;
            this.distancia = distancia;
        }
    }
    
    @Override
    public String getName() {
        return nom;
    }
}
