package games.totoro;

import core.AbstractGameState;
import core.AbstractParameters;
import core.components.Component;
import core.components.Deck;
import core.interfaces.IPrintable;
import games.GameType;
import games.totoro.components.TotoroCard;

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
public class TotoroGameState extends AbstractGameState implements IPrintable {


    public Deck<TotoroCard> hand;
    public Deck<TotoroCard> offer;
    public Deck<TotoroCard> drawPile;
    public List<Deck<TotoroCard>> operations;

    /**
     * @param gameParameters - game parameters.
     */
    public TotoroGameState(AbstractParameters gameParameters) {
        super(gameParameters, 1);
    }

    /** for compatibility with rest of framework, nPlayers is actually ignored*/
    public TotoroGameState(AbstractParameters gameParameters, int nPlayers) {
        this(gameParameters);
    }

    /**
     * @return the enum value corresponding to this game, declared in {@link GameType}.
     */
    @Override
    protected GameType _getGameType() {
        return GameType.Totoro;
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
            add(hand);
            add(offer);
            add(drawPile);
        }};
    }

    /**
     * <p>Create a deep copy of the game state containing only those components the given player can observe.</p>
     * <p>If the playerID is NOT -1 and If any components are not visible to the given player (e.g. cards in the hands
     * of other players or a face-down deck), then these components should instead be randomized (in the previous examples,
     * the cards in other players' hands would be combined with the face-down deck, shuffled together, and then new cards drawn
     * for the other players). This process is also called 'redeterminisation'.</p>
     * <p>There are some utilities to assist with this in utilities.DeterminisationUtilities. One firm guideline is
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
    protected TotoroGameState _copy(int playerId) {
        TotoroGameState copy = new TotoroGameState(gameParameters.copy());

        if (playerId == -1 || true) {
            copy.hand = hand.copy();
            copy.offer = offer.copy();
            copy.drawPile = drawPile.copy();
            copy.operations = new ArrayList<>();
            for (Deck<TotoroCard> o : operations) {
                copy.operations.add(o.copy());
            }
            return copy;
        }

        // else playerId must be 0
        copy.hand = hand.copy();
        copy.offer = offer.copy();
        copy.drawPile = drawPile.copy();
        copy.drawPile.shuffle(redeterminisationRnd);
        copy.operations = new ArrayList<>();
        for (Deck<TotoroCard> o : operations) {
            copy.operations.add(o.copy());
        }

        return copy;
    }

    /**
     * @param playerId - player observing the state.
     * @return a score for the given player approximating how well they are doing (e.g. how close they are to winning
     * the game); a value between 0 and 1 is preferred, where 0 means the game was lost, and 1 means the game was won.
     */
    @Override
    protected double _getHeuristicScore(int playerId) {
        if (isNotTerminal()) {
            return ((double)operations.size()) / ((TotoroParameters)getGameParameters()).operationSize + hand.getSize() / 10.0;
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
        return operations.size();
    }

    @Override
    public boolean _equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TotoroGameState that = (TotoroGameState) o;
        return Objects.equals(hand, that.hand) && Objects.equals(offer, that.offer) && Objects.equals(drawPile, that.drawPile) && Objects.equals(operations, that.operations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), hand, offer, drawPile, operations);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("Hand: " + hand);
        sb.append("\n");
        sb.append("Offer: " + offer);
        sb.append("\n");
        sb.append("Previously won: " + operations);

        return sb.toString();
    }
}
