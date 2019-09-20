package player;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import ship.Ship;
import world.World;
import world.World.Coordinate;
import world.World.ShipLocation;

/**
 * Random guess player (task A). Please implement this class.
 *
 * @author Youhan Xia, Jeffrey Chan
 */
public class RandomGuessPlayer implements Player {

	World world;
	int numRow, numColumn;

	// store hit coordinates
	ArrayList<Coordinate> hitCoordinates = new ArrayList<Coordinate>();

	// store world object locally and initialise world size
	@Override
	public void initialisePlayer(World world) {
		this.world = world;
		this.numColumn = world.numColumn;
		this.numRow = world.numRow;
	} // end of initialisePlayer()

	// check if coordinate guessed hits
	@Override
	public Answer getAnswer(Guess guess) {
		Answer answer = new Answer();
		Coordinate cor = world.new Coordinate();
		cor.column = guess.column;
		cor.row = guess.row;
		world.shots.add(cor);

		// check if hit coordinate has ship
		for (ShipLocation shipLoc : world.shipLocations) {
			if (shipLoc.coordinates.contains(cor)) {
				answer.isHit = true;
				int count = 0;
				for (Coordinate cdn : shipLoc.coordinates) {
					if (world.shots.contains(cdn)) {
						count++; 
					}
				}
				if (count == shipLoc.coordinates.size()) {
					answer.shipSunk = shipLoc.ship; // adds shipSunk
				}
			}
		}
		return answer;
	} // end of getAnswer()

	// make random guess / generate a random coordinate
	@Override
	public Guess makeGuess() {
		Guess guess = new Guess();
		Coordinate cor = world.new Coordinate();
		do {
			guess.column = ThreadLocalRandom.current().nextInt(0, numColumn);
			guess.row = ThreadLocalRandom.current().nextInt(0, numRow);
			cor.column = guess.column;
			cor.row = guess.row;
		} while (hitCoordinates.contains(cor)); // check if coordinate has been hit

		// save coordinate as hit coordinate
		hitCoordinates.add(cor);

		return guess;
	} // end of makeGuess()

	@Override
	public void update(Guess guess, Answer answer) {
		// nothing because randomGuessPlayer is dumb
	} // end of update()

	// check if there are ships remaining on the map
	@Override
	public boolean noRemainingShips() {
		int shipDown = 0;
		for (ShipLocation shipLoc : world.shipLocations) {
			int count = 0;
			for (Coordinate cdn : shipLoc.coordinates) {
				if (world.shots.contains(cdn)) {
					count++;
				}
			}
			if (count == shipLoc.coordinates.size()) {
				shipDown++;
			}
		}
		if (shipDown == 5)
			return true; // return true if all ships are sunk
		else
			return false;
	} // end of noRemainingShips()

} // end of class RandomGuessPlayer
