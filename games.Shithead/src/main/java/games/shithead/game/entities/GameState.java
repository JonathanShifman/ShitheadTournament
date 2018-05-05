package games.shithead.game.entities;

import games.shithead.deck.ICardFace;
import games.shithead.deck.IMultiDeck;
import games.shithead.deck.MultiDeck;
import games.shithead.game.InitParams;
import games.shithead.game.interfaces.IGameCard;
import games.shithead.game.interfaces.IPlayerState;
import games.shithead.utils.ConstantsProvider;
import games.shithead.utils.LoggingUtils;
import games.shithead.game.validation.ActionValidationResult;
import games.shithead.game.validation.ActionValidatorForGame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ini4j.Ini;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

public class GameState {

    static Logger logger = LogManager.getLogger("Game");
    static Logger initLogger = LogManager.getLogger("Init");

    private Random rnd = new Random();

    // True if the time for registration is over, and the participants have been determined
    private boolean isGameStarted;

    // Maps player ids to their states (the cards in their possession)
    private Map<Integer, IPlayerState> players;

    // The multi deck that has been allocated for the game
    private IMultiDeck deck;

    // Maps a card's unique id (as the array index) to itself
    private IGameCard[] cards;

    // Maps a card's unique id (as the array index) to its status
    private CardStatus[] cardStatuses;

    // Used to allocate incrementing unique ids to cards as they are drawn from the deck
    private int cardUniqueIdAllocator = 0;

    // The number of players who are yet to select their table cards
    private int playersPendingTableCardsSelection;

    /* The id of the next move.
     * Used to prevent ambiguity in case a PlayerActionMessage was delayed */
    private int nextMoveId = 1;

    // The id of the player who performed the last accepted action
    private int lastPerformedActionPlayer = -1;

    // Holds information about the manner in which the turn of the next player to play should be determined
    private NextTurnPolicy nextTurnPolicy = NextTurnPolicy.REGULAR;

    // Holds the cards that are currently in the pile
    private List<IGameCard> pile;

    // Queue of ids of players defining the order of their turns
    private Deque<Integer> playingQueue = new LinkedBlockingDeque<>();

    // The id of the player whose turn it is to play next
    private int nextTurnPlayerId = -1;


    // The number of hand cards a player should have at the start of the game
    private int HAND_CARDS_AT_GAME_START;

    // The number of visible table cards a player should have at the start of the game
    private int VISIBLE_TABLE_CARDS_AT_GAME_START;

    // The number of hidden table cards a player should have at the start of the game
    private int HIDDEN_TABLE_CARDS_AT_GAME_START;

    public GameState() throws IOException {
        isGameStarted = false;
        players = new HashMap<>();
        initParameters();
    }

    private void initParameters() throws IOException {
        Ini ini = new Ini(new File(System.getenv(ConstantsProvider.SYSTEM_ENV_VAR_NAME) + "\\games.Shithead\\config\\config.ini"));
        HAND_CARDS_AT_GAME_START = Integer.parseInt(ini.get("deal", "hand_cards_at_game_start"));
        VISIBLE_TABLE_CARDS_AT_GAME_START = Integer.parseInt(ini.get("deal", "visible_table_cards_at_game_start"));
        HIDDEN_TABLE_CARDS_AT_GAME_START = Integer.parseInt(ini.get("deal", "hidden_table_cards_at_game_start"));
    }

    public boolean isGameStarted() {
        return isGameStarted;
    }

    public Map<Integer, IPlayerState> getPlayers() {
        return players;
    }

    public int getNumberOfPlayers() {
        return players.size();
    }

    public int getNextPlayerTurn() {
        return nextTurnPlayerId;
    }

    public List<IGameCard> getPile() {
        return pile;
    }

    public int getNextMoveId() {
        return nextMoveId;
    }

    public int getNumOfVisibleTableCardsAtGameStart() {
        return HAND_CARDS_AT_GAME_START;
    }

    private void incrementNextMoveId() {
        nextMoveId++;
    }

    /**
     * Adds a player to the game
     * @param playerId The id of the new player
     */
    public void addPlayer(int playerId) {
        logger.info("Adding player with player id: " + playerId);
        IPlayerState playerState = new PlayerState();
        players.put(playerId, playerState);
    }

    /**
     * Checks if there are enough players to start the game
     * @return true if there are enough players, false otherwise
     */
    public boolean enoughPlayersToStartGame() {
        return players.size() > 1;
    }

