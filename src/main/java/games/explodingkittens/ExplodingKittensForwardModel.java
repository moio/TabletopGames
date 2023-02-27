package games.explodingkittens;

import core.AbstractForwardModel;
import core.AbstractGameState;
import core.CoreConstants;
import core.CoreConstants.VisibilityMode;
import core.actions.AbstractAction;
import core.components.Deck;
import core.components.PartialObservableDeck;
import core.interfaces.IOrderedActionSpace;
import games.GameType;
import games.explodingkittens.actions.*;
import games.explodingkittens.actions.reactions.ChooseSeeTheFutureOrder;
import games.explodingkittens.actions.reactions.GiveCard;
import games.explodingkittens.actions.reactions.PassAction;
import games.explodingkittens.actions.reactions.PlaceExplodingKitten;
import games.explodingkittens.cards.ExplodingKittensCard;
import utilities.ActionTreeNode;

import java.util.*;

import static games.explodingkittens.ExplodingKittensGameState.ExplodingKittensGamePhase.Nope;
import static utilities.Utils.generatePermutations;

public class ExplodingKittensForwardModel extends AbstractForwardModel implements IOrderedActionSpace {
    private final ArrayList<String> cardTypes = new ArrayList<>(Arrays.asList("EXPLODING_KITTEN", "DEFUSE", "NOPE", "ATTACK", "SKIP", "FAVOR",
            "SHUFFLE", "SEETHEFUTURE", "TACOCAT", "MELONCAT", "FURRYCAT", "BEARDCAT", "RAINBOWCAT"));
    private final int N_CARDS_TO_CHECK = 10;

    /**
     * Performs initial game setup according to game rules.
     * @param firstState - the state to be modified to the initial game state.
     */
    protected void _setup(AbstractGameState firstState) {
        root = generateActionTree();
        Random rnd = new Random(firstState.getGameParameters().getRandomSeed());

        ExplodingKittensGameState ekgs = (ExplodingKittensGameState)firstState;
        ExplodingKittensParameters ekp = (ExplodingKittensParameters)firstState.getGameParameters();
        ekgs.playerHandCards = new ArrayList<>();
        ekgs.playerGettingAFavor = -1;
        ekgs.actionStack = null;
        // Set up draw pile deck
        PartialObservableDeck<ExplodingKittensCard> drawPile = new PartialObservableDeck<>("Draw Pile", firstState.getNPlayers());
        ekgs.setDrawPile(drawPile);

        // Add all cards but defuse and exploding kittens
        for (HashMap.Entry<ExplodingKittensCard.CardType, Integer> entry : ekp.cardCounts.entrySet()) {
            if (entry.getKey() == ExplodingKittensCard.CardType.DEFUSE || entry.getKey() == ExplodingKittensCard.CardType.EXPLODING_KITTEN)
                continue;
            for (int i = 0; i < entry.getValue(); i++) {
                ExplodingKittensCard card = new ExplodingKittensCard(entry.getKey());
                drawPile.add(card);
            }
        }
        ekgs.getDrawPile().shuffle(rnd);

        // Set up player hands
        List<PartialObservableDeck<ExplodingKittensCard>> playerHandCards = new ArrayList<>(firstState.getNPlayers());
        for (int i = 0; i < firstState.getNPlayers(); i++) {
            boolean[] visible = new boolean[firstState.getNPlayers()];
            visible[i] = true;
            PartialObservableDeck<ExplodingKittensCard> playerCards = new PartialObservableDeck<>("Player Cards", visible);
            playerHandCards.add(playerCards);

            // Add defuse card
            ExplodingKittensCard defuse =  new ExplodingKittensCard(ExplodingKittensCard.CardType.DEFUSE);
            defuse.setOwnerId(i);
            playerCards.add(defuse);

            // Add N random cards from the deck
            for (int j = 0; j < ekp.nCardsPerPlayer; j++) {
                ExplodingKittensCard c = ekgs.getDrawPile().draw();
                c.setOwnerId(i);
                playerCards.add(c);
            }
        }
        ekgs.setPlayerHandCards(playerHandCards);
        ekgs.setDiscardPile(new Deck<>("Discard Pile", VisibilityMode.VISIBLE_TO_ALL));

        // Add remaining defuse cards and exploding kitten cards to the deck and shuffle again
        for (int i = ekgs.getNPlayers(); i < ekp.nDefuseCards; i++){
            ExplodingKittensCard defuse = new ExplodingKittensCard(ExplodingKittensCard.CardType.DEFUSE);
            drawPile.add(defuse);
        }
        for (int i = 0; i < ekgs.getNPlayers() + ekp.cardCounts.get(ExplodingKittensCard.CardType.EXPLODING_KITTEN); i++){
            ExplodingKittensCard explodingKitten = new ExplodingKittensCard(ExplodingKittensCard.CardType.EXPLODING_KITTEN);
            drawPile.add(explodingKitten);
        }
        drawPile.shuffle(rnd);

        ekgs.setActionStack(new Stack<>());
        ekgs.orderOfPlayerDeath = new int[ekgs.getNPlayers()];
        ekgs.setGamePhase(CoreConstants.DefaultGamePhase.Main);
    }

