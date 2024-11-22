package games.r3.components;

import core.components.Card;

import java.util.Objects;

public class R3Card extends Card {

    public enum KIND {
        ATTACK,
        DEFENSE,
        ROTATION_CHANGE
    }

    public enum SUIT {
        ROCK,
        PAPER,
        SCISSORS,
        NONE
    }

    public final KIND kind;
    public final SUIT suit;
    public final int copy;
    public final int hash;

    public R3Card(KIND kind, SUIT suit, int copy) {
        super(repr(kind, suit, copy));
        this.kind = kind;
        this.suit = suit;
        this.copy = copy;
        this.hash = Objects.hash(super.hashCode(), kind, suit, copy);
    }

    private static String repr(KIND kind, SUIT suit, int copy) {
        return kind.name().substring(0,1) + suit.name().substring(0,1);
    }

    @Override
    public String toString() {
        return repr(kind, suit, copy);
    }

    @Override
    public R3Card copy(int playerId) {
        // immutable, no actual copy needed
        return this;
    }

    @Override
    public R3Card copy() {
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
