package games.bamboo.components;

public enum Op {
    PLUS,
    MINUS,
    ;

    @Override
    public String toString() {
        return switch (this) {
            case PLUS -> "+";
            case MINUS -> "-";
        };
    }
}
