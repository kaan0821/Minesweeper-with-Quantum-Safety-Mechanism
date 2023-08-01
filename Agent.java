package model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import guiDelegate.GuiDelegate;


import java.util.Iterator;

import org.logicng.formulas.Formula;
import org.logicng.formulas.FormulaFactory;
import org.logicng.io.parsers.ParserException;
import org.logicng.io.parsers.PropositionalParser;
import org.logicng.io.parsers.ParserException;
import org.logicng.solvers.MiniSat;
import org.logicng.solvers.SATSolver;
import org.logicng.datastructures.Tristate;

public class Agent implements AgentObserver {

    // The GUI
    private GuiDelegate delegate;
    //Board from the agent's view
    private char[][] board;
    // Number of mines in the game
    private int totalMine;
    //Stores whether each cell has been probed or not
    private boolean[][] uncovered;
    private boolean[][] userUncovered;
    //The God Mode Board
    private char[][] gameBoard;
    //Stores the steps taken by the agent in sequence
    private ArrayList<ArrayList<Integer>> path = new ArrayList<ArrayList<Integer>>();
    //A FIFO queue that that stores all the coords to realize looping on covered cells
    private Queue<ArrayList<Integer>> frontier = new LinkedList<ArrayList<Integer>>();
    //List for Undeducible cells
    private ArrayList<ArrayList<Integer>> undeducibles = new ArrayList<ArrayList<Integer>>();

/**
 * Constructor for Agent.
 * @param row The row of the board
 * @param col The column of the board
 * @param gameBoard The board the agent should be playing
*/
    public Agent(int row, int col, char[][] gameBoard) {

        //The God Mode game board
        this.gameBoard = gameBoard;
        this.delegate = new GuiDelegate(this.gameBoard);
        this.delegate.addObserver(this);
        this.totalMine = countTotalMine();
        //Agent's game board
        char[][] board = new char[row][col];
        this.uncovered = new boolean[row][col];
        //Initialize a brand new board
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                board[i][j] = "?".charAt(0);
            }
        }
        this.board = board;

        // initialize frontier list with all coordinates
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                ArrayList<Integer> temp = new ArrayList<Integer>();
                temp.add(i);
                temp.add(j);
                this.frontier.add(temp);
            }
        }

    }

/** --------------------------- Observer's Reaction Methods ------------------------------ */

/**
 * This method activates when the player makes a first move.
 * @param x The row coordinate of the clicked cell
 * @param y The column coordinate of the clicked cell
*/
    @Override
    public void firstClick(int x, int y) {
        dnfSatProbe(false, true, x, y);
        System.out.println("------- Agent Progress: --------");
        printSelf();
    }

/**
 * This method activates when the player makes an move.
 * @param user The list of uncovered cells from the user side
*/
    @Override
    public void onCellClicked(boolean[][] user, int x, int y) {
        System.out.println("Clicked!");
        this.userUncovered = user;
        if (!uncovered[x][y]) {
            dnfSatProbe(false, true, x, y);
            System.out.println("------- Agent Progress: --------");
            printSelf();
        }
    }

/**
 * This method activates whent he GUI player loses the game.
 * @return the recorded steps that agent took
*/
    @Override
    public ArrayList<ArrayList<Integer>> lost() {
        return path;
    }

/**
 * This method activates when the player clicks on a mine.
 * @param x The row coordinate of the clicked cell
 * @param y The column coordinate of the clicked cell
*/
    @Override
    public boolean mineClicked(int x, int y) {
        if (checkIfUndeducible(x, y)) {
            System.out.println("Undeducible Mine!!! Quantum Safety Activated...");
            if (ifFirstMove()) {
                reMapFirst(x,y);
            } else {
                reMap(x,y);
            }
            return true;
        }
        return false;
    }

