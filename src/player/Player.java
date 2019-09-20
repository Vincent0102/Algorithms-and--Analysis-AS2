package player;

import java.util.Scanner;
import world.World;

/**
 * Superclass of all types of player.
 * You should implement all these methods for the different type of player
 * classes.
 *
 * @author Youhan Xia, Jeffrey Chan
 */
public interface Player {

    /**
     * Initialise the player using the world object.
     * 
     * @param world world object contains the configuration and ship locations
     */
    void initialisePlayer(World world);


    /**
     * Answer a guess from the opponent.
     *
     * @param guess from the opponent.
     * 
     * @return Answer object holding the player's answer.
     */
    Answer getAnswer(Guess guess);


    /**
     * Generate/make a guess.
     *
     * @return Guess object.
     */
    Guess makeGuess();


    /**
     * Callback to allow player to process the answer of their guess and possibly
     * update their internal state.
     *
     * @param guess Guess of this player.
     * @param answer Answer to the guess from opponent.
     */
    void update(Guess guess, Answer answer);


    /**
     * Check whether all the ships of this player have been destroyed by the
     * opponent.
     *
     * @return True if there are no ship remaining, i.e., all ships sunk.
     */
    boolean noRemainingShips();

} // end of interface Player
