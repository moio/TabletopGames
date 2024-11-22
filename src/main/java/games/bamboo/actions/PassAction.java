package games.bamboo.actions;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.components.Card;
import core.components.Component;
import games.bamboo.BambooGameState;
import games.bamboo.BambooParameters;
import games.bamboo.components.NumberCard;
import games.bamboo.components.OperatorCard;

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
public class PassAction extends AbstractAction {
    public final boolean shufflingOperators;

    public PassAction(boolean shufflingOperators){
        this.shufflingOperators = shufflingOperators;
    }

    /**
     * Executes this action, applying its effect to the given game state. Can access any component IDs stored
     * through the {@link AbstractGameState#getComponentById(int)} method.
     * @param gs - game state which should be modified by this action.
     * @return - true if successfully executed, false otherwise.
     */
    @Override
    public boolean execute(AbstractGameState gs) {
        var s = (BambooGameState) gs;
        var p = (BambooParameters) s.getGameParameters();
        var playerId = s.getCurrentPlayer();

        // draw _number_
        s.numberHands.get(playerId).add(s.numberDrawPile.draw());

        // optionally shuffle _operators_
        if (shufflingOperators) {
            var hand = s.operatorHands.get(playerId);

            for (int i = 0; i < p.operatorHandSize; i++) {
                s.operatorDrawPile.add(hand.draw());
            }

            s.operatorDrawPile.shuffle(s.getRnd());

            for (int i = 0; i < p.operatorHandSize; i++) {
                hand.add(s.operatorDrawPile.draw());
            }
        }
        else {
            if (s.operatorDrawPile.getSize() > 0) {
                // draw _operator_
                s.operatorHands.get(playerId).add(s.operatorDrawPile.draw());
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
    public PassAction copy() {
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PassAction that = (PassAction) o;
        return shufflingOperators == that.shufflingOperators;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(shufflingOperators);
    }

    @Override
    public String toString() {
        return "PassAction{" +
                "shufflingOperators=" + shufflingOperators +
                '}';
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
