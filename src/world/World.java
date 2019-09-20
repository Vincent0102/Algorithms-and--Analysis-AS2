package world;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import player.Guess;
import ship.*;

/**
 * Class modelling the game "world" for a player.  It includes the grid,
 * location of their ships, and all guesses from their opponents.
 *
 * The visualisation code uses an object of this class to draw correctly.
 *
 * @author Youhan Xia, Jeffrey Chan
 */
public class World {
    // nested class for keeping the coordinates of ships or shots.
    public class Coordinate {
        public int row;
        public int column;

    	@Override
    	public boolean equals(Object object) {
    	    boolean isSame = false;
            if (object != null && object instanceof Coordinate) {
    		    Coordinate other = (Coordinate) object;
    		    isSame = (this.row == other.row && this.column == other.column);
            }

            return isSame;
    	}

    }

    // nested class for storing ship locations.
    public class ShipLocation {
        public Ship ship;
        public ArrayList<Coordinate> coordinates = new ArrayList<>();
    }

    // The size of grid.
    public int numRow;
    public int numColumn;

    // which player this world is.
    int player = 0;

    // True if the map is hexagonal.
    public boolean isHex = false;

    // Ship locations and shot history.
    public ArrayList<ShipLocation> shipLocations = new ArrayList<>();
    public ArrayList<Coordinate> shots = new ArrayList<>();

    // True if visualisation switch is on.
    boolean isVisual = false;

    // To calculate the steps of coordinates in each direction.
    static final String dirs = "NWSENESW";
    static final int rowDeltas[] = {1, 0, -1, 0, 1, 0, -1, 0};
    static final int clnDeltas[] = {0, -1, 0, 1, 1, 0, -1, 0};

    /**
     * Check whether a given point is in the map.
     *
     * @param cdn coordinates of the given point.
     *
     * @return true if cdn is in the map.
     */
    boolean isIn(Coordinate cdn) {
        if (isHex)
            return cdn.row >= 0 && cdn.row < numRow && cdn.column >= (cdn.row + 1) / 2 && cdn.column < numColumn + (cdn.row + 1) / 2;
        else
            return cdn.row >= 0 && cdn.row < numRow && cdn.column >= 0 && cdn.column < numColumn;
    }

    /**
     * Setup the map, ship locations and whether to visualise.
     *
     * @param configFile configuration input file name.
     * @param locFile ship location input file name.
     * @param isVisual whether to visualise the game.
     *
     * @return true if all input files are existing and valid.
     *
     * @throws FileNotFoundException when input files missing, throws exception to caller.
     */
    public boolean setupWorld(String configFile, String locFile, int player, boolean isVisual) throws FileNotFoundException {
        this.player = player;
        this.isVisual = isVisual;
        Scanner configScanner = new Scanner(new File(configFile));
        Scanner locScanner = new Scanner(new File(locFile));
        locScanner.useDelimiter(Pattern.compile("\\s+"));

        numRow = configScanner.nextInt();
        numColumn = configScanner.nextInt();
        if (configScanner.hasNext() && "hex".equals(configScanner.next())) {
            isHex = true;
        }

        while (locScanner.hasNext()) {
            ShipLocation shipLoc = new ShipLocation();
            String shipName = locScanner.next();
            switch (shipName) {
                case "AircraftCarrier":
                    shipLoc.ship = new AircraftCarrier();
                    break;
                case "Frigate":
                    shipLoc.ship = new Frigate();
                    break;
                case "Submarine":
                    shipLoc.ship = new Submarine();
                    break;
                case "Cruiser":
                    shipLoc.ship = new Cruiser();
                    break;
                case "PatrolCraft":
                    shipLoc.ship = new PatrolCraft();
                    break;
                default:
                    return false;
            }
            int startRow = locScanner.nextInt();
            int startCln = locScanner.nextInt();
            // main direction
            int mainDir = dirs.indexOf(locScanner.next());
            // secondary direction
            int secDir = dirs.indexOf(locScanner.next());

            for (int j = 0; j < shipLoc.ship.len(); j++) {
                for (int k = 0; k < shipLoc.ship.width(); k++) {
                    Coordinate cdn = new Coordinate();
                    cdn.row = startRow + j * rowDeltas[mainDir] + k * rowDeltas[secDir];
                    cdn.column = startCln + j * clnDeltas[mainDir] + k * clnDeltas[secDir];
                    if (!isIn(cdn)) return false;
                    shipLoc.coordinates.add(cdn);
                }
            }
            shipLocations.add(shipLoc);
        }
        configScanner.close();
        locScanner.close();
        return true;
    } // end of setupWorld()

