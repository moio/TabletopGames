package games.r3;

import core.AbstractGameState;
import core.CoreConstants;
import core.StandardForwardModel;
import core.actions.AbstractAction;
import core.components.Deck;
import games.r3.actions.DrawAction;
import games.r3.actions.PlayAction;
import games.r3.components.R3Card;
import org.jetbrains.annotations.NotNull;
import utilities.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>The forward model contains all the game rules and logic. It is mainly responsible for declaring rules for:</p>
 * <ol>
 *     <li>Game setup</li>
 *     <li>Actions available to players in a given game state</li>
 *     <li>Game events or rules applied after a player's action</li>
 *     <li>Game end</li>
 * </ol>
 */
public class R3ForwardModel extends StandardForwardModel {

    /**
     * Initializes all variables in the given game state. Performs initial game setup according to game rules, e.g.:
     * <ul>
     *     <li>Sets up decks of cards and shuffles them</li>
     *     <li>Gives player cards</li>
     *     <li>Places tokens on boards</li>
     *     <li>...</li>
     * </ul>
     *
     * @param firstState - the state to be modified to the initial game state.
     */
    @Override
    protected void _setup(AbstractGameState firstState) {
        var s = (R3GameState) firstState;
        var p = (R3Parameters) s.getGameParameters();

        // create decks
        s.drawPile = new Deck<R3Card>("Draw Pile", CoreConstants.VisibilityMode.HIDDEN_TO_ALL);
        s.positionPiles = new ArrayList<>();
        for (int i = 0; i < s.getNPlayers(); i++) {
            var positions = new ArrayList<Deck<R3Card>>();
            for (int j = 0; j < p.positions; j++) {
                positions.add(new Deck<>("Player " + i + " position " + j, CoreConstants.VisibilityMode.VISIBLE_TO_ALL));
            }
            s.positionPiles.add(positions);
        }

        s.hands = new ArrayList<>();
        for (int i = 0; i < s.getNPlayers(); i++) {
            s.hands.add(new Deck<>("Player " + i + " hand", CoreConstants.VisibilityMode.VISIBLE_TO_OWNER));
        }

        // put cards in decks
        s.drawPile.add(p.allCards);
        s.drawPile.shuffle(s.getRnd());

        for (int i = 0; i < s.getNPlayers(); i++) {
            for (int j = 0; j < p.handSize; j++) {
                s.hands.get(i).add(s.drawPile.draw());
            }
        }
    }

    /**
     * Calculates the list of currently available actions, possibly depending on the game phase.
     * @return - List of AbstractAction objects.
     */
    @Override
    protected List<AbstractAction> _computeAvailableActions(AbstractGameState gameState) {
        var s = (R3GameState) gameState;
        var p = (R3Parameters) s.getGameParameters();
        var currentPlayer = s.getCurrentPlayer();

        var actions = new ArrayList<AbstractAction>();

        var emptyPositionDecks = getEmptyPositionDecks(s, currentPlayer);

        var groups = s.hands.get(currentPlayer).stream().collect(Collectors.groupingBy(c -> new Pair<>(c.kind, c.suit)));
        for (var cards : groups.values()) {
            for (int amount = 1; amount <= cards.size(); amount++) {
                var cardsToPlay = new ArrayList<>(cards.subList(0, amount));
                for(var deck : emptyPositionDecks){
                    actions.add(new PlayAction(cardsToPlay, deck));
                }
            }
        }

        if (s.drawPile.getSize() > 0) {
            actions.add(DrawAction.INSTANCE);
        }

        return actions;
    }

    @NotNull
    private static List<Deck<R3Card>> getEmptyPositionDecks(R3GameState s, int currentPlayer) {
        return s.positionPiles.get(currentPlayer).stream()
                .filter(d -> d.getSize() == 0)
                .toList();
    }

    @Override
    protected void _afterAction(AbstractGameState currentState, AbstractAction actionTaken) {
        var s = (R3GameState) currentState;
        var p = (R3Parameters) s.getGameParameters();

        for (int i = 0; i < s.getNPlayers(); i++) {
            if (getEmptyPositionDecks(s, i).size() <= p.positions - p.positionsToFight) {
                s.setGameStatus(CoreConstants.GameResult.GAME_END);
            }
        }

        endPlayerTurn(s);
    }
}
