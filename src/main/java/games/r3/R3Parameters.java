package games.r3;

import core.AbstractParameters;
import core.Game;
import evaluation.optimisation.TunableParameters;
import games.GameType;
import games.r3.components.R3Card;
import utilities.Pair;

import java.util.*;

public class R3Parameters extends TunableParameters {
    public int handSize;

    public int attackRockCopies;
    public int attackPaperCopies;
    public int attackScissorsCopies;
    public int defenseRockCopies;
    public int defensePaperCopies;
    public int defenseScissorsCopies;
    public int rotationChangeCopies;

    public int[] attackRockPoints;
    public int[] attackPaperPoints;
    public int[] attackScissorsPoints;
    public int[] defenseRockPoints;
    public int[] defensePaperPoints;
    public int[] defenseScissorsPoints;

    public int positions;
    public int positionsToFight;

    public int healthPoints;
    public int maxIterations;
    public int heuristicMaxIterations;

    public List<R3Card> allCards;
    public Map<Pair<R3Card.KIND, R3Card.SUIT>,int[]> cardPoints = new HashMap<>();

    public R3Parameters() {
        super();
        addTunableParameter("handSize", 5);
        addTunableParameter("attackRockCopies", 11);
        addTunableParameter("attackPaperCopies", 12);
        addTunableParameter("attackScissorsCopies", 8);
        addTunableParameter("defenseRockCopies", 5);
        addTunableParameter("defensePaperCopies", 7);
        addTunableParameter("defenseScissorsCopies", 4);
        addTunableParameter("rotationChangeCopies", 5);

        addTunableParameter("attackRockPoints", new int[]{0,0,1,5});
        addTunableParameter("attackPaperPoints", new int[]{0,0,1,2});
        addTunableParameter("attackScissorsPoints", new int[]{0,1,2,3});
        addTunableParameter("defenseRockPoints", new int[]{1,2,3,3});
        addTunableParameter("defensePaperPoints", new int[]{0,1,2,3});
        addTunableParameter("defenseScissorsPoints", new int[]{1,3,3,4});

        addTunableParameter("positions", 6);
        addTunableParameter("positionsToFight", 5);
        addTunableParameter("healthPoints", 20);
        // healthPoints * positionsToFight
        addTunableParameter("maxIterations", 100);
        addTunableParameter("heuristicMaxIterations", 10);
    }

    @Override
    public void _reset() {
        handSize = (int) getParameterValue("handSize");
        attackRockCopies = (int) getParameterValue("attackRockCopies");
        attackPaperCopies = (int) getParameterValue("attackPaperCopies");
        attackScissorsCopies = (int) getParameterValue("attackScissorsCopies");
        defenseRockCopies = (int) getParameterValue("defenseRockCopies");
        defensePaperCopies = (int) getParameterValue("defensePaperCopies");
        defenseScissorsCopies = (int) getParameterValue("defenseScissorsCopies");
        rotationChangeCopies = (int) getParameterValue("rotationChangeCopies");
        attackRockPoints = (int[]) getParameterValue("attackRockPoints");
        attackPaperPoints = (int[]) getParameterValue("attackPaperPoints");
        attackScissorsPoints = (int[]) getParameterValue("attackScissorsPoints");
        defenseRockPoints = (int[]) getParameterValue("defenseRockPoints");
        defensePaperPoints = (int[]) getParameterValue("defensePaperPoints");
        defenseScissorsPoints = (int[]) getParameterValue("defenseScissorsPoints");
        positions = (int) getParameterValue("positions");
        positionsToFight = (int) getParameterValue("positionsToFight");
        healthPoints = (int) getParameterValue("healthPoints");
        maxIterations = (int) getParameterValue("maxIterations");
        heuristicMaxIterations = (int) getParameterValue("heuristicMaxIterations");

        allCards = new ArrayList<>();
        for (int i = 0; i < attackRockCopies; i++) {
            allCards.add(new R3Card(R3Card.KIND.ATTACK, R3Card.SUIT.ROCK, i));
        }
        for (int i = 0; i < attackPaperCopies; i++) {
            allCards.add(new R3Card(R3Card.KIND.ATTACK, R3Card.SUIT.PAPER, i));
        }
        for (int i = 0; i < attackScissorsCopies; i++) {
            allCards.add(new R3Card(R3Card.KIND.ATTACK, R3Card.SUIT.SCISSORS, i));
        }
        for (int i = 0; i < defenseRockCopies; i++) {
            allCards.add(new R3Card(R3Card.KIND.DEFENSE, R3Card.SUIT.ROCK, i));
        }
        for (int i = 0; i < defensePaperCopies; i++) {
            allCards.add(new R3Card(R3Card.KIND.DEFENSE, R3Card.SUIT.PAPER, i));
        }
        for (int i = 0; i < defenseScissorsCopies; i++) {
            allCards.add(new R3Card(R3Card.KIND.DEFENSE, R3Card.SUIT.SCISSORS, i));
        }
        for (int i = 0; i < rotationChangeCopies; i++) {
            allCards.add(new R3Card(R3Card.KIND.ROTATION_CHANGE, R3Card.SUIT.NONE, i));
        }

        cardPoints=new HashMap<>();
        cardPoints.put(new Pair<>(R3Card.KIND.ATTACK, R3Card.SUIT.ROCK), attackRockPoints);
        cardPoints.put(new Pair<>(R3Card.KIND.ATTACK, R3Card.SUIT.PAPER), attackPaperPoints);
        cardPoints.put(new Pair<>(R3Card.KIND.ATTACK, R3Card.SUIT.SCISSORS), attackScissorsPoints);
        cardPoints.put(new Pair<>(R3Card.KIND.DEFENSE, R3Card.SUIT.ROCK), defenseRockPoints);
        cardPoints.put(new Pair<>(R3Card.KIND.DEFENSE, R3Card.SUIT.PAPER), defensePaperPoints);
        cardPoints.put(new Pair<>(R3Card.KIND.DEFENSE, R3Card.SUIT.SCISSORS), defenseScissorsPoints);
    }

