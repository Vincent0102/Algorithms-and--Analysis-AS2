package player;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import ship.*;
import ship.Ship;
import world.World;
import world.World.Coordinate;
import world.World.ShipLocation;

/**
 * Probabilistic guess player (task C). Please implement this class.
 *
 * @author Youhan Xia, Jeffrey Chan
 */
public class ProbabilisticGuessPlayer implements Player {

	private World world;
	private int numRow, numColumn;
	private boolean targetMode = false;
	private Cell[][] count;
	private ArrayList<Coordinate> shotList = new ArrayList<Coordinate>();
	private ArrayList<Ship> sunkList = new ArrayList<Ship>();
	private ArrayList<Coordinate> myOrder = new ArrayList<Coordinate>();
	private int n, e, s, w;
	private int hit, shipSunkNum;

	/*
	 * Initial a world, put the number of column and row to numColumn and numRow.
	 * Initial a two-dimensional Cell arraylist for storing the coordinate and
	 * probability of cell. Initial the hit and shipSunkNum with 0, when continue
	 * hits are finish(ships sunk), they will become 0.
	 */
	@Override
	public void initialisePlayer(World world) {
		this.world = world;
		numColumn = world.numColumn;
		numRow = world.numRow;
		count = new Cell[world.numColumn][world.numRow];
		Coordinate temp = world.new Coordinate();
		for (int i = 0; i < world.numColumn; i++) {
			for (int j = 0; j < world.numRow; j++) {
				temp.column = i;
				temp.row = j;
				count[i][j] = new Cell(temp);
			}

		}
		hit = 0;
		shipSunkNum = 0;
	} // end of initialisePlayer()

	@Override
	public Answer getAnswer(Guess guess) {
		Answer answer = new Answer();
		Coordinate cor = world.new Coordinate();
		cor.column = guess.column;
		cor.row = guess.row;
		world.shots.add(cor);
		for (ShipLocation shipLoc : world.shipLocations) {
			if (shipLoc.coordinates.contains(cor)) {
				answer.isHit = true;
				int ct = 0;
				for (Coordinate cdn : shipLoc.coordinates) {
					if (world.shots.contains(cdn)) {
						ct++;
					}
				}
				if (ct == shipLoc.coordinates.size()) {
					answer.shipSunk = shipLoc.ship;
				}
			}
		}
		return answer;
	} // end of getAnswer()

	@Override
	public Guess makeGuess() {
		Guess guess = new Guess();
		Coordinate cor = world.new Coordinate();
		int cc = 0;
		int i = 0;
		int j = 0;
		if (targetMode == false) {
			/*
			 * In hunting model, count probabilities of all cells and put the maximum
			 * probability of cell to cc
			 */
			for (i = 0; i < world.numColumn; i++) {
				for (j = 0; j < world.numRow; j++) {
					cor.column = i;
					cor.row = j;
					count[i][j].num = Count(cor);
					if (count[i][j].num > cc) {
						cc = count[i][j].num;
					}
				}
			}
			// randomly select a cell with the maximum probability
			do {
				guess.column = ThreadLocalRandom.current().nextInt(0, world.numColumn);
				guess.row = ThreadLocalRandom.current().nextInt(0, world.numRow);
			} while (count[guess.column][guess.row].num != cc);
		} else {
			// In the target model, select the latest one in the order as guess.
			guess.column = myOrder.get(myOrder.size() - 1).column;
			guess.row = myOrder.get(myOrder.size() - 1).row;
		}
		return guess;
	} // end of makeGuess()