    /**
     * Starts the game by initializing the required date
     */
    public void startGame(InitParams initParams) {
        logger.info("Starting game");
        isGameStarted = true;
        pile = new ArrayList<>();
        initDeck(initParams);
        dealInitialCards();
    }

    /**
     * Initializes the game deck
     */
    private void initDeck(InitParams initParams) {
        //Try to match deck size to number of players
        logger.info("Initializing deck");
        if (initParams.isWithDeckCards()) {
            logger.debug("Initializing deck with " + initParams.getDeckCardDescriptions().stream().collect(Collectors.joining(",")));
            deck = new MultiDeck(initParams.getDeckCardDescriptions());
        }
        else {
            deck = new MultiDeck((int) Math.ceil((double) players.size() / 4));
        }

        initLogger.info(deck.toString());

        cardStatuses = new CardStatus[deck.getNumberOfInitialCards()];
        cards = new IGameCard[deck.getNumberOfInitialCards()];
        for(int i = 0; i < cardStatuses.length; i++) {
            cardStatuses[i] = CardStatus.DECK;
        }
    }

    /**
     * Deals the players their initial cards, including cards that are pending
     * players' selections (between being hand cards or visible table cards)
     */
    public void dealInitialCards() {
        logger.info("Dealing initial cards");
        int numOfCardsToDeal = HAND_CARDS_AT_GAME_START + VISIBLE_TABLE_CARDS_AT_GAME_START +
                HIDDEN_TABLE_CARDS_AT_GAME_START;

        players.forEach((playerId, playerState) -> {
            List<ICardFace> cardFaces = deck.getNextCardFaces(numOfCardsToDeal);
            int remainingHiddenTableCardsToDeal = HIDDEN_TABLE_CARDS_AT_GAME_START;
            for(ICardFace cardFace : cardFaces) {
                final int newUniqueId = cardUniqueIdAllocator++;
                IGameCard gameCard = new GameCard(cardFace, newUniqueId);
                cards[newUniqueId] = gameCard;
                if(remainingHiddenTableCardsToDeal == 0) {
                    cardStatuses[newUniqueId] = new CardStatus(playerId, HeldCardPosition.PENDING_SELECTION);
                    playerState.getPendingSelectionCards().add(gameCard);
                }
                else {
                    cardStatuses[newUniqueId] = new CardStatus(playerId, HeldCardPosition.TABLE_HIDDEN);
                    playerState.getHiddenTableCards().add(gameCard);
                    remainingHiddenTableCardsToDeal--;
                }
            }
        });
        playersPendingTableCardsSelection = getNumberOfPlayers();
        logGameState();
    }

    /**
     * Performs a selection of visible table cards by a player.
     * The non-selected cards will become the player's hand cards.
     * @param playerId The id of the player making the selection
     * @param selectedCardsIds The ids of the selected visible table cards
     */
    public void performTableCardsSelection(int playerId, List<Integer> selectedCardsIds) {
        logger.debug("Attempting table cards selection by player " + playerId);
        validateTableCardsSelection(playerId, selectedCardsIds);
        logger.info("Performing table cards selection by player " + playerId);
        IPlayerState playerState = players.get(playerId);
        for(int selectedCardId : selectedCardsIds) {
            cardStatuses[selectedCardId].setHeldCardPosition(HeldCardPosition.TABLE_VISIBLE);
            playerState.getVisibleTableCards().add(cards[selectedCardId]);
        }
        for(IGameCard gameCard : playerState.getPendingSelectionCards()) {
            if(cardStatuses[gameCard.getUniqueId()].getHeldCardPosition() == HeldCardPosition.PENDING_SELECTION) {
                cardStatuses[gameCard.getUniqueId()].setHeldCardPosition(HeldCardPosition.IN_HAND);
                playerState.getHandCards().add(gameCard);
            }
        }
        playerState.getPendingSelectionCards().clear();
        playersPendingTableCardsSelection--;
    }