    @Override
    protected AbstractParameters _copy() {
        var copy = new R3Parameters();

        copy.handSize = handSize;
        copy.attackRockCopies = attackRockCopies;
        copy.attackPaperCopies = attackPaperCopies;
        copy.attackScissorsCopies = attackScissorsCopies;
        copy.defenseRockCopies = defenseRockCopies;
        copy.defensePaperCopies = defensePaperCopies;
        copy.defenseScissorsCopies = defenseScissorsCopies;
        copy.rotationChangeCopies = rotationChangeCopies;
        copy.attackRockPoints = attackRockPoints.clone();
        copy.attackPaperPoints = attackPaperPoints.clone();
        copy.attackScissorsPoints = attackScissorsPoints.clone();
        copy.defenseRockPoints = defenseRockPoints.clone();
        copy.defensePaperPoints = defensePaperPoints.clone();
        copy.defenseScissorsPoints = defenseScissorsPoints.clone();
        copy.positions = positions;
        copy.positionsToFight = positionsToFight;
        copy.healthPoints = healthPoints;
        copy.maxIterations = maxIterations;
        copy.heuristicMaxIterations = heuristicMaxIterations;

        copy.allCards = new ArrayList<>(allCards);
        copy.cardPoints = new HashMap<>(cardPoints);

        return this;
    }

    @Override
    protected boolean _equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        R3Parameters that = (R3Parameters) o;
        return handSize == that.handSize && attackRockCopies == that.attackRockCopies && attackPaperCopies == that.attackPaperCopies && attackScissorsCopies == that.attackScissorsCopies && defenseRockCopies == that.defenseRockCopies && defensePaperCopies == that.defensePaperCopies && defenseScissorsCopies == that.defenseScissorsCopies && rotationChangeCopies == that.rotationChangeCopies && positions == that.positions && positionsToFight == that.positionsToFight && healthPoints == that.healthPoints && Objects.deepEquals(attackRockPoints, that.attackRockPoints) && Objects.deepEquals(attackPaperPoints, that.attackPaperPoints) && Objects.deepEquals(attackScissorsPoints, that.attackScissorsPoints) && Objects.deepEquals(defenseRockPoints, that.defenseRockPoints) && Objects.deepEquals(defensePaperPoints, that.defensePaperPoints) && Objects.deepEquals(defenseScissorsPoints, that.defenseScissorsPoints) && Objects.equals(allCards, that.allCards) && Objects.equals(cardPoints, that.cardPoints) && Objects.equals(maxIterations, that.maxIterations) && Objects.equals(heuristicMaxIterations, that.heuristicMaxIterations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), handSize, attackRockCopies, attackPaperCopies, attackScissorsCopies, defenseRockCopies, defensePaperCopies, defenseScissorsCopies, rotationChangeCopies, Arrays.hashCode(attackRockPoints), Arrays.hashCode(attackPaperPoints), Arrays.hashCode(attackScissorsPoints), Arrays.hashCode(defenseRockPoints), Arrays.hashCode(defensePaperPoints), Arrays.hashCode(defenseScissorsPoints), positions, positionsToFight, healthPoints, allCards, cardPoints, maxIterations, heuristicMaxIterations);
    }

    @Override
    public Object instantiate() {
        return new Game(GameType.R3, new R3ForwardModel(), new R3GameState(this, GameType.R3.getMinPlayers()));
    }
}