/**
 * This method activates when the player resets the map.
*/
    @Override
    public void resetClicked() {

		Random rand = new Random();
        int mineCount = totalMine;
        int rows = this.board.length;
        int cols = this.board[0].length;
	
		// Reset the board to '0'
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
                this.gameBoard[i][j] = '0';
                uncovered[i][j] = false;
			}
		}

		// Randomly place the mines
		while (mineCount > 0) {
			int randRow = rand.nextInt(rows);
			int randCol = rand.nextInt(cols);
	
			// Avoid overwriting existing mines
			if (this.gameBoard[randRow][randCol] != 't') {
				this.gameBoard[randRow][randCol] = 't';
				mineCount--;
			}
		}
	
		// Calculate numbers for each cell based on neighbor mines
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (this.gameBoard[i][j] != 't') {
					int minesAround = countMines(i, j);
					this.gameBoard[i][j] = Character.forDigit(minesAround, 10);  // Convert int to char
				}
			}
		}
	
        // Re-initialize the agent's board and frontier
        for (int i = 0; i < this.board.length; i++) {
            for (int j = 0; j < this.board[0].length; j++) {
                ArrayList<Integer> tmp = new ArrayList<Integer>();
                tmp.add(i);
                tmp.add(j);
                this.frontier.add(tmp);
                this.board[i][j] = "?".charAt(0);
            }
        }
        // Clear all undeducibles
        undeducibles.removeAll(undeducibles);
        path.removeAll(path);
        System.out.println("--------- Reset Clicked! New Map ----------");
    }

/** --------------------------- Map Reconfiguring ------------------------------ */

/**
 * This method reconstucts the map while preserving the user's current view:
 * Construct a new map, moving the mine away from (x,y);
 * Step 1. Replant mines for outter uncovered cells user don't feel difference;
 * Step 2. Rearrange the entire map for the everything else, replant mines and regenerate numbers
 * @param x The row coordinate of the clicked mine to avoid
 * @param y The column coordinate of the clicked mine to avoid
 */
    public void reMap(int x, int y) {
        // Reset uncovered 
		for (int i = 0; i < this.board.length; i++) {
			for (int j = 0; j < this.board[0].length; j++) {
                uncovered[i][j] = userUncovered[i][j];
			}
		}

        int outterMineCount = 0;
        // Scan and store all the outter cells (uncovered cells that still have neighbors)
        ArrayList<ArrayList<Integer>> outterCells = new ArrayList<ArrayList<Integer>>();
        for (int i = 0; i<this.board.length; i++) {
            for (int j = 0; j<this.board[0].length; j++) {
                if (uncovered[i][j] && getNeighbors(i, j).size()>0) {
                    ArrayList<Integer> temp = new ArrayList<Integer>();
                    temp.add(i);
                    temp.add(j);
                    outterCells.add(temp);
                    outterMineCount += Character.getNumericValue(gameBoard[i][j]);;
                }
            }
        }
        outterCells = sortOutter(outterCells);
        // Have a copy of the God Mode board
        char[][] temp2 = new char[this.gameBoard.length][this.gameBoard[0].length];
        for (int i = 0; i < this.gameBoard.length; i++) {
            for (int j = 0; j < this.gameBoard[0].length; j++) {
                temp2[i][j] = this.gameBoard[i][j];
            }
        }

        // First reset the rest of the board to '0'
		for (int i = 0; i < this.board.length; i++) {
			for (int j = 0; j < this.board[0].length; j++) {
                if (!userUncovered[i][j]) {
                    this.gameBoard[i][j] = '0';
                }
			}
		}

        // Re-mapping algorithm starting with the first outter cell
        placeMines(temp2, 0, outterCells, outterMineCount,x,y);

        // Re-arrange the rest of the baord
        Random rand = new Random();
        int mineCount = totalMine - outterMineCount;

        // Randomly place the mines
        while (mineCount > 0) {
            int randRow = rand.nextInt(this.board.length);
            int randCol = rand.nextInt(this.board[0].length);
    
            // Avoid overwriting existing mines and re-arrange covered cells
            if (this.gameBoard[randRow][randCol] != 't' && !uncovered[randRow][randCol] && !(randRow == x && randCol == y) && ifPlantSafe(randRow, randCol)) {
                this.gameBoard[randRow][randCol] = 't';
                mineCount--;
            }
        }

        // Calculate numbers for each cell based on neighbor mines
		for (int i = 0; i < this.board.length; i++) {
			for (int j = 0; j < this.board[0].length; j++) {
				if (!uncovered[i][j] && this.gameBoard[i][j] != 't') {
					int minesAround = countMines(i, j);
					this.gameBoard[i][j] = Character.forDigit(minesAround, 10);  // Convert int to char
				}
			}
		}
        //Re-initialize the agent's board and frontier
        for (int i = 0; i < this.board.length; i++) {
            for (int j = 0; j < this.board[0].length; j++) {
                if (!uncovered[i][j]) {
                    if (this.board[i][j]!="?".charAt(0)) {
                        ArrayList<Integer> tmp = new ArrayList<Integer>();
                        tmp.add(i);
                        tmp.add(j);
                        this.frontier.add(tmp);
                        this.board[i][j] = "?".charAt(0);
                    }
                }
            }
        }
        // Clear all undeducibles and recorded steps taken
        undeducibles.removeAll(undeducibles);
        path.removeAll(path);
    }

