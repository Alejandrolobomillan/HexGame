package edu.upc.epsevg.prop.hex.players;

import edu.upc.epsevg.prop.hex.*;
import static edu.upc.epsevg.prop.hex.PlayerType.*;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

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
        int puntuacioActual = dijkstra2(s, jugadorActual,new Point(0, 0),new Point(4, 4));
        int puntuacioRival = dijkstra2(s, jugadorRival,new Point(0, 0),new Point(4, 1));
        return (mes_infinit - puntuacioActual) - (mes_infinit - puntuacioRival);
        //return puntuacioActual - puntuacioRival;
    }
    
    
 private int dijkstra(HexGameStatus s, PlayerType player) {
        int n = s.getSize();
        int[][] distancias = new int[n][n];
        PriorityQueue<Node> cola = new PriorityQueue<>(Comparator.comparingInt(node -> node.distancia));
        boolean[][] visitado = new boolean[n][n];

        // Inicializar distancias y cola
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (s.getPos(i, j) == colorRival) {
                    distancias[i][j] = mes_infinit; // Fichas del rival
                } else if (s.getPos(i, j) == colorActual) {
                    distancias[i][j] = 0; // Fichas propias
                    if ((player == PLAYER1 && i == 0) || (player == PLAYER2 && j == 0)) {
                        cola.add(new Node(new Point(i, j), 0)); // Bordes iniciales
                    }
                } else {
                    distancias[i][j] = 1; // Celdas vacías
                }
            }
        }

        // Dijkstra
        while (!cola.isEmpty()) {
            Node actual = cola.poll();
            System.out.println(actual.punt.x + actual.punt.y + actual.distancia);
            Point p = actual.punt;
            if (visitado[p.x][p.y]) continue;
            visitado[p.x][p.y] = true;

            // Verificar si alcanzamos el borde opuesto
            if ((player == PLAYER1 && p.x == n - 1) || (player == PLAYER2 && p.y == n - 1)) {
                return actual.distancia;
            }

            // Explorar vecinos
            for (Point vecino : s.getNeigh(p)) {
                if (!visitado[vecino.x][vecino.y]) {
                    int nuevoCosto = actual.distancia + distancias[vecino.x][vecino.y];
                    if (nuevoCosto < distancias[vecino.x][vecino.y]) {
                        distancias[vecino.x][vecino.y] = nuevoCosto;
                        cola.add(new Node(vecino, nuevoCosto));
                    }
                }
            }
        }

        return mes_infinit; // No se puede conectar
    }

    
 
 private int dijkstra2(HexGameStatus s, PlayerType player, Point start, Point end){
    Map<Point, Integer> distances;
    Set<Point> visited;
    PriorityQueue<Node> queue;
    int n = s.getSize();
    
    distances = new HashMap<>();
    visited = new HashSet<>();
    queue = new PriorityQueue<>((a, b) -> a.distancia - b.distancia);

        // Inicializar distancias y cola
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Point p = new Point(i, j);
                if (s.getPos(i, j) == colorRival) {
                    distances.put(p, mes_infinit); // ficha rival
                } else if (s.getPos(i, j) == colorActual) {
                    distances.put(p, 0); // mis fichas
                } else {
                    distances.put(p, 1);
                }
            }
        }
        // Set start distance
        distances.put(start, 0);
        queue.add(new Node(start, 0));

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            Point p = current.punt;

            if (visited.contains(p)) {
                continue;
            }

            visited.add(p);

            if (p.equals(end)) {
                return distances.get(end);
            }

            // Explore neighbors -- 
            for (Point neighbor : s.getNeigh(p)) {
                if (!visited.contains(neighbor)){
                    int newDist = distances.get(p) + distances.get(neighbor);
                    if (newDist < distances.get(neighbor)) {
                        distances.put(neighbor, newDist);
                        queue.add(new Node(neighbor, newDist));
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