    /**
     * Applies the given action to the game state and executes any other game rules.
     * @param gameState - current game state, to be modified by the action.
     * @param action - action requested to be played by a player.
     */
    @Override
    protected void _next(AbstractGameState gameState, AbstractAction action) {
        ExplodingKittensGameState ekgs = (ExplodingKittensGameState) gameState;
        ExplodingKittensTurnOrder ekTurnOrder = (ExplodingKittensTurnOrder) ekgs.getTurnOrder();
        Stack<AbstractAction> actionStack = ekgs.getActionStack();

        if (action instanceof IsNopeable) {
            actionStack.add(action);
            ((IsNopeable) action).actionPlayed(ekgs);

            ekTurnOrder.registerNopeableActionByPlayer(ekgs, ekTurnOrder.getCurrentPlayer(ekgs));
            if (action instanceof NopeAction) {
                // Nope cards added immediately to avoid infinite nopeage
                action.execute(ekgs);
            } else {
                if (ekTurnOrder.reactionsFinished()){
                    action.execute(ekgs);
                    actionStack.clear();
                }
            }
        } else if (action instanceof PassAction) {

            ekTurnOrder.endPlayerTurnStep(gameState);

            if (ekTurnOrder.reactionsFinished()) {
                // apply stack
                if (actionStack.size()%2 == 0){
                    while (actionStack.size() > 1) {
                        actionStack.pop();
                    }
                    //Action was successfully noped
                    ((IsNopeable) actionStack.pop()).nopedExecute(gameState);
//                    if (gameState.getCoreGameParameters().verbose) {
//                        System.out.println("Action was successfully noped");
//                    }
                } else {
//                    if (actionStack.size() > 2 && gameState.getCoreGameParameters().verbose) {
//                        System.out.println("All nopes were noped");
//                    }

                    while (actionStack.size() > 1) {
                        actionStack.pop();
                    }

                    //Action can be played
                    AbstractAction stackedAction = actionStack.get(0);
                    stackedAction.execute(gameState);
                }
                actionStack.clear();
                if (ekgs.getGamePhase() == Nope) {
                    ekgs.setGamePhase(CoreConstants.DefaultGamePhase.Main);
                }
            }
        } else {
            action.execute(gameState);
        }
    }