/**
* This method is for placing the mines around all outter cells.
* @param temp2 The God Mode board
* @param outerCellIndex The current recursion index of running through outterCells
* @param outerCells The list of outer cells
* @param totalMines The total number of mines for this process
* @param x The row to avoid
* @param y The column to avoid
* @return Whether a solution is found or not
*/
    public boolean placeMines(char[][] temp2, int outerCellIndex, ArrayList<ArrayList<Integer>> outerCells, int totalMines, int x, int y) {
        // Base case: if all outer cells have been handled
        if (outerCellIndex == outerCells.size()) {
            // Found a solution
            if (totalMines == 0) {
                return true; 
            } else {
                return false;
            }
        }
    
        int row = outerCells.get(outerCellIndex).get(0);
        int col = outerCells.get(outerCellIndex).get(1);
        int numMines = Character.getNumericValue(temp2[row][col]);
        ArrayList<ArrayList<Integer>> avail = getNeighbors(row, col);
        ArrayList<ArrayList<ArrayList<Integer>>> possiblePlacements = generateAllPossiblePlacements(avail, numMines);

        // Try all possible ways to place mines around the current cell
        for (ArrayList<ArrayList<Integer>> eachPlacement : possiblePlacements) {
            // Record mines planted in previous recursions in case removing by accident
            ArrayList<ArrayList<Integer>> oldMine = new ArrayList<ArrayList<Integer>>();
            for (ArrayList<Integer> e : eachPlacement) {
                if (this.gameBoard[e.get(0)][e.get(1)]=='t') {
                    oldMine.add(e);
                }
            }
            if (isValidPlacement(eachPlacement,x,y) && totalMines >= eachPlacement.size()) {
                System.out.println("row: "+ row + ", col: " + col);
                for (ArrayList<Integer> e : eachPlacement) {
                    System.out.println(e.get(0)+","+e.get(1));
                }
                // Place mines and recursively call placeMines for the next cell
                for (ArrayList<Integer> each : eachPlacement) {
                    this.gameBoard[each.get(0)][each.get(1)] = 't';
                }
                // printCanonSelf();
                if (placeMines(temp2, outerCellIndex + 1, outerCells, totalMines - eachPlacement.size(),x,y)) {
                    return true; // Found a solution
                }
                System.out.println("Removed row: "+row+", col: "+col);
                // Backtrack: remove the new mines placed in this step
                for (ArrayList<Integer> each : eachPlacement) {
                    if (!oldMine.contains(each)) {
                        this.gameBoard[each.get(0)][each.get(1)] = '?';
                    }
                }
            }
        }
    
        // No solution found for this cell with the current state of the board
        return false;
    }

/**
* This method checks if a particular placemenrt is valid or not.
* @param placement The one possible way of planting mines around a cell
* @param x The row to avoid
* @param y The column to avoid
* @return Whether a particular placement is valid
*/
    public boolean isValidPlacement(ArrayList<ArrayList<Integer>> placement,int x, int y) {

        // Record mine planted in previous recursions, in case removing by accident
        ArrayList<ArrayList<Integer>> oldMine = new ArrayList<ArrayList<Integer>>();
        for (ArrayList<Integer> each : placement) {
            if (this.gameBoard[each.get(0)][each.get(1)] == 't') {
                oldMine.add(each);
            }
        }

        for (ArrayList<Integer> each : placement) {

            if (this.gameBoard[each.get(0)][each.get(1)] == 't') {
                continue;
            }
            // If it's the mis-clicked mine, invalid placement
            if (each.get(0) == x && each.get(1) == y) {
                return false;
            }

            // If safe, plant for the moment
            if (ifPlantSafe(each.get(0), each.get(1))) {
                this.gameBoard[each.get(0)][each.get(1)] = 't';
            } else {
                // Clear all previous allocation and return invalid placement
                for (ArrayList<Integer> each2 : placement) {
                    if (!oldMine.contains(each2)) {
                        this.gameBoard[each2.get(0)][each2.get(1)] = '?';
                    }
                }
                return false;
            }

        }
        return true;
    }