    /**
     * update the shot history with a given guess.
     *
     * @param guess to be recorded.
     *
     * @return true if the guess is within the map and not repeated.
     */
    public boolean updateShot(Guess guess) {
        if (guess == null) {
            return false;
        }

        int row = guess.row;
        int column = guess.column;
        Coordinate cdn = new Coordinate();
        cdn.row = row;
        cdn.column = column;
        if (shots.contains(cdn)) {
            return false;
        }
        shots.add(cdn);

        return isIn(cdn);
    }

    /**
     * Draw the map and ships when the game begins.
     */
    public void draw() {
        if(!isVisual) return;

        int offset = player - 1;
        int d = offset * (numColumn + 1);
        if (isHex) {
            if (offset ==0) {
                StdDraw.setCanvasSize(1000, 5000);
                StdDraw.setXscale(-1, 2*(numColumn+1)+1);
                StdDraw.setYscale(-1, numRow+1);
            }

            // draw grids
            StdDraw.setPenColor(StdDraw.BLACK);
            StdDraw.setPenRadius();
            double halfEdge = 1.0 / 3;
            for (int r = 0; r < numRow; r++) {
                for (int c = d; c < d + numColumn; c++) {
                    double shift = r % 2 * 0.5;
                    StdDraw.line(c+1+shift, r+0.5-halfEdge, c+1+shift, r+0.5+halfEdge);
                    StdDraw.line(c+0.5+shift, r+0.5+2*halfEdge, c+1+shift, r+0.5+halfEdge);
                    StdDraw.line(c+0.5+shift, r+0.5+2*halfEdge, c+shift, r+0.5+halfEdge);
                    StdDraw.line(c+shift, r+0.5-halfEdge, c+shift, r+0.5+halfEdge);
                    StdDraw.line(c+shift, r+0.5-halfEdge, c+0.5+shift, r+0.5-2*halfEdge);
                    StdDraw.line(c+1+shift, r+0.5-halfEdge, c+0.5+shift, r+0.5-2*halfEdge);
                }
            }

            // draw ships
            StdDraw.setPenColor(StdDraw.GRAY);
            StdDraw.setPenRadius();
            for (ShipLocation shipLoc : shipLocations) {
                for (Coordinate cdn : shipLoc.coordinates) {
                    double r = cdn.row;
                    double shift = cdn.row % 2 * 0.5;
                    double c = d + cdn.column - (cdn.row + 1) / 2;
                    double x[] = {c+1+shift, c+1+shift, c+0.5+shift, c+shift, c+shift, c+0.5+shift};
                    double y[] = {r+0.5-halfEdge, r+0.5+halfEdge, r+0.5+2*halfEdge, r+0.5+halfEdge, r+0.5-halfEdge, r+0.5-2*halfEdge};
                    StdDraw.filledPolygon(x, y);
                }
            }

        } else {
            if (offset ==0) {
                StdDraw.setCanvasSize(1000, 500);
                StdDraw.setXscale(-1, 2*(numColumn+1));
                StdDraw.setYscale(-1, numRow+1);
            }

            // draw grids
            StdDraw.setPenColor(StdDraw.BLACK);
            StdDraw.setPenRadius();
            for (int r = 0; r < numRow; r++) {
                for (int c = 0; c < numColumn; c++) {
                    StdDraw.line(d+c+1, r, d+c+1, r+1);
                    StdDraw.line(d+c, r+1, d+c+1, r+1);
                    StdDraw.line(d+c, r, d+c, r+1);
                    StdDraw.line(d+c, r, d+c+1, r);
                }
            }

            // draw ships
            StdDraw.setPenColor(StdDraw.LIGHT_GRAY);
            StdDraw.setPenRadius();
            for (ShipLocation shipLoc : shipLocations) {
                for (Coordinate cdn : shipLoc.coordinates) {
                    StdDraw.filledRectangle(d + cdn.column + 0.5, cdn.row + 0.5, 0.5, 0.5);
                }
            }

        }
    } // end of draw()


    /**
     * Draw a red cross for each shot.
     *
     * @param guess the shot to draw.
     */
    public void drawShot(Guess guess) {
        if(!isVisual) return;
        int offset = player - 1;

        int d = offset * (numColumn + 1);
            StdDraw.setPenColor(StdDraw.RED);
            StdDraw.setPenRadius(0.01);
        if (isHex) {
            int r = guess.row;
            double c = guess.column - (r + 1) / 2 + r % 2 * 0.5;;
            StdDraw.line(d+c+0.8, r+0.2, d+c+0.2, r+0.8);
            StdDraw.line(d+c+0.8, r+0.8, d+c+0.2, r+0.2);
        } else {
            int r = guess.row;
            int c = guess.column;
            StdDraw.line(d+c+0.8, r+0.2, d+c+0.2, r+0.8);
            StdDraw.line(d+c+0.8, r+0.8, d+c+0.2, r+0.2);
        }

        // Make it slower
        try {
            TimeUnit.MILLISECONDS.sleep(300);
        } catch (InterruptedException ex) {
            // do nothing
        }
    } // end of drawShot()

} // end of class World
