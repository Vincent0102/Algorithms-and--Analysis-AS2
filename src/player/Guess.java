package player;

/**
 * This class defines the format of a guess
 *
 * @author Jeffrey Chan, Youhan Xia
 */
public class Guess {
    /** row of cell to fire at. */
    public int row = 0;
    /** column of cell to fire at. */
    public int column = 0;

    /**
     * Prints out guess information.
     */
    @Override
    public String toString() {
        return "guesses/fires at row " + row + " column " + column + '.';
    }
}
