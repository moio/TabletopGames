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
    public int minObjectiveValue;
    public int maxObjectiveValue;
    public int numberCopies;
    public int minNumberValue;
    public int maxNumberValue;
    public int operatorCopies;
    public List<NumberCard> allNumberCards;
    public List<OperatorCard> allOperatorCards;
    public List<NumberCard> allObjectiveCards;

    // default constructor is called dynamically, eg. by ForwardModelTest
    public BambooParameters() {
        addTunableParameter("numberHandSize", 3);
        addTunableParameter("operatorHandSize", 3);
        addTunableParameter("minObjectiveValue", 1);
        addTunableParameter("maxObjectiveValue", 9);
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
        minObjectiveValue = (int) getParameterValue("minObjectiveValue");
        maxObjectiveValue = (int) getParameterValue("maxObjectiveValue");
        numberCopies = (int) getParameterValue("numberCopies");
        minNumberValue = (int) getParameterValue("minNumberValue");
        maxNumberValue = (int) getParameterValue("maxNumberValue");
        operatorCopies = (int) getParameterValue("operatorCopies");

        fillCardSets(this);

        System.out.println(this.getJSONDescription());
    }

    private void fillCardSets(BambooParameters p) {
        p.allNumberCards = new ArrayList<>();
        p.allOperatorCards = new ArrayList<>();
        p.allObjectiveCards = new ArrayList<>();
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

        for (int i = p.minObjectiveValue; i <= p.maxObjectiveValue; i++) {
            p.allObjectiveCards.add(new NumberCard(i, 0));
        }
    }

    @Override
    protected AbstractParameters _copy() {
        var copy = new BambooParameters();

        copy.numberHandSize = this.numberHandSize;
        copy.operatorHandSize = this.operatorHandSize;
        copy.minObjectiveValue = this.minObjectiveValue;
        copy.maxObjectiveValue = this.maxObjectiveValue;
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
        return numberHandSize == that.numberHandSize && operatorHandSize == that.operatorHandSize && minObjectiveValue == that.minObjectiveValue && maxObjectiveValue == that.maxObjectiveValue && numberCopies == that.numberCopies && minNumberValue == that.minNumberValue && maxNumberValue == that.maxNumberValue && operatorCopies == that.operatorCopies && Objects.equals(allNumberCards, that.allNumberCards) && Objects.equals(allOperatorCards, that.allOperatorCards) && Objects.equals(allObjectiveCards, that.allObjectiveCards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), numberHandSize, operatorHandSize, minObjectiveValue, maxObjectiveValue, numberCopies, minNumberValue, maxNumberValue, operatorCopies, allNumberCards, allOperatorCards, allObjectiveCards);
    }

    @Override
    public Object instantiate() {
        return new Game(GameType.Bamboo, new BambooForwardModel(), new BambooGameState(this, GameType.Bamboo.getMinPlayers()));
    }
}
