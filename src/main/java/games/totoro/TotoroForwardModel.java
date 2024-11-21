package games.totoro;

import core.AbstractGameState;
import core.CoreConstants;
import core.StandardForwardModel;
import core.actions.AbstractAction;
import core.components.Deck;
import games.totoro.actions.GiveUpAction;
import games.totoro.actions.MulliganAction;
import games.totoro.actions.PlayOperationAction;
import games.totoro.components.TotoroCard;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static java.util.Comparator.comparingInt;

/**
 * <p>The forward model contains all the game rules and logic. It is mainly responsible for declaring rules for:</p>
 * <ol>
 *     <li>Game setup</li>
 *     <li>Actions available to players in a given game state</li>
 *     <li>Game events or rules applied after a player's action</li>
 *     <li>Game end</li>
 * </ol>
 */
public class TotoroForwardModel extends StandardForwardModel {

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
        var s = (TotoroGameState) firstState;
        var p = (TotoroParameters) s.getGameParameters();

        s.drawPile = new Deck<>("Draw Pile", CoreConstants.VisibilityMode.HIDDEN_TO_ALL);
        s.drawPile.add(TotoroParameters.allCards);
        s.drawPile.shuffle(s.getRnd());

        s.hand = new Deck<>("Hand", CoreConstants.VisibilityMode.VISIBLE_TO_ALL);
        for (int i = 0; i < p.handSize; i++) {
            s.hand.add(s.drawPile.draw());
        }

        s.offer = new Deck<>("Offer", CoreConstants.VisibilityMode.VISIBLE_TO_ALL);
        for (int i = 0; i < p.offerSize; i++) {
            s.offer.add(s.drawPile.draw());
        }

        s.operations = new ArrayList<>();
    }

    /**
     * Calculates the list of currently available actions, possibly depending on the game phase.
     * @return - List of AbstractAction objects.
     */
    @Override
    protected List<AbstractAction> _computeAvailableActions(AbstractGameState gameState) {
        var result = new ArrayList<AbstractAction>();

        var s = (TotoroGameState) gameState;
        var p = (TotoroParameters) s.getGameParameters();

        var availableCards = new ArrayList<TotoroCard>();
        availableCards.addAll(s.hand.getComponents());
        availableCards.addAll(s.offer.getComponents());

        var combinations = combinations(availableCards, p.operationSize);

        for (var combination: combinations) {
            var sc = combination.stream().sorted(comparingInt(a -> a.value)).toList();

            // is the sum of all but the biggest element equal to the biggest element?
            var sum = 0;
            for (int i = 0; i < sc.size()-1; i++) {
                sum += sc.get(i).value;
            }

            if (sum == sc.get(sc.size()-1).value) {
                result.add(new PlayOperationAction(sc));
            }
        }

        result.sort(comparingInt(a -> countIn(((PlayOperationAction)a).operandIDs, s, s.hand)));

        // giving up always possible
        result.add(GiveUpAction.INSTANCE);

        // mulligan only on first hand
        if (s.getHistory().isEmpty()){
            result.add(MulliganAction.INSTANCE);
        }

        return result;
    }

    @NotNull
    private static Integer countIn(List<Integer> ids, TotoroGameState s, Deck<TotoroCard> d) {
        return ids.stream().map(i -> d.contains((TotoroCard) s.getComponentById(i)) ? 1 : 0).reduce(0, Integer::sum);
    }


    public static <T> List<List<T>> combinations(List<T> inputSet, int k) {
        List<List<T>> results = new ArrayList<>();
        combinationsInternal(inputSet, k, results, new ArrayList<T>(), 0);
        return results;
    }

    private static <T> void combinationsInternal(
            List<T> inputSet, int k, List<List<T>> results, ArrayList<T> accumulator, int index) {
        int needToAccumulate = k - accumulator.size();
        int canAcculumate = inputSet.size() - index;

        if (accumulator.size() == k) {
            results.add(new ArrayList<>(accumulator));
        } else if (needToAccumulate <= canAcculumate) {
            combinationsInternal(inputSet, k, results, accumulator, index + 1);
            accumulator.add(inputSet.get(index));
            combinationsInternal(inputSet, k, results, accumulator, index + 1);
            accumulator.remove(accumulator.size() - 1);
        }
    }

    @Override
    protected void _afterAction(AbstractGameState currentState, AbstractAction actionTaken) {
        var s = (TotoroGameState) currentState;
        var p = (TotoroParameters) s.getGameParameters();

        // check winning condition and set winner
        if (s.operations.size() >= p.operationsToWin) {
            s.setGameStatus(CoreConstants.GameResult.GAME_END);
            s.setPlayerResult(CoreConstants.GameResult.WIN_GAME, 0);
        }
        // check losing condition and set loser
        else if (actionTaken == GiveUpAction.INSTANCE) {
            s.setGameStatus(CoreConstants.GameResult.GAME_END);
            s.setPlayerResult(CoreConstants.GameResult.LOSE_GAME, 0);
        }
        else {
            endPlayerTurn(currentState, 0);
        }

        super._afterAction(currentState, actionTaken);
    }

    @Override
    protected void endGame(AbstractGameState gs) {
        // this game only has insta-win or insta-lose, no points are calculated. Nothing to do
    }
}
