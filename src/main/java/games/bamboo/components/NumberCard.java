package games.bamboo.components;

import core.components.Card;

import java.util.Objects;

public class NumberCard extends Card {
    public final int value;
    public final int copy;
    public final int hash;

    public NumberCard(int value) {
        this(value, 0);
    }

    public NumberCard(int value, int copy) {
        super(value + (copy > 0 ? " (copy " + copy + ")" : ""));
        this.value = value;
        this.copy = copy;
        this.hash = Objects.hash(super.hashCode(), value, copy);
    }

    @Override
    public String toString() {
        return value + (copy > 0 ? " (copy " + copy + ")" : "");
    }

    @Override
    public NumberCard copy(int playerId) {
        // immutable, no actual copy needed
        return this;
    }

    @Override
    public NumberCard copy() {
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