    /**
     * Validates an attempted table cards selection. Throws an exception in case of an invalid selection.
     * @param playerId The id of the player attempting the selection
     * @param selectedCardsIds The chosen visible tabke cards
     */
    private void validateTableCardsSelection(int playerId, List<Integer> selectedCardsIds) {
        logger.debug("Validating table cards selection by player " + playerId);
        if(playersPendingTableCardsSelection == 0) {
            throw new RuntimeException("Exception: All players already selected table cards");
        }
        if(!players.containsKey(playerId)) {
            throw new RuntimeException("Exception: Unregistered player");
        }
        if(selectedCardsIds.size() != VISIBLE_TABLE_CARDS_AT_GAME_START) {
            throw new RuntimeException("Exception: Expected selection of " + VISIBLE_TABLE_CARDS_AT_GAME_START +
                    " visible table cards but received " + selectedCardsIds.size());
        }
        IPlayerState playerState = players.get(playerId);
        if(playerState.getPendingSelectionCards().size() != HAND_CARDS_AT_GAME_START + VISIBLE_TABLE_CARDS_AT_GAME_START) {
            throw new RuntimeException("Exception: Player has already made table cards selection");
        }
        for(int selectedCardId : selectedCardsIds) {
            if(selectedCardId >= cardStatuses.length) {
                throw new RuntimeException("Exception: Card id doesn't exist");
            }
            CardStatus cardStatus = cardStatuses[selectedCardId];
            if(cardStatus.getHolderId() != playerId || cardStatus.getHeldCardPosition() != HeldCardPosition.PENDING_SELECTION) {
                throw new RuntimeException("Exception: Card doesn't belong to player, or isn't pending selection");
            }
        }
    }

    /**
     * Checks if all players have selected their visible table cards
     * @return True if all players have made their selection, false otherwise
     */
    public boolean allPlayersSelectedTableCards() {
        return playersPendingTableCardsSelection == 0;
    }

    /**
     * Starts the game cycle once all players have selected their visible table cards
     * @param initParams
     */
    public void startCycle(InitParams initParams) {
        logger.info("Starting game cycle");
        if (initParams.isWithPlayingQueue()) {
            determinePlayersOrder(initParams.getPlayingQueue());
        }
        else {
            determinePlayersOrder();
        }
        logGameState();
    }

    /**
     * Produces a random permutation to represent the order of the players' turns
     */
    private void determinePlayersOrder() {
        List<Integer> playerIds = new ArrayList<>(players.keySet());
        playerIds.sort((id1, id2) -> rnd.nextBoolean() ? 1 : rnd.nextBoolean() ? 0 : -1);
        playingQueue.addAll(playerIds);
        nextTurnPlayerId = playingQueue.getFirst();
        logger.info("Determined players order: " + playingQueue.toString());
        initLogger.info(playingQueue.stream()
                .map(id -> id.toString())
                .collect(Collectors.joining(",")));
    }

    private void determinePlayersOrder(List<Integer> playingQueueList) {
        playingQueue.addAll(playingQueueList);
        nextTurnPlayerId = playingQueue.getFirst();
        logger.info("Determined players order: " + playingQueue.toString());
        initLogger.info(playingQueue.stream()
                .map(id -> id.toString())
                .collect(Collectors.joining(",")));
    }

    /**
     * Performs an action attempted by a player, as long as it is valid.
     * @param playerId The id of the player attempting the action
     * @param playerActionInfo The PlayerActionInfo object containing info about the cards the player chose
     *                         to play, and the victim id if relevant.
     * @param moveId The id of the move this action is relevant for
     */
    public void performPlayerAction(int playerId, PlayerActionInfo playerActionInfo, int moveId) {
        List<Integer> cardsToPut = playerActionInfo.getCardsToPut();
        logger.debug("Attempting action by player " + playerId);
        int victimId = playerActionInfo.getVictimId();
        ActionValidationResult validationResult = validateAction(playerId, cardsToPut, moveId);
        if(validationResult == ActionValidationResult.FOUL) {
            throw new RuntimeException("Exception: Foul");
        }

        String logString = "Performing action by player " + playerId + ". cards: " + cardIdsToDescriptions(cardsToPut);
        if(cardsToPut.size() > 0 && cards[cardsToPut.get(0)].getCardFace().get().getRank() == 15) {
            logString += ", victim: " + playerActionInfo.getVictimId();
        }
        logger.info(logString);

        IPlayerState playerState = players.get(playerId);
        List<IGameCard> cardsToRemoveFromPlayerState = new LinkedList<>();
        for (int cardId : cardsToPut) {
            cardStatuses[cardId] = CardStatus.PILE;
            cardsToRemoveFromPlayerState.add(cards[cardId]);
            pile.add(0, cards[cardId]);
        }
        playerState.removeAll(cardsToRemoveFromPlayerState);

        updateSpecialEffects(victimId);
        dealPlayerCardsIfNeeded(playerId);
        if(validationResult == ActionValidationResult.TAKE) {
            for(IGameCard gameCard : pile) {
                playerState.getHandCards().add(gameCard);
                cardStatuses[gameCard.getUniqueId()] = new CardStatus(playerId, HeldCardPosition.IN_HAND);
            }
            pile = new LinkedList<>();
        }
        lastPerformedActionPlayer = playerId;
        updatePlayerTurn();
        checkIfPlayerWon();
        incrementNextMoveId();
        logGameState();
    }

