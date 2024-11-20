package games.bamboo;

import core.AbstractGameState;
import core.AbstractParameters;
import core.components.Component;
import core.components.Deck;
import games.GameType;
import games.bamboo.components.NumberCard;
import games.bamboo.components.OperatorCard;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>The game state encapsulates all game information. It is a data-only class, with game functionality present
 * in the Forward Model or actions modifying the state of the game.</p>
 * <p>Most variables held here should be {@link Component} subclasses as much as possible.</p>
 * <p>No initialisation or game logic should be included here (not in the constructor either). This is all handled externally.</p>
 * <p>Computation may be included in functions here for ease of access, but only if this is querying the game state information.
 * Functions on the game state should never <b>change</b> the state of the game.</p>
 */
public class BambooGameState extends AbstractGameState {
    // common
    public List<NumberCard> objective;
    public Deck<NumberCard> numberDrawPile;
    public Deck<NumberCard> numberDiscardPile;
    public Deck<OperatorCard> operatorDrawPile;

    // per-player
    public List<Deck<NumberCard>> numberHands;
    public List<Deck<OperatorCard>> operatorHands;
    public List<Deck<NumberCard>> numberWonPiles;

    /**
     * @param gameParameters - game parameters.
     * @param nPlayers       - number of players in the game
     */
    public BambooGameState(AbstractParameters gameParameters, int nPlayers) {
        super(gameParameters, nPlayers);
    }

    /**
     * @return the enum value corresponding to this game, declared in {@link GameType}.
     */
    @Override
    protected GameType _getGameType() {
        return GameType.Bamboo;
    }

    /**
     * Returns all Components used in the game and referred to by componentId from actions or rules.
     * This method is called after initialising the game state, so all components will be initialised already.
     *
     * @return - List of Components in the game.
     */
    @Override
    protected List<Component> _getAllComponents() {
        return new ArrayList<>(){{
            addAll(objective);
            add(numberDrawPile);
            add(numberDiscardPile);
            add(operatorDrawPile);

            addAll(numberHands);
            addAll(operatorHands);
            addAll(numberWonPiles);
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
     *  This is to avoid this RNG stream being distorted by the number of player actions taken (where those actions are not themselves inherently random)</p>
     * <p>If the playerID passed is -1, then full observability is assumed and the state should be faithfully deep-copied.</p>
     *
     * <p>Make sure the return type matches the class type, and is not AbstractGameState.</p>
     *
     *
     * @param playerId - player observing this game state.
     */
    @Override
    protected BambooGameState _copy(int playerId) {
        BambooGameState copy = new BambooGameState(gameParameters, getNPlayers());

        copy.objective = new ArrayList<>();
        for (var digit: this.objective) {
            copy.objective.add(digit.copy());
        }
        copy.numberDrawPile = this.numberDrawPile.copy();
        copy.numberDiscardPile = this.numberDiscardPile.copy();
        copy.operatorDrawPile = this.operatorDrawPile.copy();

        copy.numberHands = new ArrayList<>();
        for (var hand: this.numberHands) {
            copy.numberHands.add(hand.copy());
        }
        copy.operatorHands = new ArrayList<>();
        for (var hand: this.operatorHands) {
            copy.operatorHands.add(hand.copy());
        }
        copy.numberWonPiles = new ArrayList<>();
        for (var hand: this.numberWonPiles) {
            copy.numberWonPiles.add(hand.copy());
        }

        if (playerId == -1) {
            return copy;
        }

        for (int i = 0; i < nPlayers; i++) {
            if (i != playerId) {
                copy.numberDrawPile.add(copy.numberHands.get(i));
                copy.numberHands.get(i).clear();

                copy.operatorDrawPile.add(copy.operatorHands.get(i));
                copy.operatorHands.get(i).clear();
            }
        }

        copy.numberDrawPile.shuffle(redeterminisationRnd);
        copy.operatorDrawPile.shuffle(redeterminisationRnd);

        for (int i = 0; i < nPlayers; i++) {
            if (i != playerId) {
                for (int j = 0; j < this.numberHands.get(i).getSize(); j++) {
                    copy.numberHands.get(i).add(copy.numberDrawPile.draw());
                }

                for (int j = 0; j < this.operatorHands.get(i).getSize(); j++) {
                    copy.operatorHands.get(i).add(copy.operatorDrawPile.draw());
                }
            }
        }

        return copy;
    }

    public int getNextPlayer(int fromPlayerId) {
        // Es: getNPlayers = 2 ; playerIds = [0,1] ; fromPlayerId = 0 ; nextPlayerId = 1
        return fromPlayerId + 1 < getNPlayers() ? fromPlayerId + 1 : 0;
    }

    /**
     * @param playerId - player observing the state.
     * @return a score for the given player approximating how well they are doing (e.g. how close they are to winning
     * the game); a value between 0 and 1 is preferred, where 0 means the game was lost, and 1 means the game was won.
     */
    @Override
    protected double _getHeuristicScore(int playerId) {
        var p = (BambooParameters) getGameParameters();

        if (isNotTerminal()) {
            return ((double) this.numberWonPiles.get(playerId).getSize()) / p.allNumberCards.size();
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
        return numberWonPiles.get(playerId).getSize();
    }

    @Override
    protected boolean _equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BambooGameState that = (BambooGameState) o;
        return Objects.equals(objective, that.objective) && Objects.equals(numberDrawPile, that.numberDrawPile) && Objects.equals(numberDiscardPile, that.numberDiscardPile) && Objects.equals(operatorDrawPile, that.operatorDrawPile) && Objects.equals(numberHands, that.numberHands) && Objects.equals(operatorHands, that.operatorHands) && Objects.equals(numberWonPiles, that.numberWonPiles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), objective, numberDrawPile, numberDiscardPile, operatorDrawPile, numberHands, operatorHands, numberWonPiles);
    }

    // This method can be used to log a game event (e.g. for something game-specific that you want to include in the metrics)
    // public void logEvent(IGameEvent...)
}
