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
            //profunditat++;
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
        int puntuacioActual = dijkstra(s, jugadorActual,new Point(0, 0),new Point(4, 0));
        int puntuacioRival = dijkstra(s, jugadorRival,new Point(0, 0),new Point(4, 0));
        return (mes_infinit - puntuacioActual) - (mes_infinit - puntuacioRival);
        //return puntuacioActual - puntuacioRival;
    }
    
    
 private int dijkstra(HexGameStatus s, PlayerType player, Point start, Point end) {
        int n = s.getSize();
        Node[][] distancias = new Node[n][n];
        PriorityQueue<Node> cola = new PriorityQueue<>(Comparator.comparingInt(node -> node.distancia));
        Set<Node> visited = new HashSet<>();
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Point p = new Point(i, j);
                if (s.getPos(i, j) == colorActual) {
                    distancias[i][j] = (new Node(p, 0));
                } else {
                    distancias[i][j] = (new Node(p, mes_infinit));
                }
            }
        }
        
        distancias[start.x][start.y] = new Node(start, 0); 
        cola.add(distancias[start.x][start.y]);

        while (!cola.isEmpty()) {
            Node current = cola.poll();
            Point p = current.punt;

            if (visited.contains(current)) {
                continue;
            }

            visited.add(current);

            // Imprimir nodo actual y su distancia
            System.out.println("Procesando nodo: " + p + " con distancia: " + current.distancia);

            // Verificar si hemos llegado al nodo final
            if (p.equals(end)) {
                System.out.println("Nodo final alcanzado: " + end + " con distancia: " + current.distancia);
                return current.distancia;
            }

            // Explorar vecinos
            System.out.println("Vecinos de " + p + ":");
            for (Point neighbor : s.getNeigh(p)) {
                if(s.getPos(neighbor.x, neighbor.y) != colorRival) {
                        System.out.println("  Vecino: " + neighbor + " (distancia actual: " + distancias[neighbor.x][neighbor.y].distancia + ")");
                    if (!visited.contains(distancias[neighbor.x][neighbor.y])) {
                        int newDist = distancias[p.x][p.y].distancia + 1;
                        if (newDist < distancias[neighbor.x][neighbor.y].distancia) {
                            distancias[neighbor.x][neighbor.y].distancia = newDist;
                            cola.add(new Node(neighbor, newDist));
                        }
                    }
                }
            }

            // Imprimir cola después de agregar vecinos
            System.out.println("Cola actual:");
            for (Node node : cola) {
                System.out.println("  Nodo en cola: " + node.punt + " con distancia: " + node.distancia);
            }
        }

        return mes_infinit; // No se puede conectar
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