    /**
     * Validates an attempted action by a player. Throws an exception in case of an invalid action.
     * @param playerId The id of the player attempting the action
     * @param cardsToPut The cards the player attempted to play. An empty list indicates the player is
     *                   attempting to take the pile.
     * @param moveId The id of the move this action is relevant for
     */
    private ActionValidationResult validateAction(int playerId, List<Integer> cardsToPut, int moveId) {
        logger.debug("Validating action by player " + playerId);
        if(!players.containsKey(playerId)) {
            throw new RuntimeException("Exception: Unregistered player");
        }
        if(moveId != nextMoveId) {
            throw new RuntimeException("Exception: Move didn't have the correct move id");
        }
        List<IGameCard> playedCards;
        try {
            playedCards = cardsToPut.stream()
                    .map(cardId -> cards[cardId])
                    .collect(Collectors.toList());
        }
        catch (Exception e) {
            throw new RuntimeException("Exception: One or more of the card ids didn't exist");
        }

        IPlayerState playerState = players.get(playerId);
        return playerId == nextTurnPlayerId ?
            ActionValidatorForGame.validateAction(playerState, playedCards, pile) :
            ActionValidatorForGame.validateInterruption(playerState, playedCards, pile);
    }

    /**
     * Updates the necessary information for implementation of special effects when
     * a special card has been played.
     * @param victimId The id of the victim chosen to take the pile - Only relevant if
     *                 a joker was played.
     */
    private void updateSpecialEffects(int victimId) {
        switch (pile.get(0).getCardFace().get().getRank()) {
            case 8:
                logger.info("Will skip one turn");
                nextTurnPolicy = NextTurnPolicy.SKIP;
                break;
            case 10:
                nextTurnPolicy = NextTurnPolicy.STEAL;
                logger.info("Burning pile");
                burnCards(pile);
                pile = new LinkedList<>();
                break;
            case 15:
                nextTurnPolicy = NextTurnPolicy.STEAL;
                boolean collectingTopJokers = true;
                List<IGameCard> topJokersToBurn = new LinkedList<>();
                List<IGameCard> remainingPileCards = new LinkedList<>();
                for(IGameCard gameCard : pile) {
                    if(collectingTopJokers) {
                        if(gameCard.getCardFace().get().getRank() == 15) {
                            topJokersToBurn.add(gameCard);
                        }
                        else {
                            remainingPileCards.add(gameCard);
                            collectingTopJokers = false;
                        }
                    }
                    else {
                        remainingPileCards.add(gameCard);
                    }
                }
                logger.info("Burning jokers");
                burnCards(topJokersToBurn);
                if(players.containsKey(victimId) && !players.get(victimId).hasWon()) {
                    logger.info("Giving cards to player " + victimId);
                    IPlayerState playerState = players.get(victimId);
                    for(IGameCard gameCard : remainingPileCards) {
                        playerState.getHandCards().add(gameCard);
                        cardStatuses[gameCard.getUniqueId()].setHolderId(victimId);
                        cardStatuses[gameCard.getUniqueId()].setHeldCardPosition(HeldCardPosition.IN_HAND);
                    }
                }
                else {
                    logger.info("Burning pile");
                    burnCards(remainingPileCards);
                }
                pile = new LinkedList<>();
                break;
        }
        if(completedFour()) {
            nextTurnPolicy = NextTurnPolicy.STEAL;
            logger.info("Burning pile");
            burnCards(pile);
            pile = new LinkedList<>();
            return;
        }
    }

    /**
     * Checks if the latest move completed a set of 4 cards with the same rank
     * at the top of the pile.
     * @return True is a set of 4 has been completed, false otherwise.
     */
    private boolean completedFour() {
        return getTopPileSequence().size() >= 4;
    }

    private List<IGameCard> getTopPileSequence() {
        int rankToCompareTo = -1;
        List<IGameCard> topPileSequence = new LinkedList<>();
        for(IGameCard gameCard : pile) {
            if(rankToCompareTo < 0) {
                rankToCompareTo = gameCard.getCardFace().get().getRank();
                topPileSequence.add(gameCard);
            }
            else if(rankToCompareTo != gameCard.getCardFace().get().getRank()) {
                break;
            }
            else {
                topPileSequence.add(gameCard);
            }
        }
        return topPileSequence;
    }