/**
* This method returns all possible placements given the neighbors and the number of mines.
* @param avail The avaliable neighbors
* @param numMines The number of mines nearby
* @return The list of all possible ways of planting mines around a cell
*/
    public ArrayList<ArrayList<ArrayList<Integer>>> generateAllPossiblePlacements(ArrayList<ArrayList<Integer>> avail, int numMines) {
        
        ArrayList<ArrayList<ArrayList<Integer>>> result = new ArrayList<ArrayList<ArrayList<Integer>>>();
    
        if (numMines == 0) {
            result.add(new ArrayList<>());  // add an empty placement
            return result;
        }
    
        if (avail.size() < numMines) {
            return result;  // no possible placements
        }
        for (int i = 0; i <= avail.size() - numMines; i++) {
            ArrayList<Integer> location = avail.get(i);
    
            // Generate all possible placements for the rest of the locations and mines
           ArrayList<ArrayList<ArrayList<Integer>>> placementsForRest = generateAllPossiblePlacements(new ArrayList<>(avail.subList(i+1, avail.size())), numMines - 1);
    
            // Add the current location to each placement and add it to the result
            for (ArrayList<ArrayList<Integer>> placement : placementsForRest) {
                ArrayList<ArrayList<Integer>> newPlacement = new ArrayList<ArrayList<Integer>>();
                newPlacement.add(location);
                newPlacement.addAll(placement);
                result.add(newPlacement);
            }
        }
    
        return result;
    }
    
/**
 * This method reconstucts the map while avoiding the first clicked mine:
 * @param x The row coordinate of the clicked mine to avoid
 * @param y The column coordinate of the clicked mine to avoid
 */
    public void reMapFirst(int x, int y) {

        // Initialize the board with '0'
		for (int i = 0; i < this.board.length; i++) {
			for (int j = 0; j < this.board[0].length; j++) {
				this.gameBoard[i][j] = '0';
			}
		}
        int mineCount = totalMine;
        Random rand = new Random();
		// Randomly place the mines
		while (mineCount > 0) {
			int randRow = rand.nextInt(this.board.length);
			int randCol = rand.nextInt(this.board[0].length);
	
			// Avoid overwriting existing mines & clicked mine
			if (this.gameBoard[randRow][randCol] != 't' && !(randRow == x & randCol == y)) {
				this.gameBoard[randRow][randCol] = 't';
				mineCount--;
			}
		}
	
		// Calculate numbers for each cell based on neighbor mines
		for (int i = 0; i < this.board.length; i++) {
			for (int j = 0; j < this.board[0].length; j++) {
				if (this.gameBoard[i][j] != 't') {
					int minesAround = countMines(i, j);
					this.gameBoard[i][j] = Character.forDigit(minesAround, 10);  // Convert int to char
				}
			}
		}
	
        //Re-initialize the agent's board and frontier
        for (int i = 0; i < this.board.length; i++) {
            for (int j = 0; j < this.board[0].length; j++) {
                if (!uncovered[i][j]) {
                    if (this.board[i][j]!="?".charAt(0)) {
                        ArrayList<Integer> tmp = new ArrayList<Integer>();
                        tmp.add(i);
                        tmp.add(j);
                        this.frontier.add(tmp);
                        this.board[i][j] = "?".charAt(0);
                    }
                }
            }
        }
        // Clear all undeducibles and recorded steps taken
        undeducibles.removeAll(undeducibles);
        path.removeAll(path);
    }

    /**
* This method is for checking if planting a mine here will affect the uncovered board.
* @param neighbors The list of neighbors
* @return The indices of the cells ranked by number of uncovered neighbors
*/
    public ArrayList<Integer> popularCellRank(ArrayList<ArrayList<Integer>> neighbors) {
        ArrayList<Integer> rank = new ArrayList<Integer>();
        for (ArrayList<Integer> each : neighbors) {
            int count = countUncovered(each.get(0), each.get(1));
            rank.add(count);
        }
        ArrayList<Integer> indices = new ArrayList<Integer>();
        for (int i = 0; i < rank.size(); i++) {
            indices.add(rank.indexOf(Collections.max(rank)));
            rank.set(rank.indexOf(Collections.max(rank)), 0);
        }
        return indices;
    }

