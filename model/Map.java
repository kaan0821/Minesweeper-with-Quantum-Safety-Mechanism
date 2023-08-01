package model;

import java.util.Random;

/*
 * Quantum Safety Minesweeper Test Boards
 * This class holds the various boards to be played
 * 
 * All the boards are in 9x12 size
 *
*/

public class Map {
	
	//----------TEST Boards-----------
	private char[][] board0 = { 
		{'1', '1', '0', '0', '0', '0', '1', 't', '2', 't', '3', 't'},
		{'t', '1', '0', '0', '0', '0', '1', '2', '3', '3', 't', '2'},
		{'1', '1', '0', '0', '0', '0', '0', '2', 't', '3', '1', '1'},
		{'0', '0', '1', '1', '1', '0', '0', '2', 't', '2', '0', '0'},
		{'1', '1', '3', 't', '2', '0', '0', '1', '1', '1', '0', '0'},
		{'1', 't', '3', 't', '2', '0', '0', '0', '0', '0', '1', '1'},
		{'1', '1', '2', '1', '1', '0', '0', '0', '0', '0', '1', 't'},
		{'1', '1', '1', '1', '2', '1', '1', '0', '1', '1', '2', '1'},
		{'t', '1', '1', 't', '2', 't', '1', '0', '1', 't', '1', '0'}
	};

	private char[][] board1 = { 
		{'1', '1', '2', '1', '1', '0', '0', '0', '0', '1', '1', '1'},
		{'1', 't', '2', 't', '2', '1', '1', '0', '0', '1', 't', '1'},
		{'2', '2', '3', '2', '3', 't', '1', '0', '0', '1', '1', '1'},
		{'t', '1', '1', 't', '3', '2', '1', '0', '1', '1', '1', '0'},
		{'1', '1', '1', '2', 't', '1', '1', '1', '2', 't', '1', '0'},
		{'0', '0', '0', '1', '1', '1', '1', 't', '3', '2', '2', '0'},
		{'0', '0', '0', '0', '0', '1', '2', '2', '2', 't', '1', '0'},
		{'0', '0', '1', '1', '2', '2', 't', '2', '2', '1', '2', '1'},
		{'0', '0', '1', 't', '2', 't', '3', 't', '1', '0', '1', 't'}
	};

	private char[][] board2 = { 
		{'0', '0', '0', '0', '1', '1', '1', '0', '0', '0', '1', '1'},
		{'0', '0', '1', '1', '2', 't', '1', '0', '0', '0', '1', 't'},
		{'0', '0', '1', 't', '2', '2', '2', '1', '0', '0', '2', '2'},
		{'0', '0', '1', '1', '1', '1', 't', '2', '1', '0', '1', 't'},
		{'1', '1', '0', '0', '0', '1', '2', 't', '1', '0', '1', '1'},
		{'t', '2', '0', '0', '0', '0', '1', '1', '1', '1', '1', '1'},
		{'t', '2', '0', '0', '1', '2', '2', '2', '1', '3', 't', '2'},
		{'2', '2', '1', '1', '2', 't', 't', '2', 't', '3', 't', '2'},
		{'1', 't', '1', '1', 't', '3', '2', '2', '1', '2', '1', '1'}
	};

	private char[][] board3 = { 
		{'0', '1', '1', '1', '0', '0', '0', '0', '0', '0', '2', 't'},
		{'0', '1', 't', '1', '0', '0', '0', '0', '0', '0', '2', 't'},
		{'1', '2', '1', '2', '1', '1', '0', '0', '0', '0', '1', '1'},
		{'t', '2', '0', '2', 't', '2', '0', '0', '1', '1', '1', '0'},
		{'t', '2', '0', '2', 't', '3', '1', '1', '1', 't', '1', '0'},
		{'1', '1', '0', '1', '1', '2', 't', '2', '2', '2', '1', '0'},
		{'0', '0', '0', '1', '1', '2', '1', '2', 't', '2', '1', '1'},
		{'0', '0', '0', '2', 't', '2', '1', '2', '2', '2', 't', '2'},
		{'0', '0', '0', '2', 't', '2', '1', 't', '1', '1', '2', 't'}
	};

