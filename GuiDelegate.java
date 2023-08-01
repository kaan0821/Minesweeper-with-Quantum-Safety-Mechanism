package guiDelegate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.event.*;

import model.AgentObserver;

public class GuiDelegate {

    private static final int FRAME_HEIGHT = 900;
    private static final int FRAME_WIDTH = 1200;

    private JFrame mainFrame;
    private JButton[][] buttons;
    private int[][] mines;
    private int rows;
    private int columns;
    private char[][] board;
    private boolean[][] uncovered;

/**
 * Constructor for GuiDelegate.
 * @param board The board the players is playing on
*/
    public GuiDelegate(char[][] board) {
        this.board = board;
        this.uncovered = new boolean[this.board.length][this.board[0].length];
        this.rows = this.board.length;
        this.columns = this.board[0].length;
        initialize();
    }

    // Create communication to the agent
    private ArrayList<AgentObserver> observers = new ArrayList<>();

/**
 * Adding an observer to the delegate.
 * @param observer The observer being added
*/
    public void addObserver(AgentObserver observer) {
        observers.add(observer);
    }

/**
 * Activate the cell clicked method on agent's end
 * @param x The row coordinate of the mine clicked
 * @param y The column coordinate of the mine clicked
*/
    public void notifyObservers1(int x, int y) {
        for (AgentObserver observer : observers) {
            observer.onCellClicked(uncovered,x,y);
        }
    }

/**
 * Activate the mine clicked method on agent's end
 * @param x The row coordinate of the mine clicked
 * @param y The column coordinate of the mine clicked
*/
    public boolean notifyObservers2(int x, int y) {
        for (AgentObserver observer : observers) {
            if (observer.mineClicked(x, y)) {
                return true;
            }
        }
        return false;
    }

/**
 * Activate the reset clicked method on agent's end
*/
    public void notifyObservers3() {
        for (AgentObserver observer : observers) {
            observer.resetClicked();
        }
    }

/**
 * Activate the reset clicked method on agent's end
 * @param verbose 
 * @param x The row coordinate of the mine clicked
 * @param y The column coordinate of the mine clicked
*/
    public void notifyObservers4(int x, int y) {
        for (AgentObserver observer : observers) {
            observer.firstClick(x, y);
        }
    }

/**
 * Activate the lost method on agent's end
 * @return The list of recorded steps taken
*/
    public ArrayList<ArrayList<Integer>> notifyObservers5() {
        ArrayList<ArrayList<Integer>> steps = new ArrayList<ArrayList<Integer>>();
        for (AgentObserver observer : observers) {
            steps = observer.lost();
        }
        return steps;
    }

/**
 * Call this method when the player clicks a cell.
 * @param x The row coordinate of the mine clicked
 * @param y The column coordinate of the mine clicked
*/
    public void cellClicked(int x, int y) {
       notifyObservers1(x,y);
    }
    
/**
 * Call this method when the player clicks a mine.
 * @param x The row coordinate of the mine clicked
 * @param y The column coordinate of the mine clicked
*/
    public boolean mineClicked(int x, int y) {
        if (notifyObservers2(x, y)) {
            return true;
        }
        return false;
    }

/**
 * Call this method when the player clicks a mine.
 * @param x The row coordinate of the mine clicked
 * @param y The column coordinate of the mine clicked
*/
    public void firstClick(boolean first, int x, int y) {
        notifyObservers4(x, y);
    }

/**
 * Call this method when the player clicks a mine.
*/
    public void resetClicked() {
        notifyObservers3();
    }

/**
 * Call this method when the player loses the game.
*/
    public ArrayList<ArrayList<Integer>> lostGame() {
        return notifyObservers5();
    }

/**
 * This method initializes the GUI.
*/
    private void initialize() {
        this.mainFrame = new JFrame();
        mainFrame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel(new GridLayout(rows, columns));
        buttons = new JButton[rows][columns];
        mines = new int[rows][columns];

         // Calculate smiley button size, dynamic
        int buttonHeight = FRAME_HEIGHT / (rows + 1); // +1 to include the row with the smiley button
        int buttonWidth = FRAME_WIDTH / columns;

        // Insert buttons aka cells
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                buttons[i][j] = new JButton();
                buttons[i][j].setBackground(Color.GRAY);
                buttons[i][j].setOpaque(true);
                buttons[i][j].setFont(new Font("Arial", Font.PLAIN, 30));
                // Set action command to the row and column of the button
                buttons[i][j].setActionCommand(i + " " + j);

                // Flagging mines functionality - right click
                MouseAdapter mouseListener = new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
                            // Get the button that was right clicked
                            JButton button = (JButton) e.getSource();
                            if (button.getText() == "\uD83D\uDEA9") {
                                button.setText("");
                            } else {
                                button.setText("\uD83D\uDEA9"); // Unicode for flag emoji
                            }
                        }
                    }
                };
                buttons[i][j].addMouseListener(mouseListener);

                buttons[i][j].addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        // Get the button that was clicked
                        JButton button = (JButton) e.getSource();

                        // Get the row and column from the action command
                        String[] parts = button.getActionCommand().split(" ");
                        int row = Integer.parseInt(parts[0]);
                        int col = Integer.parseInt(parts[1]);
                        if (ifFirstMove()) {
                            if (board[row][col] != 't') {
                                firstClick(true,row,col);
                                uncovered[row][col] = true;
                            }
                        }
                        // If a mine is clicked, call agent to decide
                        if (board[row][col] == 't') {
                            // If it is undeducible, move mine!
                            if (mineClicked(row, col)) {
                                // Display the number as normal
                                printSelf();
                                if (board[row][col] == '0') {
                                    ArrayList<ArrayList<Integer>> cells = probeNeighbors(row, col);
                                    for (ArrayList<Integer> each : cells) {
                                        buttons[each.get(0)][each.get(1)].setText(Character.toString(board[each.get(0)][each.get(1)]));
                                        buttons[each.get(0)][each.get(1)].setForeground(Color.RED);

                                    }
                                    uncovered[row][col] = true;
                                } else {
                                    uncovered[row][col] = true;
                                    button.setText(Character.toString(board[row][col]));
                                    button.setForeground(Color.RED);
                                }
                                JOptionPane.showMessageDialog(mainFrame, "Quantum Safety just saved you!!");
                            } else { // If deducible, lose the game!
                                button.setText("\uD83D\uDCA3"); // Unicode for bomb emoji
                                button.setBackground(Color.DARK_GRAY);
                                JOptionPane.showMessageDialog(mainFrame, "You have lost the game!!");

                // --------------------------- Helper Instructor After Losing -------------------------------- // 
                                //Get the recorded steps
                                final ArrayList<ArrayList<Integer>> path = cleanPath(lostGame());
                                int response = JOptionPane.showConfirmDialog(mainFrame, "AI Instructor: Would you like to see why you lost?", "Show me why", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                                if (response == JOptionPane.YES_OPTION) {
                                    // Create a new JFrame for New Move button.
                                    JFrame pathFrame = new JFrame();
                                    JButton nextMoveButton = new JButton("AI Instructor's Next Move");
                                    JLabel textLabel = new JLabel(" Instruction: Keep clicking to see all further moves!!", SwingConstants.CENTER);
                                    nextMoveButton.addActionListener(new ActionListener() {

                                        Iterator<ArrayList<Integer>> pathIterator = path.iterator();

                                        public void actionPerformed(ActionEvent e) {
                                            if (pathIterator.hasNext()) {
                                                ArrayList<Integer> cell = pathIterator.next();
                                                int row = cell.get(0);
                                                int col = cell.get(1);
                                                int action = cell.get(2);
                                                // If probe a cell
                                                if (action == 1) {
                                                    if (board[row][col] == '0') {
                                                        buttons[row][col].setBackground(Color.YELLOW);
                                                        ArrayList<ArrayList<Integer>> cells = probeNeighbors(row, col);
                                                        for (ArrayList<Integer> each : cells) {
                                                            buttons[each.get(0)][each.get(1)].setText(Character.toString(board[each.get(0)][each.get(1)]));
                                                            buttons[each.get(0)][each.get(1)].setForeground(Color.BLUE);
                                                        }
                                                    } else {
                                                        buttons[row][col].setBackground(Color.YELLOW);
                                                        buttons[row][col].setText(Character.toString(board[row][col]));
                                                        buttons[row][col].setForeground(Color.BLUE);
                                                    }
                                                // If flag a cell
                                                } else {
                                                    buttons[row][col].setBackground(Color.YELLOW);
                                                    buttons[row][col].setText("\uD83D\uDEA9");
                                                    buttons[row][col].setForeground(Color.BLUE);
                                                }
                                                
                                                // If no more moves, disable button.
                                                if (!pathIterator.hasNext()) {
                                                    nextMoveButton.setEnabled(false);
                                                }
                                            }
                                        }
                                    });
                                    pathFrame.setLayout(new BorderLayout());
                                    pathFrame.add(textLabel, BorderLayout.NORTH);
                                    pathFrame.add(nextMoveButton, BorderLayout.CENTER);
                                    pathFrame.pack();
                                    pathFrame.setVisible(true);
                                }
                // --------------------------- End of Helper Instructor  -------------------------------- //

                            }   
                        } else if (board[row][col] == '0') {
                            ArrayList<ArrayList<Integer>> cells = probeNeighbors(row, col);
                            for (ArrayList<Integer> each : cells) {
                                buttons[each.get(0)][each.get(1)].setText(Character.toString(board[each.get(0)][each.get(1)]));
                                buttons[each.get(0)][each.get(1)].setForeground(Color.RED);
                            }
                            uncovered[row][col] = true;
                        } else {
                            // Display the number as normal
                            uncovered[row][col] = true;
                            button.setText(Character.toString(board[row][col]));
                            button.setForeground(Color.RED);
                        }
                        if (ifWin()) {
                            JOptionPane.showMessageDialog(mainFrame, "Congratulations! You Won the Game!!");
                        }
                        // Notify observers that the cell was clicked
                        cellClicked(row, col);
                    }
                });
                buttonPanel.add(buttons[i][j]);
            }
        }

        // Reset smiley button
        JButton smileyButton = new JButton("\uD83D\uDE00");
        smileyButton.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
        smileyButton.setFont(new Font("Arial", Font.PLAIN, buttonHeight / 2));
        // When clicked
        smileyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resetClicked();
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < columns; j++) {
                        buttons[i][j].setText("");
                        buttons[i][j].setBackground(Color.GRAY);
                        uncovered[i][j] = false;
                    }
                }
                printSelf();
            }
        });

        JPanel smileyPanel = new JPanel(); // panel to center the smiley button
        smileyPanel.add(smileyButton);

        mainFrame.add(smileyPanel, BorderLayout.NORTH);
        mainFrame.add(buttonPanel, BorderLayout.CENTER);

        mainFrame.setVisible(true);
    }