/**
* This method is for checking if planting a mine here will affect the uncovered board.
* @param row The row coordinate of the cell
* @param col The column coordinate of the cell
* @return Whether this cell could be safely planted a mine
*/
    public boolean ifPlantSafe(int row, int col) {
        // The potentially affected neighbors
        ArrayList<ArrayList<Integer>> neighbors = new ArrayList<ArrayList<Integer>>();
        for (int i = row-1; i <= row+1; i++) {
            for (int j = col-1; j <= col+1; j++) {
                if (i >= 0 && i < board.length && j >= 0 && j < board[0].length && uncovered[i][j]) {
                    ArrayList<Integer> temp = new ArrayList<Integer>();
                    temp.add(i);
                    temp.add(j);
                    neighbors.add(temp);
                }
            }
        }
        
        // If planting this mine contaminates any of the neighbors, false
        for (ArrayList<Integer> each : neighbors) {
            int eachRow = each.get(0);
            int eachCol = each.get(1);
            if (countMines(eachRow, eachCol)+1 > Character.getNumericValue(this.gameBoard[eachRow][eachCol])) {
                return false;
            }
        }
        return true;
    }

/**
* This method is for sorting the list of outterCells based on their numeric value.
* @param outter The list of outter cells
* @return The sorted list of outter cells
*/
    public ArrayList<ArrayList<Integer>> sortOutter (ArrayList<ArrayList<Integer>> outter) {
        // Define your Comparator here
        Comparator<ArrayList<Integer>> comparator = new Comparator<ArrayList<Integer>>() {
            @Override
            public int compare(ArrayList<Integer> coord1, ArrayList<Integer> coord2) {
                int row1 = coord1.get(0);
                int col1 = coord1.get(1);
                int row2 = coord2.get(0);
                int col2 = coord2.get(1);
                
                int value1 = Character.getNumericValue(gameBoard[row1][col1]);
                int value2 = Character.getNumericValue(gameBoard[row2][col2]);
                
                return Integer.compare(value1, value2);
            }
        };

        // Use Collections.sort to sort your ArrayList
        Collections.sort(outter, comparator);
        // Return the sorted ArrayList
        return outter;
    }
/** --------------------------- Agent Probing ------------------------------ */

/**
* This method is for SAT probing using DNF.
* This snippet is taken from my previous work and changes were made to tailor to this project
* @param verbose The boolean that controls whether verbose mode print or not
*/
    public boolean dnfSatProbe(boolean verbose,boolean newMove,int p,int q) {
        if (verbose) {printSelf();}
        checkFrontier();
        //Probing the given two cells first
        if (newMove) {
            initialProbe(p,q,false);
            System.out.println("New Move: "+p+","+q);
        }
        //This list stores cells that nothing can be done with them
        ArrayList<ArrayList<Integer>> trace = new ArrayList<ArrayList<Integer>>();
        //Initialize a count to check for stalemate situation
        int count = 0;
         //Probing starts
         while (!frontier.isEmpty()) {
            //Each iteration, first check if already won
            if (checkActualWin()) {
                undeducibles.removeAll(undeducibles);
                System.out.println("**** Agent Completed the Game!! No Undeducibles!! ****");
                return true;
            }
            //For each iteration, consturct the current knowledge base of the world
            String KB = makeKB();
            ArrayList<Integer> coord = frontier.poll();

            //Check for stuck situation, call to construct list of un-deducible cells
            if (trace.contains(coord)) {
                if (count == trace.size()) {
                    System.out.println("Agent Stuck!");
                    buildUndeducibles();
                    return false;
                }
            }
            int x = coord.get(0);
            int y = coord.get(1);
            //If it's a covered cell
            if (uncovered[x][y] == false) {
                String Dclause;
                String Pclause;
                //This happens only if all the outter cells are mines
                if (KB.length() == 0) {
                    Dclause = "(~D"+ Integer.toString(x)+Integer.toString(y)+")";
                    Pclause = "(D"+ Integer.toString(x)+Integer.toString(y)+")";
                } else {
                    Dclause = "&(~D"+ Integer.toString(x)+Integer.toString(y)+")";
                    Pclause = "&(D"+ Integer.toString(x)+Integer.toString(y)+")";
                }
                //Check the cell in question is a danger with logicng
                boolean ifDanger = logicng(KB+Dclause);
                //Check the cell in question is safe with logicng
                boolean ifProbe = logicng(KB+Pclause);
                //If safe, probe
                if (ifProbe) {
                    uncovered[x][y] = true;
                    // Record this step, 1 for probed
                    ArrayList<Integer> tempp = new ArrayList<Integer>();
                    tempp.add(x);
                    tempp.add(y);
                    tempp.add(1);
                    path.add(tempp);
                    if (this.gameBoard[x][y] == '0') {
                        this.board[x][y] = this.gameBoard[x][y];
                        probeNeighbors(x, y);
                        //Remove from trace and reset count
                        if (trace.contains(coord)) {
                            trace.remove(coord);
                            count = 0;
                        }
                    } else {
                        board[x][y] = this.gameBoard[x][y];
                        if (trace.contains(coord)) {
                            trace.remove(coord);
                            count = 0;
                        }
                    }
                    if (verbose) {printSelf();}
            //If danger, mark it as danger
                } else if (ifDanger) {
                    uncovered[x][y] = true;
                    // Record this step, 0 for flagged
                    ArrayList<Integer> tempp = new ArrayList<Integer>();
                    tempp.add(x);
                    tempp.add(y);
                    tempp.add(0);
                    path.add(tempp);
                    board[x][y] = "*".charAt(0);
                    //Remove from trace and reset count
                    if (trace.contains(coord)) {
                        trace.remove(coord);
                        count = 0;
                    }
                    if (verbose) {printSelf();}
            //No candidates, nothing can be done, add to back of the queue
            //Add to trace if not yet, increment count if already in trace
                } else {
                    frontier.add(coord);
                    if (!trace.contains(coord)) {
                        trace.add(coord);
                    } else {
                        count ++;
                    }
                }
            }
        }
        undeducibles.removeAll(undeducibles);
        System.out.println("**** Agent Completed the Game!! No Undeducibles!! ****");
        return true;
    }