	private char[][] board4 = { 
		{'0', '0', '1', '1', '1', '0', '1', 't', '2', 't', '1', '0'},
		{'0', '0', '1', 't', '1', '1', '2', '2', '2', '2', '2', '1'},
		{'1', '1', '2', '2', '3', '3', 't', '1', '0', '1', 't', '1'},
		{'1', 't', '1', '2', 't', 't', '2', '1', '0', '1', '1', '1'},
		{'1', '1', '1', '2', 't', '4', '2', '0', '0', '1', '1', '1'},
		{'0', '0', '0', '2', '3', 't', '1', '0', '0', '1', 't', '1'},
		{'0', '0', '1', '2', 't', '2', '1', '0', '0', '1', '1', '1'},
		{'0', '0', '1', 't', '3', '2', '0', '1', '1', '1', '0', '0'},
		{'0', '0', '1', '2', 't', '1', '0', '1', 't', '1', '0', '0'}
	};

	private char[][] board5 = { 
		{'0', '0', '1', 't', '2', '1', '2', '1', '2', '2', 't', 't'},
		{'0', '0', '1', '1', '3', 't', '3', 't', '3', 't', '3', '2'},
		{'0', '1', '1', '1', '2', 't', '3', '2', 't', '3', '2', '1'},
		{'0', '1', 't', '2', '2', '2', '1', '1', '1', '2', 't', '1'},
		{'1', '2', '1', '2', 't', '2', '1', '0', '0', '1', '1', '1'},
		{'t', '1', '0', '1', '2', 't', '1', '1', '1', '1', '0', '0'},
		{'1', '1', '0', '0', '1', '1', '1', '1', 't', '2', '1', '1'},
		{'0', '0', '0', '0', '0', '0', '0', '1', '1', '2', 't', '1'},
		{'0', '0', '0', '0', '0', '0', '0', '0', '0', '1', '1', '1'}
	};

	private char[][] board6 = { 
		{'t', '1', '0', '0', '0', '1', '1', '2', '2', '3', 't', '1'},
		{'1', '1', '0', '1', '1', '2', 't', '2', 't', 't', '2', '1'},
		{'1', '1', '0', '1', 't', '2', '1', '2', '3', '3', '2', '0'},
		{'t', '1', '0', '1', '1', '1', '0', '0', '1', 't', '1', '0'},
		{'1', '1', '0', '0', '0', '0', '0', '0', '1', '1', '2', '1'},
		{'0', '0', '1', '1', '1', '0', '0', '0', '0', '1', '2', 't'},
		{'0', '0', '1', 't', '1', '0', '1', '1', '1', '1', 't', '3'},
		{'0', '1', '3', '3', '2', '0', '1', 't', '1', '1', '2', 't'},
		{'0', '1', 't', 't', '1', '0', '1', '1', '1', '0', '1', '1'}
	};

	private char[][] board7 = { 
		{'t', '2', '1', '1', 't', '1', '0', '1', '2', '2', '1', '0'},
		{'2', 't', '1', '1', '1', '1', '0', '1', 't', 't', '2', '1'},
		{'1', '1', '1', '0', '1', '1', '1', '1', '2', '2', '2', 't'},
		{'0', '0', '0', '0', '1', 't', '1', '0', '0', '0', '2', '2'},
		{'0', '0', '0', '0', '1', '1', '1', '0', '0', '0', '2', 't'},
		{'0', '0', '0', '1', '1', '1', '0', '0', '0', '0', '2', 't'},
		{'0', '0', '0', '2', 't', '3', '2', '1', '1', '0', '2', '2'},
		{'0', '0', '0', '2', 't', 't', '2', 't', '2', '1', '1', 't'},
		{'0', '0', '0', '1', '2', '2', '2', '2', 't', '1', '1', '1'}
	};

