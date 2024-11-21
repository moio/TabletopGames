package games.r3;

import core.AbstractGameState;
import core.AbstractParameters;
import core.components.Component;
import core.components.Deck;
import games.GameType;
import games.r3.components.R3Card;
import utilities.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class R3GameState extends AbstractGameState {
    // common
    public Deck<R3Card> drawPile;

    // per-player
    public List<List<Deck<R3Card>>> positionPiles;
    public List<Deck<R3Card>> hands;

    public R3GameState(AbstractParameters gameParameters, int nPlayers) {
        super(gameParameters, nPlayers);
    }

    @Override
    protected GameType _getGameType() {
        return GameType.R3;
    }

    /**
     * Returns all Components used in the game and referred to by componentId from actions or rules.
     * This method is called after initialising the game state, so all components will be initialised already.
     *
     * @return - List of Components in the game.
     */
    @Override
    protected List<Component> _getAllComponents() {
        return new ArrayList<>() {{
            add(drawPile);
            positionPiles.forEach(this::addAll);
            addAll(hands);
        }};
    }

    /**
     * <p>Create a deep copy of the game state containing only those components the given player can observe.</p>
     * <p>If the playerID is NOT -1 and If any components are not visible to the given player (e.g. cards in the hands
     * of other players or a face-down deck), then these components should instead be randomized (in the previous examples,
     * the cards in other players' hands would be combined with the face-down deck, shuffled together, and then new cards drawn
     * for the other players). This process is also called 'redeterminisation'.</p>
     * <p>There are some utilities to assist with this in utilities.DeterminisationUtilities. One firm is guideline is
     * that the standard random number generator from getRnd() should not be used in this method. A separate Random is provided
     * for this purpose - redeterminisationRnd.
     * This is to avoid this RNG stream being distorted by the number of player actions taken (where those actions are not themselves inherently random)</p>
     * <p>If the playerID passed is -1, then full observability is assumed and the state should be faithfully deep-copied.</p>
     *
     * <p>Make sure the return type matches the class type, and is not AbstractGameState.</p>
     *
     * @param playerId - player observing this game state.
     */
    @Override
    protected R3GameState _copy(int playerId) {
        R3GameState copy = new R3GameState(gameParameters, getNPlayers());

        // copy all components
        copy.positionPiles = new ArrayList<>();
        for (var pile : this.positionPiles) {
            var newPile = new ArrayList<Deck<R3Card>>();
            for (var deck : pile) {
                newPile.add(deck.copy());
            }
            copy.positionPiles.add(newPile);
        }

        copy.drawPile = this.drawPile.copy();

        copy.hands = new ArrayList<>();
        for (var hand : this.hands) {
            copy.hands.add(hand.copy());
        }

        // if full observability, return the copy
        if (playerId == -1) {
            return copy;
        }

        // otherwise, redeterminise parts of the state that are not visible to the requesting player
        for (int i = 0; i < nPlayers; i++) {
            if (i != playerId) {
                copy.drawPile.add(copy.hands.get(i));
                copy.hands.get(i).clear();
            }
        }

        copy.drawPile.shuffle(redeterminisationRnd);

        for (int i = 0; i < nPlayers; i++) {
            if (i != playerId) {
                for (int j = 0; j < this.hands.get(i).getSize(); j++) {
                    copy.hands.get(i).add(copy.drawPile.draw());
                }
            }
        }

        return copy;
    }

    /**
     * Applies the fighting algorithm from this state and returns the health of each player after the fight.
     */
    public int[] unleashTheFury(int maxIterations) {
        var p = (R3Parameters) getGameParameters();

        var health = new int[getNPlayers()];
        for (int i = 0; i < getNPlayers(); i++) {
            health[i] = p.healthPoints;
        }
        var activePositions = new int[getNPlayers()];
        var rotationSenses = new int[getNPlayers()];
        for (int i = 0; i < getNPlayers(); i++) {
            rotationSenses[i] = 1;
        }
        System.out.println("Unleashing the fury!");
        System.out.println("  Health: " + health[0] + " vs " + health[1]);
        for (int i = 0; i < maxIterations; i++) {
            // resolve actions
            System.out.println("  Iteration " + i);
            for (int player = 0; player < 2; player++) {
                var opponent = (player + 1) % 2;
                var activeDeck = positionPiles.get(player).get(activePositions[player]);
                var opponentActiveDeck = positionPiles.get(opponent).get(activePositions[opponent]);

                var card = activeDeck.peek();
                if (card == null) {
                    continue;
                }
                var opponentCard = opponentActiveDeck.peek();
                if (card.kind == R3Card.KIND.ATTACK) {
                    var attackPoints = getCardPoints(card, activeDeck.getSize());
                    System.out.println("    Player " + player + " attacks with " + card.suit + " (" + attackPoints + ", " + activeDeck.getSize() + " cards)");
                    if (opponentCard != null && (
                            card.suit == R3Card.SUIT.ROCK && opponentCard.suit == R3Card.SUIT.PAPER ||
                            card.suit == R3Card.SUIT.PAPER && opponentCard.suit == R3Card.SUIT.SCISSORS ||
                            card.suit == R3Card.SUIT.SCISSORS && opponentCard.suit == R3Card.SUIT.ROCK
                        )) {
                        // defense is valid for the attack
                        var defensePoints = getCardPoints(opponentCard, opponentActiveDeck.getSize());
                        health[opponent] -= Math.max(attackPoints - defensePoints, 0);
                        System.out.println("      Player " + opponent + " defends with " + opponentCard.suit + " (" + defensePoints + ", " + opponentActiveDeck.getSize() + " cards)");
                    } else {
                        health[opponent] -= attackPoints;
                    }
                } else if (card.kind == R3Card.KIND.ROTATION_CHANGE) {
                    rotationSenses[player] *= -1;
                    System.out.println("    Player " + player + " rotates!");
                }
            }

            // resolve rotations (prepare for next iteration)
            for (int player = 0; player < getNPlayers(); player++) {
                activePositions[player] = ((activePositions[player] + rotationSenses[player]) + p.positions) % p.positions;
            }

            System.out.println("  Health: " + health[0] + " vs " + health[1]);

            // check if game is over already
            for (int j = 0; j < health.length; j++) {
                if (health[j] <= 0){
                    System.out.println("  Match done!");
                    return health;
                }
            }
        }

        return health;
    }

    /**
     * Returns the points of a kind and suit for a given count of identical cards.
     */
    public int getCardPoints(R3Card card, int count) {
        var p = (R3Parameters) getGameParameters();
        var cardPoints = p.cardPoints.get(new Pair<>(card.kind, card.suit));
        var index = count -1;
        if (index >= cardPoints.length) {
            index = cardPoints.length - 1;
        }
        return cardPoints[index];
    }

    /**
     * @param playerId - player observing the state.
     * @return a score for the given player approximating how well they are doing (e.g. how close they are to winning
     * the game); a value between 0 and 1 is preferred, where 0 means the game was lost, and 1 means the game was won.
     */
    @Override
    protected double _getHeuristicScore(int playerId) {
        var p = (R3Parameters) getGameParameters();

        if (isNotTerminal()) {
            var expectedHealth = unleashTheFury(p.heuristicMaxIterations);
            return ((double) Math.max(expectedHealth[playerId] - expectedHealth[(playerId + 1) % getNPlayers()], 0)) / p.healthPoints;
        } else {
            // The game finished, we can instead return the actual result of the game for the given player.
            return getPlayerResults()[playerId].value;
        }
    }


    /**
     * @param playerId - player observing the state.
     * @return the true score for the player, according to the game rules. May be 0 if there is no score in the game.
     */
    @Override
    public double getGameScore(int playerId) {
        var p = (R3Parameters) getGameParameters();
        var expectedHealth = unleashTheFury(p.maxIterations);
        return p.healthPoints - expectedHealth[(playerId + 1) % getNPlayers()];
    }

    @Override
    protected boolean _equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        R3GameState that = (R3GameState) o;
        return Objects.equals(drawPile, that.drawPile) && Objects.equals(positionPiles, that.positionPiles) && Objects.equals(hands, that.hands);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), drawPile, positionPiles, hands);
    }

    // This method can be used to log a game event (e.g. for something game-specific that you want to include in the metrics)
    // public void logEvent(IGameEvent...)
}