/**
 * This method constructs the list for undeducible cells.
 */
    public void buildUndeducibles() {
        undeducibles.removeAll(undeducibles);
        for (int i = 0; i < this.board.length;i++) {
            for (int j = 0; j<this.board[0].length;j++) {
                if (this.board[i][j] == "?".charAt(0)) {
                    ArrayList<Integer> temp = new ArrayList<Integer>();
                    temp.add(i);
                    temp.add(j);
                    undeducibles.add(temp);
                }
            }
        }
        System.out.println("Undeducible size: " + undeducibles.size());
    }

/**
 * This method helps complete the frontier for missing cells.
 */
    public void checkFrontier() {
        for (int i = 0; i < uncovered.length;i++) {
            for (int j = 0; j<uncovered[0].length;j++) {
                if (!uncovered[i][j] && !ifInFrontier(i,j)) {
                    ArrayList<Integer> tmp = new ArrayList<Integer>();
                    tmp.add(i);
                    tmp.add(j);
                    frontier.add(tmp);
                }
            }
        }
    }

/**
 * This method constructs the list for undeducible cells.
 * @param x The x coordinate of the cell
 * @param y The y coordinate of the cell
 * @return whether the cell is in the frontier
 */
    public boolean ifInFrontier(int x, int y) {
        for (ArrayList<Integer> each : frontier) {
            if (each.get(0) == x && each.get(1) == y) {
                return true;
            }
        }
        return false;
    }

/**
 * This method checks if the move is the first move.
 * @return whether a move is the first move or not
*/
    public boolean ifFirstMove() {
        int count = 0;
        for (int i = 0; i < this.board.length;i++) {
            for (int j = 0; j<this.board[0].length;j++) {
                if (uncovered[i][j]) {
                    count++;
                }
            }
        }
        if (count == 0) {
            return true;
        } else {
            return false;
        }
    }

/**
 * This method checks if a cell is undeducible or not.
 * @param row The row coordinate of the cell
 * @param col The row coordinate of the cell
 * @return whether or not a cell is logically deducible
*/
    public boolean checkIfUndeducible(int row, int col) {
        if (ifFirstMove()) {
            return true;
        }
        for (int i = 0; i < undeducibles.size(); i++) {
            if (undeducibles.get(i).get(0) == row && undeducibles.get(i).get(1) == col) {
                return true;
            }
        }
        return false;
    }

/**
 * This method constructs the knowledge base query in DNF.
 * @return The knowledge base string
*/
    public String makeKB() {
        //Knowledge Base
        StringBuilder KB = new StringBuilder();

        //Iterate over all uncovered numbered cells
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (uncovered[i][j] && board[i][j] != "0".charAt(0) && board[i][j]!="*".charAt(0)) {
                    //Get the covered neighbors of the current cell
                    ArrayList<ArrayList<Integer>> neighbors = getNeighbors(i, j);
                    if (neighbors.size()>0) {
                        //A list that stores the clauses for the current cell
                        ArrayList<String> clauses = new ArrayList<String>();
                        clauses.add(makeDNFClause(neighbors,Character.getNumericValue(board[i][j])-countDanger(i, j)));
                        for (String each : clauses) {
                            KB.append(each +"&");
                        }
                    }
                    
                }
            }
        }
        //Stripe the ending &
        if (KB.length() != 0) {
            KB.deleteCharAt(KB.length()-1);
        }
        return KB.toString();

    }

