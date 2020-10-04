package games.catan.actions;

import core.AbstractGameState;
import core.actions.AbstractAction;
import games.catan.CatanGameState;
import games.catan.CatanTile;

public class BuildRoad extends AbstractAction {
    int row;
    int col;
    int edge;
    int playerID;

    public BuildRoad(int row, int col, int edge, int playerID){
        this.row = row;
        this.col = col;
        this.edge = edge;
        this.playerID = playerID;
    }

    @Override
    public boolean execute(AbstractGameState gs) {
        // todo (mb) check if valid
        CatanGameState cgs = (CatanGameState)gs;
        CatanTile[][] board = cgs.getBoard();
        board[this.row][this.col].addRoad(edge, playerID);
        return true;
    }

    @Override
    public AbstractAction copy() {
        BuildRoad copy = new BuildRoad(row, col, edge, playerID);
        return copy;
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
        // todo put random roads on the board
        return "Buildroad in row=" + row + " col=" + col + " edge=" + edge;
    }
}
