package games.totoro;

import core.AbstractGameState;
import core.AbstractParameters;
import core.Game;
import evaluation.optimisation.TunableParameters;
import games.GameType;
import games.totoro.components.TotoroCard;

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
public class TotoroParameters extends TunableParameters {
    public int handSize = 4;
    public int offerSize = 4;
    public int operationSize = 3;
    public int operationsToWin = 4;

    public static final List<TotoroCard> allCards = new ArrayList<>(){{
        add(new TotoroCard(1));
        add(new TotoroCard(2));
        add(new TotoroCard(3));
        add(new TotoroCard(4));
        add(new TotoroCard(5));
        add(new TotoroCard(6));
        add(new TotoroCard(7));
        add(new TotoroCard(8));
        add(new TotoroCard(9));
        add(new TotoroCard(10));
        add(new TotoroCard(11));
        add(new TotoroCard(12));
    }};

    public TotoroParameters(){
        addTunableParameter("handSize", 4);
        addTunableParameter("offerSize", 4);
        addTunableParameter("operationSize", 3);
        addTunableParameter("operationsToWin", 4);
        _reset();
    }

    @Override
    public void _reset() {
        handSize = (int) getParameterValue("handSize");
        offerSize = (int) getParameterValue("offerSize");
        operationSize = (int) getParameterValue("operationSize");
        operationsToWin = (int) getParameterValue("operationsToWin");
    }

    @Override
    protected AbstractParameters _copy() {
        var copy = new TotoroParameters();

        copy.offerSize = this.offerSize;
        copy.handSize = this.handSize;
        copy.operationSize = this.operationSize;
        copy.operationsToWin = this.operationsToWin;

        return copy;
    }

    @Override
    public boolean _equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TotoroParameters that = (TotoroParameters) o;
        return handSize == that.handSize && offerSize == that.offerSize && operationSize == that.operationSize && operationsToWin == that.operationsToWin;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), handSize, offerSize, operationSize, operationsToWin);
    }

    @Override
    public Object instantiate() {
        return new Game(GameType.Totoro, new TotoroForwardModel(), new TotoroGameState(this, 1));
    }
}
