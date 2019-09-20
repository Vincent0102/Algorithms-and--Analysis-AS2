
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;
import player.*;
import world.*;

/**
 * Battleship game main class
 *
 * @author Jeffrey Chan, Youhan Xia
 */
public class BattleshipMain {

    /**
     * Name of class, used in error messages.
     */
    protected static final String progName = "Battleship";

    /**
     * Print help/usage message.
     */
    public static void usage(String progName) {
        System.err.println(progName + ":[-v] [-l <game log file>] <game configuration file> <ship location file 1> <ship location file 2> <player 1 type> <player 2 type>");
        System.err.println("<player x type> = random | greedy | prob | custom");
        System.exit(1);
    } // end of usage

    public static Player constructPlayer(String playerName) {
        switch (playerName) {
            case "random":
                return new RandomGuessPlayer();
            case "greedy":
                return new GreedyGuessPlayer();
            case "prob":
                return new ProbabilisticGuessPlayer();
            case "sample":
                return new SampleRandomGuessPlayer();
            default:
                return null;
        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        PrintWriter logWriter = null;
        int offset = 0;
        boolean isVisual = false;

        // Check the number of command line arguments
        if (args.length < 5) {
            System.err.println("Incorrect number of arguments.");
            usage(progName);
        }

        while (true) {
            if ("-v".equals(args[offset])) {
                // see if we need to visualise the game
                isVisual = true;
                offset++;
            } else if ("-l".equals(args[offset])) {
                // see if we need to log game trace
                try {
                    logWriter = new PrintWriter(args[offset + 1]);
                } catch (FileNotFoundException ex) {
                    System.err.println("Game log file not found.");
                    usage(progName);
                }
                offset += 2;
            } else {
                break;
            }
        }

        // ensure there is the correct number of command line files
        if (args.length == offset + 5) {
            // construct players
            Player p1 = constructPlayer(args[offset + 3]);
            Player p2 = constructPlayer(args[offset + 4]);
            if (p1 == null || p2 == null) {
                System.err.println("Incorrect argument value for players.");
                usage(progName);
            } else {
                // setup the game world, including the shape and size of the
                // map, locations of the ships, whether to visualise.
                World world1 = new World();
                World world2 = new World();
                try {
                    // if either world failed to setup
                    if (!world1.setupWorld(args[offset], args[offset + 1], 1, isVisual) || !world2.setupWorld(args[offset], args[offset + 2], 2, isVisual)) {
                        System.err.println("Read input files failed.");
                        usage(progName);
                    }
                } catch (FileNotFoundException ex) {
                    System.err.println("ships location file not found.");
                    usage(progName);
                }

                p1.initialisePlayer(world1);
                p2.initialisePlayer(world2);

                // draw (if visualising)
                world1.draw();
                world2.draw();

                //
                // Run the game.
                // The game will run until both players have finished (i.e,
                // destroyed all ships).  Whichever player used less rounds to
                // destroyed all of the opponent ships wins.  If the same number
                // of turns, then it is a draw.
                //

                int Round = 0;
                // true if player 1 has all their ships destroyed
                boolean p1HasNoShipsLeft = false;
                // true if player 2 has all their ships destroyed
                boolean p2HasNoShipsLeft = false;
                int p1Rounds = 0;
                int p2Rounds = 0;

                while (!p1HasNoShipsLeft || !p2HasNoShipsLeft) {
                    Round++;
                    if (logWriter != null) logWriter.println("Round " + Round);

                    // Player 1's turn
                    // Keep playing as long as player 2 still have ships
                    if (!p2HasNoShipsLeft) {
                        // player 1 makes a guess
                        Guess guess = p1.makeGuess();
                        if (logWriter != null) {
                            logWriter.println("Player 1 " + guess);
                        }

                        // Check whether the guess is valid.
                        if (!world2.updateShot(guess)) {
                            System.err.println("Invalid guess or repeated guess.");
                            if (logWriter != null) {
                                logWriter.println("Invalid guess or repeated guess.");
                            }
                        } else {
                            world2.drawShot(guess);
                        }
                        // player 2 answers player 1
                        Answer answer = p2.getAnswer(guess);
                        // player 1 updates their own state
                        p1.update(guess, answer);
                        if (logWriter != null) {
                            logWriter.println("Player 2 " + answer);
                        }
                        // check if player 2 has ships left
                        if (p2.noRemainingShips()) {
                            p1Rounds = Round;
                            p2HasNoShipsLeft = true;
                        }
                    }

                    // Player 2's turn
                    // Keep playing as long as player 1 still have ships
                    if (!p1HasNoShipsLeft) {
                        // player 2 makes a guess
                        Guess guess = p2.makeGuess();
                        if (logWriter != null) {
                            logWriter.println("Player 2 " + guess);
                        }

                        // Check whether the guess is valid.
                        if (!world1.updateShot(guess)) {
                            System.err.println("Invalid guess or repeated guess.");
                            if (logWriter != null) {
                                logWriter.println("Invalid guess or repeated guess.");
                            }
                        } else {
                            world1.drawShot(guess);
                        }
                        // player 1 answers player 2
                        Answer answer = p1.getAnswer(guess);
                        // player 2 updates their own state
                        p2.update(guess, answer);
                        if (logWriter != null) {
                            logWriter.println("Player 1 " + answer);
                        }
                        // check if player 1 has ships left
                        if (p1.noRemainingShips()) {
                            p2Rounds = Round;
                            p1HasNoShipsLeft = true;
                        }
                    }

                }
                System.err.println("Player 1 took " + p1Rounds +
                    " rounds to destroy opponent's ships.");
                System.err.println("Player 2 took " + p2Rounds +
                    " rounds to destroy opponent's ships.");

                // determine who won or is it a draw
                if (p1Rounds < p2Rounds) {
                    System.err.println("Player 1 wins.");
                }
                else if (p1Rounds > p2Rounds) {
                    System.err.println("Player 2 wins.");
                }
                else {
                    System.err.println("It is a draw.");
                }

                // if not null, means we should log final game results
                if (logWriter != null) {
                    logWriter.println("Player 1 took " + p1Rounds +
                        " rounds to destroy opponent's ships.");
                    logWriter.println("Player 2 took " + p2Rounds +
                        " rounds to destroy opponent's ships.");

                    if (p1Rounds < p2Rounds) {
                        logWriter.println("Player 1 wins.");
                    }
                    else if (p1Rounds > p2Rounds) {
                        logWriter.println("Player 2 wins.");
                    }
                    else {
                        logWriter.println("It is a draw.");
                    }
                }
            }
        }
        else {
                System.err.println("Incorrect number of arguments.");
                usage(progName);
        }
        if (logWriter != null) logWriter.close();
    } // end of main()

} // end of class BattleshipMain
