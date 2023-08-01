package model;

import java.util.ArrayList;

public interface AgentObserver {
    void onCellClicked(boolean[][] user, int x, int y);
    boolean mineClicked(int x, int y);
    void resetClicked();
    void firstClick(int p,int q);
    ArrayList<ArrayList<Integer>> lost();
}
