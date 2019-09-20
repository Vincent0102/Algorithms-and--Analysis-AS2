package player;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import world.World;
import world.World.Coordinate;
import world.World.ShipLocation;

/**
 * Greedy guess player (task B). Please implement this class.
 *
 * @author Youhan Xia, Jeffrey Chan
 */
public class GreedyGuessPlayer implements Player {

	World world;
	int numRow, numColumn;
	boolean targetMode = false;
	ArrayList<Coordinate> hitCoordinates = new ArrayList<Coordinate>();  // store hit coordinates
	ArrayList<Targeted> targetedList = new ArrayList<Targeted>(); // store list of coordinates that triggers targetMode
	int hit, shipSunkNum;

	// store world object locally and initialise world size
	@Override
	public void initialisePlayer(World world) {
		this.world = world;
		this.numColumn = world.numColumn;
		this.numRow = world.numRow;
		hit = 0;
		shipSunkNum = 0;
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
					answer.shipSunk = shipLoc.ship;
				}
			}
		}
		return answer;
	} // end of getAnswer()

	// generate a random coordinate with 2 modes (parity mode / target mode)
	@Override
	public Guess makeGuess() {
		Guess guess = new Guess();
		Coordinate cor = world.new Coordinate();
		do {
			// parity mode
			if (targetMode == false) {
				do {
					guess.row = ThreadLocalRandom.current().nextInt(0, numRow);
					guess.column = ThreadLocalRandom.current().nextInt(0, numColumn);
				} while ((guess.row + guess.column) % 2 == 0); // random coordinates for parity mode
			} 
			// target mode
			else 
			{
				int i;
				Targeted tar = targetedList.get(targetedList.size() - 1);
				for (i = 0; i < 4; i++) {
					if (tar.adjacents[i] == false) {
						break;
					}
				}
				// attacks 4 adjacent cells
				switch (i) {
				case 0:
					guess.column = tar.cor.column - 1;
					guess.row = tar.cor.row;
					break;
				case 1:
					guess.column = tar.cor.column;
					guess.row = tar.cor.row + 1;
					break;
				case 2:
					guess.column = tar.cor.column + 1;
					guess.row = tar.cor.row;
					break;
				case 3:
					guess.column = tar.cor.column;
					guess.row = tar.cor.row - 1;
					break;
				}
				// turns targetMode off when all adjacent cells have been hit
				if (i == 4) {
					targetedList.remove(targetedList.size() - 1);
					if (!(targetedList.size() > 0))
						targetMode = false;
					continue;
				}
				tar.adjacents[i] = true;
			}
			cor.column = guess.column;
			cor.row = guess.row;
		} while (hitCoordinates.contains(cor) || cor.column > numColumn - 1 || cor.row > numRow - 1 || cor.column < 0
				|| cor.row < 0); // verification to generate valid coordinate
		hitCoordinates.add(cor);
		return guess;
	} // end of makeGuess()

	// update player with info to make it smarter :) using 'stack' data structure
	@Override
	public void update(Guess guess, Answer answer) {
		if (answer.isHit == true) {
			hit++;
		}
		// turns targetMode on if ship is hit
		if (answer.isHit == true && targetMode == false) {
			targetMode = true;
			Coordinate temp = world.new Coordinate();
			temp.column = guess.column;
			temp.row = guess.row;
			Targeted tar = new Targeted(temp);
			targetedList.add(tar);
			return;
		}
		// adds coordinate into targetedList when targetMode is on and ship is hit
		else if (answer.isHit == true && targetMode == true && answer.shipSunk == null) {
			Coordinate temp = world.new Coordinate();
			temp.column = guess.column;
			temp.row = guess.row;
			Targeted tar = new Targeted(temp);
			targetedList.add(tar);
			return;
		}
		// turns targetMode off when ship has been sunken 
		else if (answer.isHit == true && targetMode == true && answer.shipSunk != null) {
			shipSunkNum += answer.shipSunk.len() * answer.shipSunk.width();
			if (hit == shipSunkNum) {
				targetedList.clear();
				targetMode = false;
				hit=0;
				shipSunkNum=0;
				return;
			}
		}

		// check if there are leftover ships to be sunken by checking the coordinates in the targetedList
		if (!(targetedList.isEmpty())) {
			Targeted tar = targetedList.get(targetedList.size() - 1);

			boolean leftover = false;
			for (int i = 0; i < tar.adjacents.length; i++) {
				if (tar.adjacents[i] == false) {
					leftover = true;
					break;
				}
			}

			if (leftover == false) {
				targetedList.remove(targetedList.size() - 1);
				if (!(targetedList.size() > 0))
					targetMode = false;
			}
		}
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

	private class Targeted {
		Coordinate cor;
		boolean adjacents[] = { false, false, false, false };

		public Targeted(Coordinate cor) {
			this.cor = cor;
		}
	}
} // end of class GreedyGuessPlayer
