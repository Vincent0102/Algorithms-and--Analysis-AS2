package player;

import ship.Ship;

/**
 * This class defines the format of an answer to a guess.
 *
 * @author Jeffrey Chan, Youhan Xia
 */
public class Answer {
    /** True if the guess was for a cell containing a ship, otherwise False. */
    boolean isHit = false;
    /** If isHit is True (hit ship) and this hit destroyed a shit, this
        shipSunk attribute should be set to the object of ship destroyed. */
    Ship shipSunk = null;

    /**
     * Prints out answer information.
     */
    @Override
    public String toString() {
        String str = "answers that ";

        if (isHit) {
            if (shipSunk != null) {
                str += "the " + shipSunk.name() + " is hit and sunk.";
            } else {
                str += "a ship is hit.";
            }
        } else {
            str += "guess/shot missed.";
        }

        return str;
    } // end of toString()

} // end of class Answer
