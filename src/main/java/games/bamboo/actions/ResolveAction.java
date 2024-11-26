package games.bamboo.actions;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.components.Card;
import core.components.Component;
import games.bamboo.BambooGameState;
import games.bamboo.BambooParameters;
import games.bamboo.components.NumberCard;
import games.bamboo.components.Op;
import games.bamboo.components.OperatorCard;
import games.totoro.components.TotoroCard;

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
public class ResolveAction extends AbstractAction {
    public final List<Integer> numberIDs;
    public final List<Integer> operatorIDs;

    public final List<String> numberStrings;
    public final List<String> operatorStrings;
    public final Integer objectiveId;
    public final int objectiveValue;

    public ResolveAction(List<NumberCard> numbers, List<OperatorCard> operators, NumberCard objectiveCard) {
        // sanity check
        if (operators.size() != numbers.size() -1) {
            throw new RuntimeException("bad operators");
        }

        this.numberIDs = numbers.stream().map(Component::getComponentID).toList();
        this.operatorIDs = operators.stream().map(Component::getComponentID).toList();

        this.numberStrings = numbers.stream().map(c -> c.value + "").toList();
        this.operatorStrings = operators.stream().map(c -> c.op.toString()).toList();

        this.objectiveId = objectiveCard.getComponentID();
        this.objectiveValue = objectiveCard.value;
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

        var numberWonPile = s.numberWonPiles.get(playerId);
        var hand = s.numberHands.get(playerId);

        // take cards out of hands into won pile
        numberIDs.forEach(i -> {
            var c = (NumberCard) s.getComponentById(i);
            hand.remove(c);
            numberWonPile.add(c);
        });

        // take objectiveCard into won pile
        var objectiveCard = (NumberCard) s.getComponentById(objectiveId);
        s.objective.remove(objectiveCard);
        numberWonPile.add(objectiveCard);

        // replenish objective
        var nextObjective = s.objectiveDrawPile.draw();
        if (nextObjective != null) {
            s.objective.add(nextObjective);
        }

        // fix current player numbers hand
        var replenishment = p.numberHandSize - hand.getSize();
        for (int i = 0; i < replenishment; i++) {
            var c = s.numberDrawPile.draw();
            if (c != null) {
                hand.add(c);
            }
        }

        // fix other player numbers hands
        for (int i = 0; i < s.getNPlayers(); i++) {
            if (i != playerId) {
                var otherPlayerHand = s.numberHands.get(i);
                var punishment = otherPlayerHand.getSize() - p.numberHandSize;
                for (int j = 0; j < punishment; j++) {
                    numberWonPile.add(otherPlayerHand.pick(s.getRnd()));
                }
            }
        }

        // fix all player operators hands
        for (int i = 0; i < s.getNPlayers(); i++) {
            var playerHand = s.operatorHands.get(i);
            for (int j = 0; j < playerHand.getSize(); j++) {
                s.operatorDrawPile.add(playerHand.draw());
            }
        }
        s.operatorDrawPile.shuffle(s.getRnd());
        for (int i = 0; i < s.getNPlayers(); i++) {
            var playerHand = s.operatorHands.get(i);
            for (int j = 0; j < p.operatorHandSize; j++) {
                playerHand.add(s.operatorDrawPile.draw());
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
    public ResolveAction copy() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResolveAction that = (ResolveAction) o;
        return Objects.equals(numberIDs, that.numberIDs) && Objects.equals(operatorIDs, that.operatorIDs) && Objects.equals(objectiveValue, that.objectiveValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numberIDs, operatorIDs, objectiveValue);
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

    @Override
    public String toString() {
        var result = new StringBuilder();

        result.append(numberStrings.get(0));
        for (int i = 0; i < operatorStrings.size(); i++) {
            result.append(operatorStrings.get(i));
            result.append(numberStrings.get(i+1));
        }
        result.append("=");
        result.append(objectiveValue);

        return result.toString();
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
