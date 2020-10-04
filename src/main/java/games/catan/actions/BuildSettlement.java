package games.catan.actions;

import core.AbstractGameState;
import core.actions.AbstractAction;
import games.catan.CatanGameState;
import games.catan.CatanTile;

public class BuildSettlement extends AbstractAction {
    int row;
    int col;
    int vertex;

    public BuildSettlement(int row, int col, int vertex){
        this.row = row;
        this.col = col;
        this.vertex = vertex;
    }
    @Override
    public boolean execute(AbstractGameState gs) {
        // todo (mb) check if valid
        CatanGameState cgs = (CatanGameState)gs;
        CatanTile[][] board = cgs.getBoard();

        if (vertex == 0){
            board[row][col].addSettlement(vertex, cgs.getCurrentPlayer());
            if (col-1 >= 0)
                    board[row][col-1].addSettlement(2, cgs.getCurrentPlayer());
            if (row+1 < board.length)
                board[row+1][col].addSettlement(4, cgs.getCurrentPlayer());
        }
        return true;
    }

    @Override
    public AbstractAction copy() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String getString(AbstractGameState gameState) {
        return null;
    }
}
