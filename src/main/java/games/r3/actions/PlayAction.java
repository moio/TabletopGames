package games.r3.actions;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.components.Component;
import core.components.Deck;
import games.r3.R3GameState;
import games.r3.components.R3Card;

import java.util.List;
import java.util.Objects;

/**
 * Action to play a card from the player's hand into a position pile.
 */
public class PlayAction extends AbstractAction {

    // note this class should not store Components, therefore we are storing the IDs of the cards
    public final List<Integer> cardIDs;
    public final List<String> cardStrings;
    public final int deckID;
    public final String deckString;

    public PlayAction(List<R3Card> cards, Deck<R3Card> deck) {
        this.cardIDs = cards.stream().map(Component::getComponentID).toList();
        this.cardStrings = cards.stream().map(R3Card::toString).toList();
        this.deckID = deck.getComponentID();
        this.deckString = deck.getComponentName();
    }

    @Override
    public boolean execute(AbstractGameState gs) {
        var s = (R3GameState) gs;
        var playerId = s.getCurrentPlayer();

        var deck = (Deck<R3Card>) s.getComponentById(deckID);

        for (Integer cardID : cardIDs) {
            var card = (R3Card) s.getComponentById(cardID);
            deck.add(card);
            s.hands.get(playerId).remove(card);
        }

        return true;
    }

    @Override
    public PlayAction copy() {
        // immutable, no actual copy needed
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayAction that = (PlayAction) o;
        return deckID == that.deckID && Objects.equals(cardIDs, that.cardIDs) && Objects.equals(cardStrings, that.cardStrings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardIDs, cardStrings, deckID);
    }

    @Override
    public String toString() {
        return "Play " + cardStrings + " in " + deckString;
    }

    @Override
    public String getString(AbstractGameState gameState) {
        // toString already takes gameState into account
        return toString();
    }
}
