package edu.upc.epsevg.prop.hex.players;


import edu.upc.epsevg.prop.hex.HexGameStatus;
import edu.upc.epsevg.prop.hex.IAuto;
import edu.upc.epsevg.prop.hex.IPlayer;
import edu.upc.epsevg.prop.hex.MoveNode;
import edu.upc.epsevg.prop.hex.PlayerMove;
import edu.upc.epsevg.prop.hex.SearchType;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    /**
     * Decideix el moviment del jugador donat un tauler i un color de peça que
     * ha de posar.
     *
     * @param s Tauler i estat actual de joc.
     * @return el moviment que fa el jugador.
     */
    @Override
    public PlayerMove move(HexGameStatus s) {
        return null;
    }

    /**
     * Ens avisa que hem de parar la cerca en curs perquè s'ha exhaurit el temps
     * de joc.
     * @return 
     */
    @Override
    public String getName() {
        return nom;
    }

}