    /**
     * Calculates the list of currently available actions, possibly depending on the game phase.
     * @return - List of AbstractAction objects.
     */
    @Override
    protected List<AbstractAction> _computeAvailableActions(AbstractGameState gameState) {
        root.resetTree();
        ExplodingKittensGameState ekgs = (ExplodingKittensGameState) gameState;
        ArrayList<AbstractAction> actions;

        // The actions per player do not change a lot in between two turns
        // Could update an existing list instead of generating a new list every time we query this function

        // Find actions for the player depending on current game phase
        int player = ekgs.getCurrentPlayer();
        if (CoreConstants.DefaultGamePhase.Main.equals(ekgs.getGamePhase())) {
            actions = playerActions(ekgs, player);
        } else if (ExplodingKittensGameState.ExplodingKittensGamePhase.Defuse.equals(ekgs.getGamePhase())) {
            actions = placeKittenActions(ekgs, player);
        } else if (ExplodingKittensGameState.ExplodingKittensGamePhase.Nope.equals(ekgs.getGamePhase())) {
            actions = nopeActions(ekgs, player);
        } else if (ExplodingKittensGameState.ExplodingKittensGamePhase.Favor.equals(ekgs.getGamePhase())) {
            actions = favorActions(ekgs, player);
        } else if (ExplodingKittensGameState.ExplodingKittensGamePhase.SeeTheFuture.equals(ekgs.getGamePhase())) {
            actions = seeTheFutureActions(ekgs, player);
        } else {
            actions = new ArrayList<>();
        }

        return actions;
    }

    @Override
    protected AbstractForwardModel _copy() {
        return new ExplodingKittensForwardModel();
    }

    @Override
    protected void endPlayerTurn(AbstractGameState state) {
        ExplodingKittensGameState ekgs = (ExplodingKittensGameState) state;
        ekgs.getTurnOrder().endPlayerTurn(ekgs);
    }

    private ArrayList<AbstractAction> playerActions(ExplodingKittensGameState ekgs, int playerID){
        ArrayList<AbstractAction> actions = new ArrayList<>();
        Deck<ExplodingKittensCard> playerDeck = ekgs.playerHandCards.get(playerID);
        ActionTreeNode playNode = root.findChildrenByName("PLAY");

        HashSet<ExplodingKittensCard.CardType> types = new HashSet<>();
        for (int c = 0; c < playerDeck.getSize(); c++) {
            ExplodingKittensCard card = playerDeck.get(c);
            if (types.contains(card.cardType)) continue;
            types.add(card.cardType);

            switch (card.cardType) {
                case DEFUSE:
                case MELONCAT:
                case RAINBOWCAT:
                case FURRYCAT:
                case BEARDCAT:
                case TACOCAT:
                case NOPE:
                case EXPLODING_KITTEN:
                    break;
                case SKIP:
                    playNode.setValue(1);
                    actions.add(new SkipAction(playerDeck.getComponentID(), ekgs.discardPile.getComponentID(), c));
                    playNode.findChildrenByName("SKIP").setAction(new SkipAction(playerDeck.getComponentID(), ekgs.discardPile.getComponentID(), c));
                    break;
                case FAVOR:
                    for (int player = 0; player < ekgs.getNPlayers(); player++) {
                        if (player == playerID)
                            continue;
                        if (ekgs.playerHandCards.get(player).getSize() > 0) {
                            actions.add(new FavorAction(playerDeck.getComponentID(), ekgs.discardPile.getComponentID(), c, player));
                            playNode.setValue(1);
                            playNode.findChildrenByName("FAVOR").setAction(new FavorAction(playerDeck.getComponentID(), ekgs.discardPile.getComponentID(), c, player));
                        }
                    }
                    break;
                case ATTACK:
                    for (int targetPlayer = 0; targetPlayer < ekgs.getNPlayers(); targetPlayer++) {

                        if (targetPlayer == playerID || ekgs.getPlayerResults()[targetPlayer] != CoreConstants.GameResult.GAME_ONGOING)
                            continue;

                        actions.add(new AttackAction(playerDeck.getComponentID(), ekgs.discardPile.getComponentID(), c, targetPlayer));
                        playNode.setValue(1);
                        playNode.findChildrenByName("ATTACK").setAction(new AttackAction(playerDeck.getComponentID(), ekgs.discardPile.getComponentID(), c, targetPlayer));
                    }
                    break;
                case SHUFFLE:
                    actions.add(new ShuffleAction(playerDeck.getComponentID(), ekgs.discardPile.getComponentID(), c));
                    playNode.setValue(1);
                    playNode.findChildrenByName("SHUFFLE").setAction(new ShuffleAction(playerDeck.getComponentID(), ekgs.discardPile.getComponentID(), c));
                    break;
                case SEETHEFUTURE:
                    actions.add(new SeeTheFuture(playerDeck.getComponentID(), ekgs.discardPile.getComponentID(), c, playerID));
                    playNode.setValue(1);
                    playNode.findChildrenByName("SEETHEFUTURE").setAction(new SeeTheFuture(playerDeck.getComponentID(), ekgs.discardPile.getComponentID(), c, playerID));
                    break;
                default:
                    System.out.println("No actions known for cardtype: " + card.cardType);
            }
        }
        /* todo add special combos
        // can take any card from anyone
        for (int i = 0; i < nPlayers; i++){
            if (i != activePlayer){
                Deck otherDeck = (Deck)this.areas.get(activePlayer).getComponent(playerHandHash);
                for (Card card: otherDeck.getCards()){
                    core.actions.add(new TakeCard(card, i));
                }
            }
        }*/

        // add end turn by drawing a card
        actions.add(new DrawExplodingKittenCard(ekgs.drawPile.getComponentID(), playerDeck.getComponentID()));
        root.findChildrenByName("DRAW").setAction(new DrawExplodingKittenCard(ekgs.drawPile.getComponentID(), playerDeck.getComponentID()));
        return actions;
    }

