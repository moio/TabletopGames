package games.bamboo;

import core.AbstractGameState;
import core.AbstractParameters;
import core.Game;
import evaluation.optimisation.TunableParameters;
import games.GameType;
import games.bamboo.components.NumberCard;
import games.bamboo.components.Op;
import games.bamboo.components.OperatorCard;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>This class should hold a series of variables representing game parameters (e.g. number of cards dealt to players,
 * maximum number of rounds in the game etc.). These parameters should be used everywhere in the code instead of
 * local variables or hard-coded numbers, by accessing these parameters from the game state via {@link AbstractGameState#getGameParameters()}.</p>
 *
 * <p>It should then implement appropriate {@link #_copy()}, {@link #_equals(Object)} and {@link #hashCode()} functions.</p>
 *
 * <p>The class can optionally extend from {@link TunableParameters} instead, which allows to use
 * automatic game parameter optimisation tools in the framework.</p>
 */
public class BambooParameters extends TunableParameters {
    public int numberHandSize;
    public int operatorHandSize;
    public int objectiveSize;
    public int numberCopies;
    public int minNumberValue;
    public int maxNumberValue;
    public int operatorCopies;
    public List<NumberCard> allNumberCards;
    public List<OperatorCard> allOperatorCards;

    // default constructor is called dynamically, eg. by ForwardModelTest
    public BambooParameters() {
        addTunableParameter("numberHandSize", 3);
        addTunableParameter("operatorHandSize", 3);
        addTunableParameter("objectiveSize", 1);
        addTunableParameter("numberCopies", 5);
        addTunableParameter("minNumberValue", 1);
        addTunableParameter("maxNumberValue", 9);
        addTunableParameter("operatorCopies", 10);

        _reset();
    }

    @Override
    public void _reset() {
        numberHandSize = (int) getParameterValue("numberHandSize");
        operatorHandSize = (int) getParameterValue("operatorHandSize");
        objectiveSize = (int) getParameterValue("objectiveSize");
        numberCopies = (int) getParameterValue("numberCopies");
        minNumberValue = (int) getParameterValue("minNumberValue");
        maxNumberValue = (int) getParameterValue("maxNumberValue");
        operatorCopies = (int) getParameterValue("operatorCopies");

        fillCardSets(this);
    }

    private void fillCardSets(BambooParameters p) {
        p.allNumberCards = new ArrayList<>();
        p.allOperatorCards = new ArrayList<>();
        for (int i = p.minNumberValue; i <= p.maxNumberValue; i++) {
            for (int j = 0; j < p.numberCopies; j++) {
                p.allNumberCards.add(new NumberCard(i, j));
            }
        }

        for (int i = 0; i <= p.operatorCopies; i++) {
            for (var op : Op.values()){
                p.allOperatorCards.add(new OperatorCard(op, i));
            }
        }
    }

    @Override
    protected AbstractParameters _copy() {
        var copy = new BambooParameters();

        copy.numberHandSize = this.numberHandSize;
        copy.operatorHandSize = this.operatorHandSize;
        copy.objectiveSize = this.objectiveSize;
        copy.numberCopies = this.numberCopies;
        copy.minNumberValue = this.minNumberValue;
        copy.maxNumberValue = this.maxNumberValue;
        copy.operatorCopies = this.operatorCopies;
        fillCardSets(copy);

        return copy;
    }

    @Override
    protected boolean _equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BambooParameters that = (BambooParameters) o;
        return numberHandSize == that.numberHandSize && operatorHandSize == that.operatorHandSize && objectiveSize == that.objectiveSize && numberCopies == that.numberCopies && minNumberValue == that.minNumberValue && maxNumberValue == that.maxNumberValue && operatorCopies == that.operatorCopies && Objects.equals(allNumberCards, that.allNumberCards) && Objects.equals(allOperatorCards, that.allOperatorCards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), numberHandSize, operatorHandSize, objectiveSize, numberCopies, minNumberValue, maxNumberValue, operatorCopies, allNumberCards, allOperatorCards);
    }

    @Override
    public Object instantiate() {
        return new Game(GameType.Bamboo, new BambooForwardModel(), new BambooGameState(this, GameType.Bamboo.getMinPlayers()));
    }
}
