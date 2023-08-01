package Main;
import java.util.ArrayList;

import model.Agent;
import model.Map;

public class Pmain {

/**
* Gets input from the user, start the frontend and the backend agent.
* @param args the parameter received from the users
*/
	public static void main(String[] args) {
		
		boolean verbose=false; //prints the formulas for SAT if true
		if (args.length>1 && args[1].equals("verbose") ){
			verbose=true; //prints the formulas for SAT if true
		}

		// Read input from command line
		// Agent type
		System.out.println("-------------------------------------------\n");
		System.out.println("Agent plays Board" + args[0] + "\n");

		// Get a game board from Map class
		Map map = new Map();
        char[][] board = map.getBoard(Integer.parseInt(args[0]));
		printBoard(board);
		System.out.println("Start!");

		//Creating the game and the agent
		int row = board.length;
		int col = board[0].length;
		Agent agent = new Agent(row, col, board);

	}

/**
* This method prints the board in a decent way.
* @param board the board one wants to print
*/
	public static void printBoard(char[][] board) {
		System.out.println();
		// second line
        for (int i = 0; i<board.length; i++) {
            for (int j = 0; j<board[0].length; j++) {
                System.out.print(" "+ board[i][j]+" ");
            }
            System.out.println();
        }
		System.out.println();
	}

}