    private ArrayList<AbstractAction> placeKittenActions(ExplodingKittensGameState ekgs, int playerID){
        ArrayList<AbstractAction> actions = new ArrayList<>();
        Deck<ExplodingKittensCard> playerDeck = ekgs.playerHandCards.get(playerID);
        int explodingKittenCard = -1;
        for (int i = 0; i < playerDeck.getSize(); i++) {
            if (playerDeck.getComponents().get(i).cardType == ExplodingKittensCard.CardType.EXPLODING_KITTEN) {
                explodingKittenCard = i;
                break;
            }
        }
        if (explodingKittenCard != -1) {
            for (int i = 0; i <= ekgs.drawPile.getSize(); i++) {
                actions.add(new PlaceExplodingKitten(playerDeck.getComponentID(), ekgs.drawPile.getComponentID(), explodingKittenCard, i));
                if (i < N_CARDS_TO_CHECK){
                    root.findChildrenByName("defuse").findChildrenByName("put" + i, true).setAction(new PlaceExplodingKitten(playerDeck.getComponentID(), ekgs.drawPile.getComponentID(), explodingKittenCard, i));
                }
            }
        }
        return actions;
    }

    private ArrayList<AbstractAction> nopeActions(ExplodingKittensGameState ekgs, int playerID){
        ArrayList<AbstractAction> actions = new ArrayList<>();
        Deck<ExplodingKittensCard> playerDeck = ekgs.playerHandCards.get(playerID);
        for (int c = 0; c < playerDeck.getSize(); c++) {
            if (playerDeck.getComponents().get(c).cardType == ExplodingKittensCard.CardType.NOPE) {
                actions.add(new NopeAction(playerDeck.getComponentID(), ekgs.discardPile.getComponentID(), c));
                root.findChildrenByName("nope").setAction(new NopeAction(playerDeck.getComponentID(), ekgs.discardPile.getComponentID(), c));
                break;
            }
        }
        actions.add(new PassAction());
        return actions;
    }

    private ArrayList<AbstractAction> favorActions(ExplodingKittensGameState ekgs, int playerID){
        ArrayList<AbstractAction> actions = new ArrayList<>();
        Deck<ExplodingKittensCard> playerDeck = ekgs.playerHandCards.get(playerID);
        Deck<ExplodingKittensCard> receiverDeck = ekgs.playerHandCards.get(ekgs.playerGettingAFavor);
        // todo this requires mapping the card type to indices
        for (int card = 0; card < playerDeck.getSize(); card++) {
            actions.add(new GiveCard(playerDeck.getComponentID(), receiverDeck.getComponentID(), card));
        }
        if (actions.isEmpty()) // the target has no cards.
            actions.add(new GiveCard(playerDeck.getComponentID(), receiverDeck.getComponentID(), -1));
        return actions;
    }

