package edu.upc.epsevg.prop.hex.players;

import edu.upc.epsevg.prop.hex.*;
import static edu.upc.epsevg.prop.hex.PlayerType.*;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

public class CacadelavacaPlayer implements IPlayer, IAuto {

    private final String nom;
    private boolean timeOut = false; 
    private PlayerType jugadorActual = null;
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
        int puntuacioRival = dijkstra(s, opposite(jugadorActual));
        System.out.println(puntuacioActual);
        return (mes_infinit - puntuacioActual) - (mes_infinit - puntuacioRival);
    }
    
    
    public static int dijkstra(HexGameStatus s, PlayerType player) {
        int n = s.getSize();
        Node[][] distancias = new Node[n][n];
        PriorityQueue<Node> cola = new PriorityQueue<>(Comparator.comparingInt(node -> node.distancia));
        Set<Node> visited = new HashSet<>();
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Point p = new Point(i, j);
                distancias[i][j] = (new Node(p, Integer.MAX_VALUE));      
                int ii = i+1;
                int jj = j-2;
                Point pont = new Point(ii, jj);
                List<Point> nouPonts = new ArrayList<>();
                if((ii >= 0 && ii < n) && (jj >= 0 && jj < n)) {
                    nouPonts.add(pont);
                }
                ii = i+2;
                jj = j-1;
                if((ii >= 0 && ii < n) && (jj >= 0 && jj < n)) {
                    nouPonts.add(pont);
                }
                ii = i+1;
                jj = j+1;
                if((ii >= 0 && ii < n) && (jj >= 0 && jj < n)) {
                    nouPonts.add(pont);
                }
                ii = i-1;
                jj = j-2;
                if((ii >= 0 && ii < n) && (jj >= 0 && jj < n)) {
                    nouPonts.add(pont);
                }
                ii = i-2;
                jj = j+1;
                if((ii >= 0 && ii < n) && (jj >= 0 && jj < n)) {
                    nouPonts.add(pont);
                }
                ii = i-1;
                jj = j-1;
                if((ii >= 0 && ii < n) && (jj >= 0 && jj < n)) {
                    nouPonts.add(pont);
                }
                distancias[i][j].setPonts(nouPonts);
            }
        }
        
        if(player == PLAYER1) {
            int colorActual = getColor(player);
            int colorRival = getColor(opposite(player));
            for (int j = 0; j < n; j++) {
                Point p = new Point(0, j);
                if(s.getPos(0, j) == colorActual) {
                    distancias[0][j] = (new Node(p, 0));
                    cola.add(distancias[0][j]);
                } else if(s.getPos(0, j) == colorRival) {
                    distancias[0][j] = (new Node(p, Integer.MAX_VALUE)); 
                } else {
                    distancias[0][j] = (new Node(p, 1));
                    cola.add(distancias[0][j]);
                }
            }
       
            while (!cola.isEmpty()) {
                Node current = cola.poll();
                Point p = current.punt;

                if (visited.contains(current)) {
                    continue;
                }

                visited.add(current);

                if (p.x == n-1) {
                    return current.distancia;
                }

                for (Point neighbor : s.getNeigh(p)) {
                    if(s.getPos(neighbor.x, neighbor.y) != colorRival) {
                        if (!visited.contains(distancias[neighbor.x][neighbor.y])) {
                            int newDist = 0;
                            if(s.getPos(neighbor.x, neighbor.y) == colorActual) {
                                newDist = distancias[p.x][p.y].distancia;
                            } else {
                                newDist = distancias[p.x][p.y].distancia + 1;
                            }
                            if (newDist < distancias[neighbor.x][neighbor.y].distancia) {
                                distancias[neighbor.x][neighbor.y].distancia = newDist;
                                cola.add(new Node(neighbor, newDist));
                            }
                        }
                    }
                }
            }           
        } else {
            int colorActual = getColor(player);
            int colorRival = getColor(opposite(player));
            for (int i = 0; i < n; i++) {
                Point p = new Point(i, 0);
                if(s.getPos(i, 0) == colorActual) {
                    distancias[i][0] = (new Node(p, 0));
                    cola.add(distancias[i][0]);
                } else if(s.getPos(1, 0) == colorRival) {
                    distancias[i][0] = (new Node(p, Integer.MAX_VALUE)); 
                } else {
                    distancias[i][0] = (new Node(p, 1));
                    cola.add(distancias[i][0]);
                }
            }

            while (!cola.isEmpty()) {
                Node current = cola.poll();
                Point p = current.punt;

                if (visited.contains(current)) {
                    continue;
                }

                visited.add(current);

                if (p.y == n-1) {
                    return current.distancia;
                }

                for (Point neighbor : s.getNeigh(p)) {
                    if(s.getPos(neighbor.x, neighbor.y) != colorRival) {
                        if (!visited.contains(distancias[neighbor.x][neighbor.y])) {
                            int newDist = 0;
                            if(s.getPos(neighbor.x, neighbor.y) == colorActual) {
                                newDist = distancias[p.x][p.y].distancia;
                            } else {
                                newDist = distancias[p.x][p.y].distancia + 1;
                            }
                            if (newDist < distancias[neighbor.x][neighbor.y].distancia) {
                                distancias[neighbor.x][neighbor.y].distancia = newDist;
                                cola.add(new Node(neighbor, newDist));
                            }
                        }
                    }
                }
            }   
        }
        return Integer.MAX_VALUE; 
    }
 
    private static class Node {
        Point punt;
        int distancia;
        List<Point> ponts;

        public Node(Point punt, int distancia) {
            this.punt = punt;
            this.distancia = distancia;
        }
        
        public void setPonts(List<Point> ponts) {
            this.ponts = new ArrayList<>(ponts); 
        }

    }
    
    @Override
    public String getName() {
        return nom;
    }
    
}