package games.bamboo.components;

import core.components.Card;

import java.util.Objects;

public class OperatorCard extends Card {
    public final Op op;
    public final int copy;
    public final int hash;

    public OperatorCard(Op op, int copy) {
        super(op.name() + (copy > 0 ? " (copy " + copy + ")" : ""));
        this.op = op;
        this.copy = copy;
        this.hash = Objects.hash(super.hashCode(), op, copy);
    }


    @Override
    public String toString() {
        return op.name() + (copy > 0 ? " (copy " + copy + ")" : "");
    }

    @Override
    public OperatorCard copy(int playerId) {
        // immutable, no actual copy needed
        return this;
    }

    @Override
    public OperatorCard copy() {
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
}