    private ArrayList<AbstractAction> seeTheFutureActions(ExplodingKittensGameState ekgs, int playerID){
        ArrayList<AbstractAction> actions = new ArrayList<>();
        Deck<ExplodingKittensCard> playerDeck = ekgs.playerHandCards.get(playerID);

        int cardIdx = -1;
        for (int c = 0; c < playerDeck.getSize(); c++) {
            if (playerDeck.get(c).cardType == ExplodingKittensCard.CardType.SEETHEFUTURE) {
                cardIdx = c;
                break;
            }
        }

        if (cardIdx != -1) {
            int numberOfCards = ekgs.drawPile.getSize();
            int n = Math.min(((ExplodingKittensParameters) ekgs.getGameParameters()).nSeeFutureCards, numberOfCards);
            if (n > 0) {

                ArrayList<int[]> permutations = new ArrayList<>();
                int[] order = new int[n];
                for (int i = 0; i < n; i++) {
                    order[i] = i;
                }
                generatePermutations(n, order, permutations);
                for (int[] perm : permutations) {
                    actions.add(new ChooseSeeTheFutureOrder(playerDeck.getComponentID(),
                            ekgs.discardPile.getComponentID(), cardIdx, ekgs.drawPile.getComponentID(), perm));
                }
            }
        } else {
            System.out.println("ERROR: Player doesn't have see the future card");
        }

        return actions;
    }
    
    public ActionTreeNode generateActionTree(){
        // set up the action tree
        ActionTreeNode tree = new ActionTreeNode(0, "root");
        tree.addChild(0, "DRAW");
        ActionTreeNode play = tree.addChild(0, "PLAY");
        ActionTreeNode favor = tree.addChild(0, "FAVOR");
        ActionTreeNode nope = tree.addChild(0, "NOPE");
        ActionTreeNode defuse = tree.addChild(0, "DEFUSE");

        for (String cardType : cardTypes) {
            ActionTreeNode cardTypeNode = play.addChild(0, cardType);
            // can ask a favor from any other player
            if (cardType.equals("FAVOR")) {
                for (int j = 0; j < GameType.ExplodingKittens.getMaxPlayers(); j++) {
                    cardTypeNode.addChild(0, "PLAYER" + j);
                }
            }
            favor.addChild(0, cardType);
        }
        // if player has a nope card it can decide if it wants to play it or not
        nope.addChild(0, "NOPE");
        nope.addChild(0, "PASS");
        
        // allow to put the back the defuse card to these positions
        for (int i =0; i < N_CARDS_TO_CHECK; i++){
            defuse.addChild(0, "PUT"+i);

        }
        return tree;
    }

    @Override
    public int getActionSpace() {
        return root.getSubNodes(); // pass (draw) or play any of the card types
    }

    @Override
    public int[] getFixedActionSpace() {
        return new int[getActionSpace()];
    }

    @Override
    public int[] getActionMask(AbstractGameState gameState) {
        return root.getActionMask();
    }

