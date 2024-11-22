package games.totoro.actions;

import core.AbstractGameState;
import core.CoreConstants;
import core.actions.AbstractAction;
import core.components.Card;
import core.components.Component;
import core.components.Deck;
import games.totoro.TotoroGameState;
import games.totoro.TotoroParameters;
import games.totoro.components.TotoroCard;
import games.totoro.metrics.TotoroMetrics;

import java.util.List;
import java.util.Objects;

/**
 * <p>Actions are unit things players can do in the game (e.g. play a card, move a pawn, roll dice, attack etc.).</p>
 * <p>Actions in the game can (and should, if applicable) extend one of the other existing actions, in package {@link core.actions}.
 * Or, a game may simply reuse one of the existing core actions.</p>
 * <p>Actions may have parameters, so as not to duplicate actions for the same type of functionality,
 * e.g. playing card of different types (see {@link games.sushigo.actions.ChooseCard} action from SushiGo as an example).
 * Include these parameters in the class constructor.</p>
 * <p>They need to extend at a minimum the {@link AbstractAction} super class and implement the {@link AbstractAction#execute(AbstractGameState)} method.
 * This is where the main functionality of the action should be inserted, which modifies the given game state appropriately (e.g. if the action is to play a card,
 * then the card will be moved from the player's hand to the discard pile, and the card's effect will be applied).</p>
 * <p>They also need to include {@link Object#equals(Object)} and {@link Object#hashCode()} methods.</p>
 * <p>They <b>MUST NOT</b> keep references to game components. Instead, store the {@link Component#getComponentID()}
 * in variables for any components that must be referenced in the action. Then, in the execute() function,
 * use the {@link AbstractGameState#getComponentById(int)} function to retrieve the actual reference to the component,
 * given your componentID.</p>
 */
public class PlayOperationAction extends AbstractAction {

    public final List<Integer> operandIDs;
    public final List<String> strings;

    public PlayOperationAction(List<TotoroCard> cards) {
        this.operandIDs = cards.stream().map(Component::getComponentID).toList();
        this.strings = cards.stream().map(Card::toString).toList();
    }

    /**
     * Executes this action, applying its effect to the given game state. Can access any component IDs stored
     * through the {@link AbstractGameState#getComponentById(int)} method.
     * @param gs - game state which should be modified by this action.
     * @return - true if successfully executed, false otherwise.
     */
    @Override
    public boolean execute(AbstractGameState gs) {
        var s = (TotoroGameState) gs;

        Deck<TotoroCard> operation = new Deck<>("Operation", CoreConstants.VisibilityMode.VISIBLE_TO_ALL);
        operandIDs.forEach(i -> {
            var c = (TotoroCard) s.getComponentById(i);
            s.hand.remove(c);
            s.offer.remove(c);
            operation.add(c);
            s.logEvent(TotoroMetrics.TotoroEvent.CardPlayed, c.toString());
        });
        s.operations.add(operation);

        while (s.offer.getSize() < ((TotoroParameters)s.getGameParameters()).offerSize) {
            var drawn = s.drawPile.draw();
            if (drawn != null) {
                s.offer.add(drawn);
            }
            else {
                break;
            }
        }

        return true;
    }

    /**
     * @return Make sure to return an exact <b>deep</b> copy of the object, including all of its variables.
     * Make sure the return type is this class (e.g. GTAction) and NOT the super class AbstractAction.
     * <p>If all variables in this class are final or effectively final (which they should be),
     * then you can just return <code>`this`</code>.</p>
     */
    @Override
    public PlayOperationAction copy() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayOperationAction that = (PlayOperationAction) o;
        return Objects.equals(operandIDs, that.operandIDs);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(operandIDs);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        for (var i = 0; i < strings.size(); i++) {
            sb.append(strings.get(i));
            if (i < strings.size() - 2) {
                sb.append("+");
            }
            else if (i < strings.size() - 1) {
                sb.append("=");
            }
        }
        return sb.toString();
    }

    /**
     * @param gameState - game state provided for context.
     * @return A more descriptive alternative to the toString action, after access to the game state to e.g.
     * retrieve components for which only the ID is stored on the action object, and include the name of those components.
     * Optional.
     */
    @Override
    public String getString(AbstractGameState gameState) {
        return toString();
    }


    /**
     * This next one is optional.
     *
     *  May optionally be implemented if Actions are not fully visible
     *  The only impact this has is in the GUI, to avoid this giving too much information to the human player.
     *
     *  An example is in Resistance or Sushi Go, in which all cards are technically revealed simultaneously,
     *  but the game engine asks for the moves sequentially. In this case, the action should be able to
     *  output something like "Player N plays card", without saying what the card is.
     * @param gameState - game state to be used to generate the string.
     * @param playerId - player to whom the action should be represented.
     * @return
     */
   // @Override
   // public String getString(AbstractGameState gameState, int playerId);
}
