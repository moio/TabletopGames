package games.bamboo;

import core.AbstractGameState;
import core.CoreConstants;
import core.StandardForwardModel;
import core.actions.AbstractAction;
import core.components.Component;
import core.components.Deck;
import games.bamboo.actions.PassAction;
import games.bamboo.actions.ResolveAction;
import games.bamboo.components.NumberCard;
import games.bamboo.components.Op;
import games.bamboo.components.OperatorCard;
import games.totoro.TotoroGameState;
import games.totoro.TotoroParameters;
import games.totoro.actions.GiveUpAction;
import games.totoro.components.TotoroCard;

import java.util.*;

import org.apache.commons.collections4.iterators.PermutationIterator;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.jetbrains.annotations.NotNull;

/**
 * <p>The forward model contains all the game rules and logic. It is mainly responsible for declaring rules for:</p>
 * <ol>
 *     <li>Game setup</li>
 *     <li>Actions available to players in a given game state</li>
 *     <li>Game events or rules applied after a player's action</li>
 *     <li>Game end</li>
 * </ol>
 */
public class BambooForwardModel extends StandardForwardModel {

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
        var s = (BambooGameState) firstState;
        var p = (BambooParameters) s.getGameParameters();

        // create decks
        s.numberDrawPile = new Deck<NumberCard>("Number Draw Pile", CoreConstants.VisibilityMode.HIDDEN_TO_ALL);
        s.operatorDrawPile = new Deck<OperatorCard>("Operator Draw Pile", CoreConstants.VisibilityMode.HIDDEN_TO_ALL);
        s.objectiveDrawPile = new Deck<NumberCard>("Objective Draw Pile", CoreConstants.VisibilityMode.HIDDEN_TO_ALL);
        s.objective = new Deck<NumberCard>("Objective", CoreConstants.VisibilityMode.VISIBLE_TO_ALL);

        s.numberHands = new ArrayList<>();
        s.operatorHands = new ArrayList<>();
        s.numberWonPiles = new ArrayList<>();
        for (int i = 0; i < s.getNPlayers(); i++) {
            s.numberHands.add(new Deck<NumberCard>("Player " + i + " number hand", i, CoreConstants.VisibilityMode.VISIBLE_TO_OWNER));
            s.operatorHands.add(new Deck<OperatorCard>("Player " + i + " operator hand", i, CoreConstants.VisibilityMode.VISIBLE_TO_OWNER));
            s.numberWonPiles.add(new Deck<NumberCard>("Player " + i + " won", CoreConstants.VisibilityMode.VISIBLE_TO_ALL));
        }

        // put cards in decks
        s.numberDrawPile.add(p.allNumberCards);
        s.numberDrawPile.shuffle(s.getRnd());

        s.operatorDrawPile.add(p.allOperatorCards);
        s.operatorDrawPile.shuffle(s.getRnd());

        s.objectiveDrawPile.add(p.allObjectiveCards);
        s.objectiveDrawPile.shuffle(s.getRnd());

        // discover the objective
        s.objective.add(s.objectiveDrawPile.draw());

        for (int i = 0; i < s.getNPlayers(); i++) {
            for (int j = 0; j < p.numberHandSize; j++) {
                s.numberHands.get(i).add(s.numberDrawPile.draw());
            }
            for (int j = 0; j < p.operatorHandSize; j++) {
                s.operatorHands.get(i).add(s.operatorDrawPile.draw());
            }
        }
    }

    /**
     * Calculates the list of currently available actions, possibly depending on the game phase.
     * @return - List of AbstractAction objects.
     */
    @Override
    protected List<AbstractAction> _computeAvailableActions(AbstractGameState gameState) {
        List<AbstractAction> actions = new ArrayList<>();
        var s = (BambooGameState) gameState;
        var p = (BambooParameters) s.getGameParameters();

        var playerId = s.getCurrentPlayer();
        var operatorHand = s.operatorHands.get(playerId).stream().toList();
        var numberHand = s.numberHands.get(playerId).stream().toList();
        var maxOperators = Math.min(p.operatorHandSize, numberHand.size()-1);



        for (int nOperators = 1; nOperators <= maxOperators; nOperators++) {
            var operatorCombinations = new PermutationIteratorPlus<OperatorCard>(operatorHand, nOperators);

            while (operatorCombinations.hasNext()) {
                var operatorCombination = operatorCombinations.next();
                var numberCombinations = new PermutationIteratorPlus<NumberCard>(numberHand, operatorCombination.size()+1);

                while (numberCombinations.hasNext()) {
                    var numberCombination = numberCombinations.next();
                    int result = numberCombination.get(0).value;

                    for (int i = 0; i < operatorCombination.size(); i++) {
                        result = apply(result, numberCombination.get(i+1).value, operatorCombination.get(i));
                    }

                    if (result == s.objective.peek().value) {
                        actions.add(new ResolveAction(
                                numberCombination,
                                operatorCombination,
                                s.objective.peek()
                        ));
                    }
                }
            }
        }

        actions.add(new PassAction(1, 1));
        actions.add(new PassAction(2, 0));
        actions.add(new PassAction(0, 2));
        actions.add(new PassAction(0, 0));

        return actions;
    }

    public class PermutationIteratorPlus <T> implements Iterator<List<T>> {

        private List<T> l;
        private Iterator<int[]> i;
        private PermutationIterator<T> j = null;

        PermutationIteratorPlus(List<T> l, int k){
            this.i = CombinatoricsUtils.combinationsIterator(l.size(), k);
            this.l = l;

            advance();
        }

        private void advance() {
            if (i.hasNext()) {
                var indices = i.next();
                var m = new ArrayList<T>();
                for (var index : indices) {
                    m.add(l.get(index));
                }
                this.j = new PermutationIterator<>(m);
            }
            else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public boolean hasNext() {
            return i.hasNext() || j.hasNext();
        }

        @Override
        public List<T> next() {
            if (j.hasNext()){
                return j.next();
            }
            advance();
            return this.next();
        }
    }

    public int apply(int n, int m, OperatorCard o){
        if (o.op == Op.PLUS) {
            return n+m;
        }
        else if (o.op == Op.MINUS) {
            return n-m;
        }
        else {
            throw new RuntimeException("unexpected op");
        }
    }

    @Override
    protected void _afterAction(AbstractGameState currentState, AbstractAction actionTaken) {
        var s = (BambooGameState) currentState;

        if (s.numberDrawPile.getSize() == 0 || s.objectiveDrawPile.getSize() == 0) {
            s.setGameStatus(CoreConstants.GameResult.GAME_END);
        }

        endPlayerTurn(s);
    }
}