/**
 * This method constructs each DNF clause.
 * @param neighbors The list of all neighbor coordinates
 * @param count How many dangers are in the neighbors
 * @return The DNF clause
*/
    public String makeDNFClause(ArrayList<ArrayList<Integer>> neighbors, int count) {
        
        StringBuilder clause = new StringBuilder();
        if (neighbors.size() != 1) {
            clause.append("(");
        }
        int n = neighbors.size();
        String[] literals = new String[n];
    
        //Construct the literal representation for each neighbor
        for (int i = 0; i < n; i++) {
            ArrayList<Integer> coords = neighbors.get(i);
            int row = coords.get(0);
            int col = coords.get(1);
            literals[i] = "D" + row + col;
        }
    
        //Generate the clause
        //The loop iterates over all possible subsets of the neighbors
        //i has n bits and each bit indicates whether the literal is included or not. 2^n combinations
        for (int i = 0; i < (1 << n); i++) {
            //'1' in the i indicates Dij, 0 indicates ~Dij
            int setBits = Integer.bitCount(i);
            //Counts the '1' in the i, if the 'count' number of '1' is included, make the clause
            if (setBits == count) {
                clause.append("(");
                for (int j = 0; j < n; j++) {
                    if (((i >> j) & 1) == 1) {
                        clause.append(literals[j]).append("&");
                    } else {
                        clause.append("~").append(literals[j]).append("&");
                    }
                }
                clause.setCharAt(clause.length() - 1, ')');
                clause.append("|");
            }
        }
    
        //Remove the trailing char and close the clause
        if (neighbors.size() == 1) {
            clause.setLength(clause.length() - 1);
        } else {
            clause.setCharAt(clause.length() - 1, ')');
        }
        return clause.toString();
    }

/**
 * This method uses the logicng library for SAT solving.
 * @param query The logical query for SAT solving
 * @return whether the query is unsatisfiable or not
*/
    public boolean logicng(String query) {
        try{
            FormulaFactory f = new FormulaFactory();
            PropositionalParser p = new PropositionalParser(f);

            Formula formula = p.parse(query);
            
            SATSolver miniSat = MiniSat.miniSat(f);
            miniSat.add(formula);
            Tristate result = miniSat.sat();
            if (result == Tristate.FALSE) {
                return true;
            } else {
                return false;
            }
      
          } catch (ParserException e) {
            System.out.println("Error");
            return false;
          }
    }

/**
 * This method chekcs if all cells have been uncovered or marked, check if board same.
 * @return whether the agent has won the game or not
*/
    public boolean checkActualWin() {
        for (int i = 0; i < uncovered.length;i++) {
            for (int j = 0; j<uncovered[0].length;j++) {
                if (uncovered[i][j] == false) {
                    return false;
                }
            }
        }
        return true;
    }


/**
 * This method counts the number of marked neighbor dangers.
 * @param x The row coordinate of the cell
 * @param y The column coordinate of the cell
 * @return number of marked neighbor dangers
*/
    public int countDanger(int x, int y) {
        int count = 0;
        for (int i = x-1; i <= x+1; i++) {
            for (int j = y-1; j <= y+1; j++) {
                if (i >= 0 && i < board.length && j >= 0 && j < board[0].length) {
                    if (board[i][j] == "*".charAt(0)) {
                        count ++;
                    }
                    
                }
            }
        }
        return count;
    }

/**
 * This method counts the number of covered cells.
 * @param x The row coordinate of the cell
 * @param y The column coordinate of the cell
 * @return number of covered cells
*/  
    public int countCovered(int x, int y) {
        int count = 0;
        for (int i = x-1; i <= x+1; i++) {
            for (int j = y-1; j <= y+1; j++) {
                if (i >= 0 && i < board.length && j >= 0 && j < board[0].length) {
                    if (uncovered[i][j] == false) {
                        count ++;
                    }
                    
                }
            }
        }
        return count;
    }


/** ---------------------------   General Utility ------------------------------*/

/**
 * This method gets all the covered neighbors' coordinates.
 * @param x The row coordinate of the cell
 * @param y The column coordinate of the cell
 * @return the list of all covered neighbors
*/
    public ArrayList<ArrayList<Integer>> getNeighbors(int x, int y) {
        //Stores the eventual list of covered neighbor coordinates
        ArrayList<ArrayList<Integer>> neighbors = new ArrayList<ArrayList<Integer>>();
        for (int i = x-1; i <= x+1; i++) {
            for (int j = y-1; j <= y+1; j++) {
                if (i >= 0 && i < board.length && j >= 0 && j < board[0].length && !uncovered[i][j]) {
                    ArrayList<Integer> temp = new ArrayList<Integer>();
                    temp.add(i);
                    temp.add(j);
                    neighbors.add(temp);
                }
            }
        }
        return neighbors;
    }