    /**
     * Marks the given cards as burned
     * @param cardsToBurn The cards to burn
     */
    private void burnCards(List<IGameCard> cardsToBurn) {
        for(IGameCard gameCard : cardsToBurn) {
            cardStatuses[gameCard.getUniqueId()] = CardStatus.BURNT;
        }
    }

    /**
     * Updates the turn to the player who is supposed to play next, with consideration
     * to special effects
     */
    private void updatePlayerTurn() {
        if(nextTurnPolicy == NextTurnPolicy.STEAL) {
            nextTurnPlayerId = lastPerformedActionPlayer;
            advancePlayingQueue(nextTurnPlayerId);
        }
        else if(nextTurnPolicy == NextTurnPolicy.SKIP) {
            // FIXME
            advancePlayingQueue();
            advancePlayingQueue();
        }
        else {
            advancePlayingQueue();
        }
        nextTurnPolicy = NextTurnPolicy.REGULAR;
    }

    private void checkIfPlayerWon() {
        IPlayerState playerToCheck = players.get(lastPerformedActionPlayer);
        if(deck.isEmpty() && playerToCheck.getNumOfCardsRemaining() == 0) {
            logger.info("Player " + lastPerformedActionPlayer + " won");
            playerToCheck.markAsWon();
            playingQueue.remove(lastPerformedActionPlayer);
            nextTurnPlayerId = playingQueue.getFirst();
        }
    }

    /**
     * Advances the playing queue by 1 step
     */
    private void advancePlayingQueue() {
        playingQueue.addLast(playingQueue.poll());
        nextTurnPlayerId = playingQueue.getFirst();
    }

    /**
     * Advances the playing queue to the selected player
     * @param nextTurnPlayerId The id of the player to advance the queue to
     */
    private void advancePlayingQueue(int nextTurnPlayerId) {
        while (playingQueue.getFirst() != nextTurnPlayerId) {
            playingQueue.addLast(playingQueue.poll());
        }
    }

    /**
     * Deals a player as many cards as needed (if at all) to complete his hand
     * @param playerId The id of the player to deal cards to
     */
    private void dealPlayerCardsIfNeeded(int playerId) {
        IPlayerState playerInfo = players.get(playerId);
        int neededCards = HAND_CARDS_AT_GAME_START - playerInfo.getHandCards().size();
        if(neededCards > 0) {
            List<ICardFace> cardFaces = deck.getNextCardFaces(neededCards);
            for(ICardFace cardFace : cardFaces) {
                final int newUniqueId = cardUniqueIdAllocator++;
                IGameCard gameCard = new GameCard(cardFace, newUniqueId);
                cardStatuses[newUniqueId] = new CardStatus(playerId, HeldCardPosition.IN_HAND);
                cards[newUniqueId] = gameCard;
                playerInfo.getHandCards().add(gameCard);
            }
        }
    }

    /**
     * Checks if the game is over
     * @return True if the game is over, false otherwise
     */
    public boolean isGameOver() {
        return playingQueue.size() == 1;
    }

    /**
     * Converts a list of card ids to a string made of the cards' descriptions. Used for logging.
     * @param cardIds The list of card ids to analyze
     * @return A String representing the given cards' descriptions
     */
    private String cardIdsToDescriptions(List<Integer> cardIds) {
        return LoggingUtils.cardsToMinDescriptions(cardIds.stream()
                .map(cardId -> cards[cardId])
                .collect(Collectors.toList()));
    }

    private void logGameState() {
        logger.info("Current Game State: ");
        players.entrySet().forEach(entry -> logPlayerState(entry.getKey(), entry.getValue()));
        logger.info("Pile: " + LoggingUtils.cardsToMinDescriptions(pile));
        logger.info("Next move id: " + nextMoveId);
        logger.info("Next player turn id: " + nextTurnPlayerId);
    }

    private void logPlayerState(int playerId, IPlayerState playerState) {
        logger.info("Player " + playerId + " state: " + playerState.toString());
    }

    public IPlayerState getPublicState(int playerId) {
        return players.get(playerId).publicClone();
    }

    public IPlayerState getPrivateState(int playerId) {
        return players.get(playerId).privateClone();
    }
}