	private char[][] board8 = {
		{'0', '1', 't', '2', 't', '2', '1', '0', '0', '0', '0', '0'},
		{'0', '1', '2', '4', '4', 't', '1', '0', '0', '0', '0', '0'},
		{'0', '1', '2', 't', 't', '4', '3', '1', '0', '0', '0', '0'},
		{'0', '2', 't', '4', '3', 't', 't', '2', '0', '0', '0', '0'},
		{'0', '2', 't', '3', '2', '4', 't', '2', '0', '0', '0', '0'},
		{'0', '2', '3', '4', 't', '2', '1', '1', '0', '0', '0', '0'},
		{'0' ,'1' ,'t' ,'t' ,'4' ,'4' ,'2' ,'1' ,'0' ,'0' ,'0' ,'0'},
		{'0' ,'1' ,'2' ,'3' ,'t' ,'t' ,'t' ,'1' ,'0' ,'0' ,'0' ,'0'},
		{'0' ,'0' ,'0' ,'1' ,'2' ,'3' ,'2' ,'1' ,'0' ,'0' ,'0' ,'0'}
	};

	private char[][] board9 = {
		{'0', '2', 't', '2', '1', 't', '1', '0', '0', '0', '0', '0'},
		{'0', '2', 't', '2', '1', '2', '2', '1', '0', '0', '0', '0'},
		{'0', '2', '2', '3', '1', '3', 't', '2', '0', '0', '0', '0'},
		{'0', '1', 't', '2', 't', '4', 't', '2', '0', '0', '0', '0'},
		{'0', '1', '2', '3', '3', 't', '2', '1', '0', '0', '0', '0'},
		{'0', '0', '1', 't', '2', '1', '1', '0', '0', '0', '0', '0'},
		{'0', '0', '1', '1', '1', '0', '0', '0', '0', '0', '0', '0'},
		{'0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0'},
		{'0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0'}
	};

	// Generate random boards by placing random mines, then calculate numbers
	private char[][] randomBoard() {
		int rows = 9;
		int columns = 12;
		int mineCount = 30;
		char[][] board = new char[rows][columns];
		Random rand = new Random();
	
		// Initialize the board with '0'
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				board[i][j] = '0';
			}
		}
	
		// Randomly place the mines
		while (mineCount > 0) {
			int randRow = rand.nextInt(rows);
			int randCol = rand.nextInt(columns);
	
			// Avoid overwriting existing mines
			if (board[randRow][randCol] != 't') {
				board[randRow][randCol] = 't';
				mineCount--;
			}
		}
	
		// Calculate numbers for each cell based on neighbor mines
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				if (board[i][j] != 't') {
					int minesAround = countMines(board, i, j);
					board[i][j] = Character.forDigit(minesAround, 10);  // Convert int to char
				}
			}
		}
	
		return board;
	}
	
	// Count the neighbor mines for a given cell
	private int countMines(char[][] board, int row, int col) {
		int rows = board.length;
		int cols = board[0].length;
		int count = 0;
	
		// Check neighbor cells
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				int newRow = row + i;
				int newCol = col + j;
	
				// Boundary check
				if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols) {
					if (board[newRow][newCol] == 't') {
						count++;
					}
				}
			}
		}
	
		return count;
	}
	

	public char[][] getBoard(int index) {
		switch (index) {
			case 0:
				return board0;
            case 1:  
				return board1;
            case 2:  
				return board2;
            case 3:  
                return board3;
            case 4:  
                return board4;
            case 5:  
                return board5;
            case 6:  
                return board6;
            case 7:  
                return board7;
			case 8:  
                return board8;
			case 9:  
                return board9;	
			case 10:
				return randomBoard();
			default:
				return null;	
        }
	 }

}