    @Override
    public void nextPython(AbstractGameState state, int actionID) {
        AbstractAction action = root.getActionByVector(new int[]{actionID});
        _next(state, action);
        // todo chooses some actions randomly - could fix this with the multi-level trees
        // TODO the 2 of a kind cards are seemingly not implemented
        // TODO these could be saved somewhere, this is useful for reference
//        ArrayList<String> cardTypes = new ArrayList<>(Arrays.asList("EXPLODING_KITTEN", "DEFUSE", "NOPE", "ATTACK", "SKIP", "FAVOR",
//                "SHUFFLE", "SEETHEFUTURE", "TACOCAT", "MELONCAT", "FURRYCAT", "BEARDCAT", "RAINBOWCAT"));
//        Random rand = new Random();
//        ExplodingKittensGameState ekgs = (ExplodingKittensGameState)state;
//        int player = ekgs.getCurrentPlayer();
//        Deck<ExplodingKittensCard> playerDeck = ekgs.playerHandCards.get(player);
//
//        ArrayList<AbstractAction> actions = playerActions(ekgs, player);
//
//        // todo check if we need all this or if we even need to compute all these?
//        // todo depending on the gamephase we may have different actions, these may be handled using the mask
//        if (CoreConstants.DefaultGamePhase.Main.equals(ekgs.getGamePhase())) {
//            actions = playerActions(ekgs, player);
//        } else if (ExplodingKittensGameState.ExplodingKittensGamePhase.Defuse.equals(ekgs.getGamePhase())) {
//            // here the player just picks an index ,which may be any of the other action IDs
//            actions = placeKittenActions(ekgs, player);
//            _next(state, actions.get(rand.nextInt(actions.size())));
//            return;
//        } else if (ExplodingKittensGameState.ExplodingKittensGamePhase.Nope.equals(ekgs.getGamePhase())) {
//            actions = nopeActions(ekgs, player);
//        } else if (ExplodingKittensGameState.ExplodingKittensGamePhase.Favor.equals(ekgs.getGamePhase())) {
//            actions = favorActions(ekgs, player);
//            // find our selected card type to match the generated actions
//            // todo: getFromIndex may be 0 if our hand is empty, which may happen
//            for (AbstractAction favorAction: actions){
//                ExplodingKittensCard c = playerDeck.get(((GiveCard)favorAction).getFromIndex());
//                if (c.toString().equals(cardTypes.get(actionID))){
//                    _next(state, favorAction);
//                    return;
//                }
//            }
//            System.out.println("WE have a problem with not finding the chosen card to return as a favor");
//
//        } else if (ExplodingKittensGameState.ExplodingKittensGamePhase.SeeTheFuture.equals(ekgs.getGamePhase())) {
//            actions = seeTheFutureActions(ekgs, player);
//            _next(state, actions.get(rand.nextInt(actions.size())));
//            return;
//        }
//
//        Class actionClass;
//        switch (actionID){
//            case 0:
//                actionClass = DrawExplodingKittenCard.class; // pass/draw
//                break;
//            case 1:
//                actionClass = PlaceExplodingKitten.class; // defuse
//                break;
//            case 2:
//                actionClass = NopeAction.class;
//                break;
//            case 3:
//                actionClass = AttackAction.class;
//                break;
//            case 4:
//                actionClass = SkipAction.class;
//                break;
//            case 5:
//                actionClass = FavorAction.class;
//                break;
//            case 6:
//                actionClass = ShuffleAction.class;
//                break;
//            case 7:
//                actionClass = SeeTheFuture.class;
//                break;
//            default:
//                // todo default should be something else, but not all cards are implemented
//                actionClass = DrawExplodingKittenCard.class;
//        }
//        try{
//        switch (actionID) {
//            case 0:
//                if (!CoreConstants.DefaultGamePhase.Main.equals(ekgs.getGamePhase())) {
//                    // pass (situational, i.e: pass using nope)
//                    _next(state, new PassAction());
//                    return;
//                } else {
//                    // draw card
//                    _next(state, new DrawExplodingKittenCard(ekgs.drawPile.getComponentID(), playerDeck.getComponentID()));
//                    return;
//                }
//            case 1: // defuse
//                actions = placeKittenActions(ekgs, player);
//                _next(state, actions.get(rand.nextInt(actions.size())));
//                return;
//            case 2: // nope
//                actions = nopeActions(ekgs, player);
//                _next(state, actions.get(rand.nextInt(actions.size())));
//                return;
//            default:
//                ArrayList<AbstractAction> finalActions = new ArrayList<>();
////                for (AbstractAction action: computeAvailableActions(state)){
//                // rest of the actions are more straightforward
//                for (AbstractAction action : actions) {
//                    if (action.getClass().equals(actionClass)) {
//                        finalActions.add(action);
//                    }
//                }
//                _next(state, finalActions.get(rand.nextInt(finalActions.size())));
//                return;
//        }
//        } catch (Exception e){
//            System.out.println(e);
//        }

    }
}