/**
 * This method weeds out the unnecessary recorded steps taken by the agent.
 * @param steps The list of recorded steps
 * @return the new list of recorded steps after cleaning
*/
    public ArrayList<ArrayList<Integer>> cleanPath(ArrayList<ArrayList<Integer>> steps) {
        ArrayList<ArrayList<Integer>> newSteps = new ArrayList<ArrayList<Integer>>();
        for (ArrayList<Integer> each : steps) {
            // If the recorded step is already made by the user, discard it
            if (!uncovered[each.get(0)][each.get(1)]) {
                newSteps.add(each);
            }
        }
        return newSteps;
    }

/**
 * This method checks if the user has won the game.
*/
    public boolean ifWin() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
               if (board[i][j] != 't' & !uncovered[i][j]) {
                return false;
               }
            }
        }
        return true;
    }

/**
 * This method checks if a move is the first move.
*/
    public boolean ifFirstMove() {
        int count = 0;
        for (int i = 0; i < this.board.length; i++) {
            for (int j = 0; j < this.board[0].length; j++) {
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
 * This method prints the current board from GUI's view.
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
 * This method probes all the neighbors if 0 is discovered.
 * @param x The row coordinate of the cell
 * @param y The column coordinate of the cell
*/
    public ArrayList<ArrayList<Integer>> probeNeighbors(int x, int y) {
        ArrayList<ArrayList<Integer>> cells = new ArrayList<ArrayList<Integer>>();
        ArrayList<Integer> tmp = new ArrayList<Integer>();
        tmp.add(x);
        tmp.add(y);
        cells.add(tmp);
        for (int i = x-1; i <= x+1; i++) {
            for (int j = y-1; j <= y+1; j++) {
                if (i >= 0 && i < board.length && j >= 0 && j < board[0].length && !uncovered[i][j]) {
                    char cellValue = board[i][j];
                    uncovered[i][j] = true;
                    ArrayList<Integer> temp = new ArrayList<Integer>();
                    temp.add(i);
                    temp.add(j);
                    cells.add(temp);
                    if (cellValue == '0') {
                        cells.addAll(probeNeighbors(i, j));
                    }
                }
            }
        }
        return cells;
    }

/**
 * This method closes the GUI.
*/
    public void closeGui() {
        mainFrame.setVisible(false);
        mainFrame.dispose();
    }
}

