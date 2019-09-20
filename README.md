# Algorithms-and--Analysis-AS2
Assignment 2 Battleship


Marks: 14.5/15

# 1 Objectives

There are three key objectives for this project:

• Implement a Battleship game.

• Design and implement Battleship guessing algorithms.

• Have fun!

# 2 Background

Battleship is a classic two player game. The game consists of two players, and each player has a number of ships and a 2D grid. The players place their ships onto the grid, and these ships takes up a number of cells. Each player takes turn at guessing a cell to fire at in their opponent’s grid. If that cell contains part of a ship, the player gets a hit. If every part of a ship has been hit, then it is sunk and the owner of the ship will announce the name of the ship sunk (see ships section below for ships in the standard game). The aim of the game is to sink all of your opponent’s ships before they sink all of yours. For more details, see https://en.wikipedia.org/wiki/Battleship_(game).

Traditionally, Battleship is played between human players. In this assignment, your group will develop algorithms to automatically play Battleship, that uses a variety of the algorithmic paradigms we have covered in class. It will also give you a taste of artificial intelligence (AI), as algorithms is an important component of AI.

# 2.1 Ships

In the standard Battleship game, there are five ships available for each side. For this assignment, we have ships that are different from the standard Battleship game and have dimensions as follows: 

    Name Dimensions
    Patrol Craft 1 by 2 cells
    Cruiser 2 by 2 cells
    Submarine 1 by 3 cells
    Frigate 1 by 4 cells
    Aircraft Carrier 2 by 3 cells

# 3 Tasks

The project is broken up into a number of tasks to help you progress. Task A is to develop a random guessing algorithm as an initial attempt at a Battleship playing agent. Task B and C develops more sophisticated algorithms to play Battleship. For details on how each task will be assessed, please see the \Assessment" section.

To help you understand the tasks better, we will use the example illustrated in Figure 1, which is a Battleship game played on a 10 by 10 grid.

# Task A: Implement Random Guessing Player

In this task, your group will implement a random guessing player for Battleship, which can be considered as a type of brute force algorithm. Each turn, this type of algorithmic player will randomly select a cell it hasn’t tried before, fire upon that cell, and continue this process until all the opponent’s ships are sunk.

# Task B: Implement Greedy Guessing Player

In this task, your group will make two improvements to the random guessing player. First one is rather than randomly guess, we can utilise the fact that the ships are at least 2 cells long and use the partiy principle. See Figure 3a. As ships are at least of length 2, the player do not need to fire at every cell to ensure we eventually find the opponent’s ships. It just need to fire at every 2nd square (Figure 3a). Hence, when hunting for one of the opponent’s ships, it can now randonly select a cell from this checkboard type of pattern.

The second improvement is to implement more sophicated behaviour once we have a hit. We now divide the process into two parts: hunting mode, where the player is seeking opponent’s ships (for this task B type of player, they will use the parity guessing improvement), and targeting mode, where once there is a hit, the player greedily tries to sink the partially hit/damaged ship. For the targeting mode, once a cell register a hit, we know the rest of the ship must be in one of the four adjacent cells, as highlighted as oranged circles in Figure 3b. The player seeks to destroy the ship before moving on, hence will try to fire at those four possible cells first (assuming they haven’t been fired upon, if they have, then no need to fire at a cell twice). Once all possible targeting cells have been exhausted, the player can be sure to have sunk the ship(s) (can be more than one if ships are adjacent to each other) and it returns to the hunting mode until it finds the next ship.

# Task C: Implement Probabilistic Guessing Player

In this task, your group will implement a smarter type of player. This one is based on the transformand-conquer principle, where we do some preprocessing to improve our hunting and targeting strategies. 

When a ship is sunk, the opponent will indicate which ship of theirs have been sunk. We can make use of this fact to improve both the hunting and targeting mode. In the two previous type of players they assumed every cell is as likely to contain a ship. But this is unlikely to be true. For example, consider the frigate and a 10 by 10 grid. It can only be in two placement configurations if placed in top left corner (see Figure 4a), but in 8 different placement configurations if part of it occupies one of the centre cell, e.g., cell (4,4) (see Figure 4b). Hence, assuming our opponent randomly places ships (typically they don’t, but that is beyond this course, as we are going towards game theory and more advanced AI), it is more likely to find the frigate occupying one of the centre cells. This exercise can be repeated for all ships, and for each ship, we end up with a count of the number of ship configurations that can occupy that cell. The cell with the highest total count over all ships is the one most likely to contain a ship.

In hunt mode, this type of player will select from those cells yet to be fired upon, the one with the highest possible ship configuration count (if there are several, randomly select one). If there is a miss, then the count of that cell and neighbouring cells (because we missed, it means there isn’t any ship that can occupy that cell, so need to update its count and the neighbouring ones, as the count of neighbour ones may depend on a ship being upon to fit onto the fired upon cell). If hit, we go to targeting mode. In targeting more, the player makes use of the fact that there has been a hit to calculate which adjacent cell is the most likely to contain a ship (with the highest configuration count). Using the same counting method as the hunting mode, we can calculate the number of possible ship configurations that pass through the hit cell (remember previous misses, previously sunk ships and grid boundaries should be considered as obstacles and taken into account). When we get a miss or another hit, update the counts correspondingly and repeat at firing at an adjacent cell with the highest count. Once a ship is sunk, the counts of the whole grid must be updated to reflect this ship is no longer in play. When the player has sunk the ship(s), then it goes back to hunting mode.  

# Compiling and Executing

To compile the files, run the following command from the root directory (the directory that BattleshipMain.java is in):

    javac -cp .:samplePlayer.jar BattleshipMain.java

Note that for Windows machine, remember to replace ’:’ with ’;’ in the classpath.

To run the Battleship framework:
    
    java -cp .:samplePlayer.jar BattleshipMain [-v] [-l <game log file>] <game configurationfile> <ship location file 1> <ship location file 2> <player 1 type> <player 2 type> 

where

• -v: whether to visualise the game.

• game log file: name of the file to write the log of the game.

• game configuration file: name of the file that contains the configuration of the game.

• ship location file 1: name of file containing the locations of each ship of player 1.

• ship location file 2: name of file containing the locations of each ship of player 2.

• player 1 type: specifies which type of algorithmic player to use for the first player, one of [random | greedy | prob | sample]. random is the random guessing player, greedy is the greedy guessing player, prob is the probabilistic guessing player, bonus is the bonus task player and sample is a sample player we provided for you to initially play with.

• player 2 type: specifies which type of algorithmic player to use for the second player, one of
[random j greedy j prob j sample].

The jar file contains the sample player to get you going

# Ship location file

The ship location file specifies the location of the ships of each player. It is formated as follows:

    [ ship name ] [ row coordinates ] [ column coordinates ] [ primary direction the ships spans] [ secondary direction the ship spans ]

The values are separated by space.

Ship names are one of fPatrolCraft, Cruiser, Submarine, Frigate, AircraftCarrierg. Directions are one
of {N (North), S (South), E (East), W (West)}

An example ship location file is as follows:

    AircraftCarrier 1 1 E S
    Frigate 2 5 S E

For more detail read assignment2.pdf
