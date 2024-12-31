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
        return (mes_infinit - puntuacioActual) - (mes_infinit - puntuacioRival);
    }
    
    
    public static int dijkstra(HexGameStatus s, PlayerType player) {
        int n = s.getSize();
        Node[][] distancias = new Node[n][n];
        PriorityQueue<Node> cola = new PriorityQueue<>(Comparator.comparingInt(node -> node.distancia));
        //Set<Node> visited = new HashSet<>();
        boolean[][] visited = new boolean[n][n];
        int colorActual = getColor(player);
        int colorRival = getColor(opposite(player));

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Point p = new Point(i, j);
                distancias[i][j] = new Node(p, Integer.MAX_VALUE);
                distancias[i][j].setPonts(generateBridges(p, n));
            }
        }
        
        if (player == PLAYER1) {
            for (int j = 0; j < n; j++) {
                if (s.getPos(0, j) == colorActual) {
                    distancias[0][j].setDistancia(0);
                    cola.add(distancias[0][j]);
                } else if (s.getPos(0, j) == colorRival) {
                    distancias[0][j].setDistancia(Integer.MAX_VALUE);
                } else {
                    distancias[0][j].setDistancia(1);
                    cola.add(distancias[0][j]);
                }
            }
            
            distancias[1][9].setDistancia(0);
            cola.add(distancias[1][9]);
        } else {
            for (int i = 0; i < n; i++) {
                if (s.getPos(i, 0) == colorActual) {
                    distancias[i][0].setDistancia(0);
                    cola.add(distancias[i][0]);
                } else if (s.getPos(i, 0) == colorRival) {
                    distancias[i][0].setDistancia(Integer.MAX_VALUE);
                } else {
                    distancias[i][0].setDistancia(1);
                    cola.add(distancias[i][0]);
                }
            }
            distancias[9][1].setDistancia(0);
            cola.add(distancias[9][1]);
        }

        while (!cola.isEmpty()) {
            Node current = cola.poll();
            Point p = current.punt;
            if (visited[p.x][p.y]) continue;
            visited[p.x][p.y] = true;

            if ((player == PLAYER1 && p.x == n - 1) || (player == PLAYER2 && p.y == n - 1)) {
                return current.distancia;
            }
            
            

            for (Point neighbor : s.getNeigh(p)) {
                int bonus = 0;
                int penalizacion = 0;
                
                if ( s.getPos(p)== colorActual)bonus = intermediasBonus(current,neighbor,s,colorActual,colorRival);
                else penalizacion = blocked_path(distancias[neighbor.x][neighbor.y],s,colorActual,colorRival,distancias);
                  
                    
                if (s.getPos(neighbor.x, neighbor.y) != colorRival) {
                    int newDist = distancias[p.x][p.y].distancia + (s.getPos(neighbor.x, neighbor.y) == colorActual ? 0 : (2 - bonus + penalizacion));
                    // if (bonus == 1) System.out.println("BONUS ACTIVADO, SOY EL PUNTO: " + p + "Y MI DISTANCIA ES: " + distancias[p.x][p.y].distancia + "NEWDIST: " + newDist + "y la del vecino es : " + distancias[neighbor.x][neighbor.y].distancia);
                    if (newDist < distancias[neighbor.x][neighbor.y].distancia) {
                        distancias[neighbor.x][neighbor.y].setDistancia(newDist);
                        cola.add(distancias[neighbor.x][neighbor.y]);
                    }
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    private static List<Point> generateBridges(Point p, int n) {
        List<Point> bridges = new ArrayList<>();
        int[][] offsets = {{1, -2}, {2, -1}, {1, 1}, {-1, -2}, {-2, 1}, {-1, 1}};
        for (int[] offset : offsets) {
            int x = p.x + offset[0];
            int y = p.y + offset[1];
            if (x >= 0 && x < n && y >= 0 && y < n) {
                bridges.add(new Point(x, y));
            }
        }
        return bridges;
    }
    
    private static int blocked_path(Node inter, HexGameStatus s, int colorActual, int colorRival, Node[][] distancias){
        Point intermedio = inter.punt;
        List<Point> enemys = new ArrayList<>();
        List<Point> neighs = s.getNeigh(intermedio);
        List<Point> intermedias = new ArrayList<>();
        int bonus = 0;
        for (Point n : neighs){
            if (s.getPos(n) == colorRival){
                enemys.add(n);
            }
        }
        
        for (Point enemy : enemys){
            List<Point> bridges = distancias[enemy.x][enemy.y].ponts;
            for (Point bridge : bridges){
                if (enemys.contains(bridge)){
                    bonus = 1000;// hay un puente del rival
                    intermedias = getIntermedias(enemy,bridge,s);
                    break;
                }  
            }
            
            //compruebo si la otra intermedia esta ocupada:
            for ( Point p : intermedias){
                if ( s.getPos(p) == colorActual || s.getPos(p) == colorRival){
                    bonus = 0;
                }
            }
        }
        
        return bonus;
    }
    
    private static int intermediasBonus(Node n, Point neighbor, HexGameStatus s, int colorActual, int colorRival){
    
        int bonus = 0;
        for (Point bridge : n.ponts) {
          if (s.getPos(bridge.x, bridge.y) == colorActual) {
              List<Point> intermedias = getIntermedias(n.punt, bridge,s);
              if ( intermedias.contains(neighbor)) bonus = 1;
              if ( bonus == 1){
                for ( Point inter : intermedias){
                    if (s.getPos(inter) == colorActual || s.getPos(inter) == colorRival){
                        bonus = 0;
                    }   
                }
              }  
          }  
        }
        
        return bonus;
        
    }
    
    private static List<Point> getIntermedias(Point p, Point bridge, HexGameStatus s){
        
        List<Point> pNeigh = s.getNeigh(p);
        List<Point> bridgeNeigh = s.getNeigh(bridge);
        List<Point> comunes = new ArrayList<>();
        for (Point pn : pNeigh) {
            if (bridgeNeigh.contains(pn)) {
                comunes.add(pn);
            }
        }
        return comunes;
    }

    private static int calculateBridgeBonus(Point p, Node[][] distancias, HexGameStatus s, int colorActual) {
        for (Point bridge : distancias[p.x][p.y].ponts) {
            if (s.getPos(bridge.x, bridge.y) == colorActual) {
                return 1;
            } 
        }
        return 0;
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
        
        public Point getPont(int i){
            return this.ponts.get(i);
        }
        public void setDistancia(int distancia){
            this.distancia = distancia;
        }

    }
    
    @Override
    public String getName() {
        return nom;
    }
    
}