	@Override
	public void update(Guess guess, Answer answer) {
		Coordinate tempCor = world.new Coordinate();
		tempCor.column = guess.column;
		tempCor.row = guess.row;
		if (answer.isHit == true) { // count every continue hits
			hit++;
		}
		if (answer.isHit == true && targetMode == false) {
			/*
			 * The first hit from hunting model to the target model.Count the probability of
			 * all adjacent cells and put the no-zero-probability cells into myOrder with
			 * the probability.
			 */
			targetMode = true;
			Count(tempCor);
			orderTargeted(tempCor, n, e, s, w);
		} else if (answer.isHit == true && targetMode == true && answer.shipSunk == null) {
			/*
			 * In the target mode already and continually hit a cell. Remove the latest
			 * coordinate that has been run. CCount and order the adjacent cells into
			 * arraylist.
			 */
			myOrder.remove(myOrder.size() - 1);
			Count(tempCor);
			orderTargeted(tempCor, n, e, s, w);
		} else if (answer.isHit == true && targetMode == true && answer.shipSunk != null) {
			/*
			 * In the target mode already and sink a ship. Add the sunk ship to the
			 * sunkList, and record the number of cells of the sunk ship. IF the number of
			 * the continue sunk ship(s) cells is equal to the number of continued hits,
			 * make the number of the continue sunk ship(s) cells and continued hits to
			 * zero, turn off the target model. ELSE, just remove the latest coordinate that
			 * has been run from order list.
			 */
			sunkList.add(answer.shipSunk);
			shipSunkNum += answer.shipSunk.len() * answer.shipSunk.width();
			if (hit == shipSunkNum) {
				myOrder.clear();
				targetMode = false;
				shipSunkNum = 0;
				hit = 0;
			} else {
				myOrder.remove(myOrder.size() - 1);
			}
		} else if (answer.isHit == false && targetMode == true) {
			// In the target mode and hit nothing, just remove the latest coordinate
			myOrder.remove(myOrder.size() - 1);
		}
		shotList.add(tempCor); // add the coordinate that has been shot to the shotList
	} // end of update()

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
			return true;
		else
			return false;
	} // end of noRemainingShips()

	/*
	 * Count the current coordinate probability and return it. Count the
	 * probabilities of adjacent cells of current coordinate, and record it in n, e,
	 * s, w
	 */
	private int Count(Coordinate cor) {
		int c = 0;
		n = 0;
		e = 0;
		s = 0;
		w = 0;
		Coordinate c1 = world.new Coordinate();
		Coordinate c2 = world.new Coordinate();
		Coordinate c3 = world.new Coordinate();
		Coordinate c4 = world.new Coordinate();
		Coordinate c5 = world.new Coordinate();
		if (shotList.contains(cor)) {
			// return 0 if the coordinate is already recorded in the shotList
			return 0;
		}
		for (ShipLocation shipLoc : world.shipLocations) {
			if (!sunkList.contains(shipLoc.ship)) {
				if (shipLoc.ship instanceof AircraftCarrier) {
					c1.column = cor.column;
					c1.row = cor.row + 1;
					c2.column = cor.column;
					c2.row = cor.row + 2;
					c3.column = cor.column + 1;
					c3.row = cor.row;
					c4.column = cor.column + 1;
					c4.row = cor.row + 1;
					c5.column = cor.column + 1;
					c5.row = cor.row + 2;
					if (!shotList.contains(c1) && !shotList.contains(c2) && !shotList.contains(c3)
							&& !shotList.contains(c4) && isIn(c5) && !shotList.contains(c5)) {
						c++;
						n++;
						e++;
					}
					c1.column = cor.column;
					c1.row = cor.row + 1;
					c2.column = cor.column;
					c2.row = cor.row + 2;
					c3.column = cor.column - 1;
					c3.row = cor.row;
					c4.column = cor.column - 1;
					c4.row = cor.row + 1;
					c5.column = cor.column - 1;
					c5.row = cor.row + 2;
					if (!shotList.contains(c1) && !shotList.contains(c2) && !shotList.contains(c3)
							&& !shotList.contains(c4) && isIn(c5) && !shotList.contains(c5)) {
						c++;
						n++;
						w++;
					}
					c1.column = cor.column;
					c1.row = cor.row - 1;
					c2.column = cor.column;
					c2.row = cor.row - 2;
					c3.column = cor.column - 1;
					c3.row = cor.row;
					c4.column = cor.column - 1;
					c4.row = cor.row - 1;
					c5.column = cor.column - 1;
					c5.row = cor.row - 2;
					if (!shotList.contains(c1) && !shotList.contains(c2) && !shotList.contains(c3)
							&& !shotList.contains(c4) && isIn(c5) && !shotList.contains(c5)) {
						c++;
						w++;
						s++;
					}
					c1.column = cor.column;
					c1.row = cor.row - 1;
					c2.column = cor.column;
					c2.row = cor.row - 2;
					c3.column = cor.column + 1;
					c3.row = cor.row;
					c4.column = cor.column + 1;
					c4.row = cor.row - 1;
					c5.column = cor.column + 1;
					c5.row = cor.row - 2;
					if (!shotList.contains(c1) && !shotList.contains(c2) && !shotList.contains(c3)
							&& !shotList.contains(c4) && isIn(c5) && !shotList.contains(c5)) {
						c++;
						e++;
						s++;
					}
					c1.column = cor.column;
					c1.row = cor.row + 1;
					c2.column = cor.column + 1;
					c2.row = cor.row;
					c3.column = cor.column + 1;
					c3.row = cor.row + 1;
					c4.column = cor.column + 2;
					c4.row = cor.row;
					c5.column = cor.column + 2;
					c5.row = cor.row + 1;
					if (!shotList.contains(c1) && !shotList.contains(c2) && !shotList.contains(c3)
							&& !shotList.contains(c4) && isIn(c5) && !shotList.contains(c5)) {
						c++;
						n++;
						e++;
					}
					c1.column = cor.column;
					c1.row = cor.row + 1;
					c2.column = cor.column - 1;
					c2.row = cor.row;
					c3.column = cor.column - 1;
					c3.row = cor.row + 1;
					c4.column = cor.column - 2;
					c4.row = cor.row;
					c5.column = cor.column - 2;
					c5.row = cor.row + 1;
					if (!shotList.contains(c1) && !shotList.contains(c2) && !shotList.contains(c3)
							&& !shotList.contains(c4) && isIn(c5) && !shotList.contains(c5)) {
						c++;
						n++;
						w++;
					}
					c1.column = cor.column;
					c1.row = cor.row - 1;
					c2.column = cor.column - 1;
					c2.row = cor.row;
					c3.column = cor.column - 1;
					c3.row = cor.row - 1;
					c4.column = cor.column - 2;
					c4.row = cor.row;
					c5.column = cor.column - 2;
					c5.row = cor.row - 1;
					if (!shotList.contains(c1) && !shotList.contains(c2) && !shotList.contains(c3)
							&& !shotList.contains(c4) && isIn(c5) && !shotList.contains(c5)) {
						c++;
						w++;
						s++;
					}
					c1.column = cor.column;
					c1.row = cor.row - 1;
					c2.column = cor.column + 1;
					c2.row = cor.row;
					c3.column = cor.column + 1;
					c3.row = cor.row - 1;
					c4.column = cor.column + 2;
					c4.row = cor.row;
					c5.column = cor.column + 2;
					c5.row = cor.row - 1;
					if (!shotList.contains(c1) && !shotList.contains(c2) && !shotList.contains(c3)
							&& !shotList.contains(c4) && isIn(c5) && !shotList.contains(c5)) {
						c++;
						e++;
						s++;
					}
					c1.column = cor.column;
					c1.row = cor.row + 1;
					c2.column = cor.column + 1;
					c2.row = cor.row;
					c3.column = cor.column + 1;
					c3.row = cor.row + 1;
					c4.column = cor.column - 1;
					c4.row = cor.row;
					c5.column = cor.column - 1;
					c5.row = cor.row + 1;
					if (!shotList.contains(c1) && !shotList.contains(c2) && isIn(c3) && !shotList.contains(c3)
							&& !shotList.contains(c4) && isIn(c5) && !shotList.contains(c5)) {
						c++;
						n++;
						e++;
						w++;
					}
					c1.column = cor.column;
					c1.row = cor.row - 1;
					c2.column = cor.column + 1;
					c2.row = cor.row;
					c3.column = cor.column + 1;
					c3.row = cor.row - 1;
					c4.column = cor.column - 1;
					c4.row = cor.row;
					c5.column = cor.column - 1;
					c5.row = cor.row - 1;
					if (!shotList.contains(c1) && !shotList.contains(c2) && isIn(c3) && !shotList.contains(c3)
							&& !shotList.contains(c4) && isIn(c5) && !shotList.contains(c5)) {
						c++;
						e++;
						s++;
						w++;
					}
					c1.column = cor.column - 1;
					c1.row = cor.row;
					c2.column = cor.column;
					c2.row = cor.row + 1;
					c3.column = cor.column - 1;
					c3.row = cor.row + 1;
					c4.column = cor.column;
					c4.row = cor.row - 1;
					c5.column = cor.column - 1;
					c5.row = cor.row - 1;
					if (!shotList.contains(c1) && !shotList.contains(c2) && isIn(c3) && !shotList.contains(c3)
							&& !shotList.contains(c4) && isIn(c5) && !shotList.contains(c5)) {
						c++;
						n++;
						s++;
						w++;
					}
					c1.column = cor.column + 1;
					c1.row = cor.row;
					c2.column = cor.column;
					c2.row = cor.row + 1;
					c3.column = cor.column + 1;
					c3.row = cor.row + 1;
					c4.column = cor.column;
					c4.row = cor.row - 1;
					c5.column = cor.column + 1;
					c5.row = cor.row - 1;
					if (!shotList.contains(c1) && !shotList.contains(c2) && isIn(c3) && !shotList.contains(c3)
							&& !shotList.contains(c4) && isIn(c5) && !shotList.contains(c5)) {
						c++;
						n++;
						e++;
						s++;
					}
				} else if (shipLoc.ship instanceof Cruiser) {
					c1.column = cor.column;
					c1.row = cor.row + 1;
					c2.column = cor.column + 1;
					c2.row = cor.row;
					c3.column = cor.column + 1;
					c3.row = cor.row + 1;
					if (!shotList.contains(c1) && !shotList.contains(c2) && isIn(c3) && !shotList.contains(c3)) {
						c++;
						n++;
						e++;
					}
					c1.column = cor.column;
					c1.row = cor.row + 1;
					c2.column = cor.column - 1;
					c2.row = cor.row;
					c3.column = cor.column - 1;
					c3.row = cor.row + 1;
					if (!shotList.contains(c1) && !shotList.contains(c2) && isIn(c3) && !shotList.contains(c3)) {
						c++;
						n++;
						w++;
					}
					c1.column = cor.column;
					c1.row = cor.row - 1;
					c2.column = cor.column - 1;
					c2.row = cor.row;
					c3.column = cor.column - 1;
					c3.row = cor.row - 1;
					if (!shotList.contains(c1) && !shotList.contains(c2) && isIn(c3) && !shotList.contains(c3)) {
						c++;
						w++;
						s++;
					}
					c1.column = cor.column;
					c1.row = cor.row - 1;
					c2.column = cor.column + 1;
					c2.row = cor.row;
					c3.column = cor.column + 1;
					c3.row = cor.row - 1;
					if (!shotList.contains(c1) && !shotList.contains(c2) && isIn(c3) && !shotList.contains(c3)) {
						c++;
						e++;
						s++;
					}
				} else if (shipLoc.ship instanceof Frigate) {
					c1.column = cor.column;
					c1.row = cor.row + 1;
					c2.column = cor.column;
					c2.row = cor.row + 2;
					c3.column = cor.column;
					c3.row = cor.row + 3;
					if (!shotList.contains(c1) && !shotList.contains(c2) && isIn(c3) && !shotList.contains(c3)) {
						c++;
						n++;
					}
					c1.column = cor.column;
					c1.row = cor.row - 1;
					c2.column = cor.column;
					c2.row = cor.row - 2;
					c3.column = cor.column;
					c3.row = cor.row - 3;
					if (!shotList.contains(c1) && !shotList.contains(c2) && isIn(c3) && !shotList.contains(c3)) {
						c++;
						s++;
					}
					c1.column = cor.column + 1;
					c1.row = cor.row;
					c2.column = cor.column + 2;
					c2.row = cor.row;
					c3.column = cor.column + 3;
					c3.row = cor.row;
					if (!shotList.contains(c1) && !shotList.contains(c2) && isIn(c3) && !shotList.contains(c3)) {
						c++;
						e++;
					}
					c1.column = cor.column - 1;
					c1.row = cor.row;
					c2.column = cor.column - 2;
					c2.row = cor.row;
					c3.column = cor.column - 3;
					c3.row = cor.row;
					if (!shotList.contains(c1) && !shotList.contains(c2) && isIn(c3) && !shotList.contains(c3)) {
						c++;
						w++;
					}
					c1.column = cor.column;
					c1.row = cor.row + 1;
					c2.column = cor.column;
					c2.row = cor.row + 2;
					c3.column = cor.column;
					c3.row = cor.row - 1;
					if (!shotList.contains(c1) && isIn(c2) && !shotList.contains(c2) && isIn(c3)
							&& !shotList.contains(c3)) {
						c++;
						n++;
						s++;
					}
					c1.column = cor.column;
					c1.row = cor.row + 1;
					c2.column = cor.column;
					c2.row = cor.row - 1;
					c3.column = cor.column;
					c3.row = cor.row - 2;
					if (isIn(c1) && !shotList.contains(c1) && !shotList.contains(c2) && isIn(c3)
							&& !shotList.contains(c3)) {
						c++;
						n++;
						s++;
					}
					c1.column = cor.column + 1;
					c1.row = cor.row;
					c2.column = cor.column + 2;
					c2.row = cor.row;
					c3.column = cor.column - 1;
					c3.row = cor.row;
					if (!shotList.contains(c1) && isIn(c2) && !shotList.contains(c2) && isIn(c3)
							&& !shotList.contains(c3)) {
						c++;
						e++;
						w++;
					}
					c1.column = cor.column + 1;
					c1.row = cor.row;
					c2.column = cor.column - 1;
					c2.row = cor.row;
					c3.column = cor.column - 2;
					c3.row = cor.row;
					if (isIn(c1) && !shotList.contains(c1) && !shotList.contains(c2) && isIn(c3)
							&& !shotList.contains(c3)) {
						c++;
						e++;
						w++;
					}
				} else if (shipLoc.ship instanceof PatrolCraft) {
					c1.column = cor.column;
					c1.row = cor.row + 1;
					if (isIn(c1) && !shotList.contains(c1)) {
						c++;
						n++;
					}
					c1.column = cor.column;
					c1.row = cor.row - 1;
					if (isIn(c1) && !shotList.contains(c1)) {
						c++;
						s++;
					}
					c1.column = cor.column + 1;
					c1.row = cor.row;
					if (isIn(c1) && !shotList.contains(c1)) {
						c++;
						e++;
					}
					c1.column = cor.column - 1;
					c1.row = cor.row;
					if (isIn(c1) && !shotList.contains(c1)) {
						c++;
						w++;
					}
				} else if (shipLoc.ship instanceof Submarine) {
					c1.column = cor.column;
					c1.row = cor.row + 1;
					c2.column = cor.column;
					c2.row = cor.row + 2;
					if (!shotList.contains(c1) && isIn(c2) && !shotList.contains(c2)) {
						c++;
						n++;
					}
					c1.column = cor.column;
					c1.row = cor.row - 1;
					c2.column = cor.column;
					c2.row = cor.row - 2;
					if (!shotList.contains(c1) && isIn(c2) && !shotList.contains(c2)) {
						c++;
						s++;
					}
					c1.column = cor.column + 1;
					c1.row = cor.row;
					c2.column = cor.column + 2;
					c2.row = cor.row;
					if (!shotList.contains(c1) && isIn(c2) && !shotList.contains(c2)) {
						c++;
						e++;
					}
					c1.column = cor.column - 1;
					c1.row = cor.row;
					c2.column = cor.column - 2;
					c2.row = cor.row;
					if (!shotList.contains(c1) && isIn(c2) && !shotList.contains(c2)) {
						c++;
						w++;
					}
					c1.column = cor.column;
					c1.row = cor.row + 1;
					c2.column = cor.column;
					c2.row = cor.row - 1;
					if (isIn(c1) && !shotList.contains(c1) && isIn(c2) && !shotList.contains(c2)) {
						c++;
						n++;
						s++;
					}
					c1.column = cor.column + 1;
					c1.row = cor.row;
					c2.column = cor.column - 1;
					c2.row = cor.row;
					if (isIn(c1) && !shotList.contains(c1) && isIn(c2) && !shotList.contains(c2)) {
						c++;
						e++;
						w++;
					}
				}
			}
		}
		return c;
	}

	// Distinguish if the coordinate is in the bound.
	private boolean isIn(Coordinate cdn) {
		return cdn.row >= 0 && cdn.row < numRow && cdn.column >= 0 && cdn.column < numColumn;
	}

	/*
	 * Order adjacent cells with their probabilities that not zero and add them into
	 * order list.
	 */
	private void orderTargeted(Coordinate C, int n, int e, int s, int w) {

		Coordinate nn = world.new Coordinate();
		nn.column = C.column;
		nn.row = C.row + 1;
		Cell N = new Cell(nn);
		Coordinate ss = world.new Coordinate();
		ss.column = C.column;
		ss.row = C.row - 1;
		Cell S = new Cell(ss);
		Coordinate ee = world.new Coordinate();
		ee.column = C.column + 1;
		ee.row = C.row;
		Cell E = new Cell(ee);
		Coordinate ww = world.new Coordinate();
		ww.column = C.column - 1;
		ww.row = C.row;
		Cell W = new Cell(ww);

		N.num = n;
		E.num = e;
		S.num = s;
		W.num = w;

		int t[] = new int[4];
		int ex;
		for (int i = 0; i < 4; i++) {
			if (w < s) {
				ex = s;
				s = w;
				w = ex;
			}
			if (s < e) {
				ex = e;
				e = s;
				s = ex;
			}
			if (e < n) {
				ex = n;
				n = e;
				e = ex;
			}
			t[3] = w;
			t[2] = s;
			t[1] = e;
			t[0] = n;

			if (t[i] != 0) {
				if (t[i] == N.num && !myOrder.contains(N.cor)) {
					myOrder.add(N.cor);
				} else if (t[i] == E.num && !myOrder.contains(E.cor)) {
					myOrder.add(E.cor);
				} else if (t[i] == S.num && !myOrder.contains(S.cor)) {
					myOrder.add(S.cor);
				} else if (t[i] == W.num && !myOrder.contains(W.cor)) {
					myOrder.add(W.cor);
				}
			}
		}
	}

	// store a cell with its coordinate and probability
	private class Cell {
		private Coordinate cor;
		private int num;

		Cell(Coordinate cor) {
			this.cor = cor;
			num = 0;
		}
	}
} // end of class ProbabilisticGuessPlayer
