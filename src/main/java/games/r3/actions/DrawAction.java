package games.r3.actions;

import core.AbstractGameState;
import core.actions.AbstractAction;
import games.r3.R3GameState;

import java.util.Objects;

/**
 * Action to draw a card from the draw pile and add it to the current player's hand.
 */
public class DrawAction extends AbstractAction {

    public static final DrawAction INSTANCE = new DrawAction();

    private final int hash;

    public DrawAction(){
        super();
        this.hash = Objects.hash(this.getClass().getName());
    }

    @Override
    public boolean execute(AbstractGameState gs) {
        var s = (R3GameState) gs;

        s.hands.get(s.getCurrentPlayer()).add(s.drawPile.draw());

        return true;
    }

    @Override
    public DrawAction copy() {
        // immutable, no actual copy needed
        return this;
    }

    @Override
    public boolean equals(Object o) {
        // immutable, reference equality suffices
        return this == o;
    }

    @Override
    public int hashCode() {
        // immutable, precomputed hash
        return hash;
    }

    public String toString() {
        return "Draw";
    }

    @Override
    public String getString(AbstractGameState gameState) {
        return toString();
    }
}