/**
 * This method probes all the neighbors if 0 is discovered.
 * @param x The row coordinate of the cell
 * @param y The column coordinate of the cell
*/
    public void probeNeighbors(int x, int y) {
        for (int i = x-1; i <= x+1; i++) {
            for (int j = y-1; j <= y+1; j++) {
                if (i >= 0 && i < board.length && j >= 0 && j < board[0].length && !uncovered[i][j]) {
                    char cellValue = this.gameBoard[i][j];
                    uncovered[i][j] = true;
                    board[i][j] = cellValue;
                    if (cellValue == '0') {
                        probeNeighbors(i, j);
                    }
                }
            }
        }
    }

/**
 * This method gets the current agent's board.
 * @return the current board from the agent's view
*/
    public char[][] getBoard() {
        return this.board;
    }

/**
 * This method probes the new move cell.
 * @param row The row coordinate of the probed cell
 * @param col The column coordinate of the probed cell
 * @param verbose Control whether to print the current view or not
*/
    public void initialProbe(int row, int col, boolean verbose) {
        //Probing the same cell as the user
        char val = this.gameBoard[row][col];
        uncovered[row][col] = true;
        //Probe the first cell, if 0 probe all 0's
        if (val == '0') {
            this.board[row][col] = val;
            probeNeighbors(row, col);
            if (verbose) {printSelf();}
        //If not 0, simply probe
        } else {
            this.board[row][col] = val;
            if (verbose) {printSelf();}
        }
    }

/**
 * This method counts the unocvered neighbors for a given cell.
 * @param row The row coordinate of the cell
 * @param col The column coordinate of the cell
 * @return the number of uncovered cells
*/
    public int countUncovered(int row, int col) {
        int rows = this.gameBoard.length;
		int cols = this.gameBoard[0].length;
		int count = 0;
	
		// Check neighbor cells
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				int newRow = row + i;
				int newCol = col + j;
	
				// Boundary check
				if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols) {
					if (uncovered[newRow][newCol]) {
						count++;
					}
				}
			}
		}
		return count;

    }
    
/**
 * This method counts the neighbor mines for a given cell.
 * @param row The row coordinate of the cell
 * @param col The column coordinate of the cell
 * @return the number of neighbor mines
*/
	public int countMines(int row, int col) {
		int rows = this.gameBoard.length;
		int cols = this.gameBoard[0].length;
		int count = 0;
	
		// Check neighbor cells
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				int newRow = row + i;
				int newCol = col + j;
	
				// Boundary check
				if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols) {
					if (this.gameBoard[newRow][newCol] == 't') {
						count++;
					}
				}
			}
		}
	
		return count;
	}

/**
 * This method prints the current board from agent's view.
*/
	public void printSelf() {
		System.out.println();
		// second line
        System.out.print("   ");
        for (int a = 0; a < this.board[0].length; a++) {
            System.out.print(" " + a + " ");
        }
        System.out.println();
        for (int i = 0; i<this.board.length; i++) {
            System.out.print(" "+i+" ");
            for (int j = 0; j<this.board[0].length; j++) {
                System.out.print(" "+ this.board[i][j]+" ");
            }
            System.out.println();
        }
        System.out.println();
	}

/**
 * This method prints the God Mode board.
*/
    public void printCanonSelf() {
        System.out.println();
        // second line
        System.out.print("   ");
        for (int a = 0; a < this.board[0].length; a++) {
            System.out.print(" " + a + " ");
        }
        System.out.println();
        for (int i = 0; i<this.board.length; i++) {
            System.out.print(" "+i+" ");
            for (int j = 0; j<this.board[0].length; j++) {
                System.out.print(" "+ this.gameBoard[i][j]+" ");
            }
            System.out.println();
        }
        System.out.println();
    }

/**
 * This method counts the total number of mines in the game.
 * @return total number of mines
*/ 
    public int countTotalMine() {
        int count = 0;
        for (int i = 0; i<this.gameBoard.length; i++) {
            for (int j = 0; j<this.gameBoard[0].length; j++) {
                if (this.gameBoard[i][j] == 't') {
                    count ++;
                }
            }
        }
        return count;
    }
}
