package games.shithead.game;

import games.shithead.deck.ICardFace;
import games.shithead.deck.IMultiDeck;
import games.shithead.deck.MultiDeck;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

public class GameState {

    static Logger logger = LogManager.getLogger(GameState.class);

    private Random rnd = new Random();

    // True if the time for registration is over, and the participants have been determined
    private boolean isGameStarted;

    // Maps player ids to their hands (the cards in their possession)
    private Map<Integer, IPlayerHand> players;

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

    /* The id of the current move.
     * Used to prevent ambiguity in case a PlayerActionMessage was delayed */
    private int currentMoveId = 1;

    // The id of the player who performed the last accepted action
    private int lastPerformedActionPlayer = -1;

    // Holds information about the manner in which the turn of the next player to play should be determined
    private NextTurnPolicy nextTurnPolicy = NextTurnPolicy.REGULAR;

    // Holds the cards that are currently in the pile
    private List<IGameCard> pile;

    // Queue of ids of players defining the order of their turns
    private Deque<Integer> playingQueue = new LinkedBlockingDeque<>();

    // The id of the player whose turn it is to play next
    private int currentTurnPlayerId = -1;


    // The number of hand cards a player should have at the start of the game
    private final int HAND_CARDS_AT_GAME_START = 3;

    // The number of revealed table cards a player should have at the start of the game
    private final int REVEALED_TABLE_CARDS_AT_GAME_START = 3;

    // The number of hidden table cards a player should have at the start of the game
    private final int HIDDEN_TABLE_CARDS_AT_GAME_START = 0;

    public GameState() {
        isGameStarted = false;
        players = new HashMap<>();
    }

    public boolean isGameStarted() {
        return isGameStarted;
    }

    public Map<Integer, IPlayerHand> getPlayers() {
        return players;
    }

    public int getNumberOfPlayers() {
        return players.size();
    }

    public int getCurrentPlayerTurn() {
        return currentTurnPlayerId;
    }

    public List<IGameCard> getPile() {
        return pile;
    }

    public int getCurrentMoveId() {
        return currentMoveId;
    }

    public int getRevealedCardsAtGameStart() {
        return HAND_CARDS_AT_GAME_START;
    }

    private void incrementCurrentMoveId() {
        currentMoveId++;
    }

    /**
     * Adds a player to the game
     * @param playerId The id of the new player
     */
    public void addPlayer(int playerId) {
        PlayerHand playerHand = new PlayerHand();
        players.put(playerId, playerHand);
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
    public void startGame() {
        isGameStarted = true;
        pile = new ArrayList<>();
        initDeck();
        dealInitialCards();
    }

    /**
     * Initializes the game deck
     */
    private void initDeck() {
        //Try to match deck size to number of players
        System.out.println("Initializing deck");
        deck = new MultiDeck((int) Math.ceil((double)players.size()/4));
        cardStatuses = new CardStatus[deck.getNumberOfInitialCards()];
        cards = new IGameCard[deck.getNumberOfInitialCards()];
        for(int i = 0; i < cardStatuses.length; i++) {
            cardStatuses[i] = CardStatus.DECK;
        }
    }

    /**
     * Deals the players their initial cards, including cards that are pending
     * players' selections (between being hand cards or revealed table cards)
     */
    public void dealInitialCards() {
        int numOfCardsToDeal = HAND_CARDS_AT_GAME_START + REVEALED_TABLE_CARDS_AT_GAME_START +
                HIDDEN_TABLE_CARDS_AT_GAME_START;

        players.forEach((playerId, playerInfo) -> {
            List<ICardFace> cardFaces = deck.getNextCardFaces(numOfCardsToDeal);
            int remainingHiddenTableCardsToDeal = HIDDEN_TABLE_CARDS_AT_GAME_START;
            for(ICardFace cardFace : cardFaces) {
                final int newUniqueId = cardUniqueIdAllocator++;
                IGameCard gameCard = new GameCard(cardFace, newUniqueId);
                cards[newUniqueId] = gameCard;
                if(remainingHiddenTableCardsToDeal == 0) {
                    cardStatuses[newUniqueId] = new CardStatus(playerId, HeldCardPosition.PENDING_SELECTION);
                    playerInfo.getPendingSelectionCards().add(gameCard);
                }
                else {
                    cardStatuses[newUniqueId] = new CardStatus(playerId, HeldCardPosition.TABLE_HIDDEN);
                    playerInfo.getHiddenTableCards().add(gameCard);
                    remainingHiddenTableCardsToDeal--;
                }
            }
        });
        playersPendingTableCardsSelection = getNumberOfPlayers();
    }

    /**
     * Performs a selection of revealed table cards by a player.
     * The non-selected cards will become the player's hand cards.
     * @param playerId The id of the player making the selection
     * @param selectedCardsIds The ids of the selected revealed table cards
     */
    public void performTableCardsSelection(int playerId, List<Integer> selectedCardsIds) {
        validateTableCardsSelection(playerId, selectedCardsIds);
        IPlayerHand playerHand = players.get(playerId);
        for(int selectedCardId : selectedCardsIds) {
            cardStatuses[selectedCardId].setHeldCardPosition(HeldCardPosition.TABLE_REVEALED);
            playerHand.getRevealedTableCards().add(cards[selectedCardId]);
        }
        for(IGameCard gameCard : playerHand.getPendingSelectionCards()) {
            if(cardStatuses[gameCard.getUniqueId()].getHeldCardPosition() == HeldCardPosition.PENDING_SELECTION) {
                cardStatuses[gameCard.getUniqueId()].setHeldCardPosition(HeldCardPosition.IN_HAND);
                playerHand.getHandCards().add(gameCard);
            }
        }
        playerHand.getPendingSelectionCards().clear();
        playersPendingTableCardsSelection--;
    }

    /**
     * Validates an attempted table cards selection. Throws an exception in case of an invalid selection.
     * @param playerId The id of the player attempting the selection
     * @param selectedCardsIds The chosen revealed tabke cards
     */
    private void validateTableCardsSelection(int playerId, List<Integer> selectedCardsIds) {
        if(playersPendingTableCardsSelection == 0) {
            throw new RuntimeException("Exception: All players already selected table cards");
        }
        if(!players.containsKey(playerId)) {
            throw new RuntimeException("Exception: Unregistered player");
        }
        if(selectedCardsIds.size() != REVEALED_TABLE_CARDS_AT_GAME_START) {
            throw new RuntimeException("Exception: Expected selection of " + REVEALED_TABLE_CARDS_AT_GAME_START +
                    " revealed table cards but received " + selectedCardsIds.size());
        }
        IPlayerHand playerHand = players.get(playerId);
        if(playerHand.getPendingSelectionCards().size() != HAND_CARDS_AT_GAME_START + REVEALED_TABLE_CARDS_AT_GAME_START) {
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
     * Checks if all players have selected their revealed table cards
     * @return True if all players have made their selection, false otherwise
     */
    public boolean allPlayersSelectedTableCards() {
        return playersPendingTableCardsSelection == 0;
    }

    /**
     * Produces a random permutation to represent the order of the players' turns
     */
    private void determinePlayersOrder() {
        List<Integer> playerIds = new ArrayList<>(players.keySet());
        playerIds.sort((id1, id2) -> rnd.nextBoolean() ? 1 : rnd.nextBoolean() ? 0 : -1);
        playingQueue.addAll(playerIds);
        currentTurnPlayerId = playingQueue.getFirst();
        logger.info("Players order: " + playingQueue.toString());
    }

    /**
     * Starts the game cycle once all players have selected their revealed table cards
     */
    public void startCycle() {
        determinePlayersOrder();
    }

    /**
     * Performs an action attempted by a player, as long as it is valid.
     * @param playerId The id of the player attempting the action
     * @param cardsToPut The cards the player attempted to play. An empty list indicates the player is
     **                  attempting to take the pile.
     * @param moveId The id of the move this action is relevant for
     * @param victimId The id of the victim chosen to take the pile in case a joker was played.
     *                 Will be ignored if a joker wasn't played.
     */
    public void performPlayerAction(int playerId, List<Integer> cardsToPut, int moveId, int victimId) {
        validateAction(playerId, cardsToPut, moveId);
        logger.info("Performing action");
        IPlayerHand playerHand = players.get(playerId);
        if(!cardsToPut.isEmpty()) {
            List<IGameCard> cardsToRemoveFromPlayerHand = new LinkedList<>();
            for (int cardId : cardsToPut) {
                cardStatuses[cardId] = CardStatus.PILE;
                cardsToRemoveFromPlayerHand.add(cards[cardId]);
                pile.add(0, cards[cardId]);
            }
            playerHand.removeAll(cardsToRemoveFromPlayerHand);
            updateSpecialEffects(victimId);
            dealPlayerCardsIfNeeded(playerId);
        }
        else { // Take pile
            for(IGameCard gameCard : pile) {
                playerHand.getHandCards().add(gameCard);
                cardStatuses[gameCard.getUniqueId()] = new CardStatus(playerId, HeldCardPosition.IN_HAND);
            }
            pile = new LinkedList<>();
        }
        lastPerformedActionPlayer = playerId;
        updatePlayerTurn();
        incrementCurrentMoveId();
    }

    /**
     * Validates an attempted action by a player. Throws an exception in case of an invalid action.
     * @param playerId The id of the player attempting the action
     * @param cardsToPut The cards the player attempted to play. An empty list indicates the player is
     *                   attempting to take the pile.
     * @param moveId The id of the move this action is relevant for
     */
    private void validateAction(int playerId, List<Integer> cardsToPut, int moveId) {
        logger.info("Attempting action: cards " + toCardDescriptions(cardsToPut) + " by player " + playerId);
        if(!players.containsKey(playerId)) {
            throw new RuntimeException("Exception: Unregistered player");
        }
        if(moveId != currentMoveId) {
            throw new RuntimeException("Exception: Move didn't have current move id");
        }
        for(int cardId : cardsToPut) {
            if(cardId > cards.length) {
                throw new RuntimeException("Exception: Card id doesn't exist");
            }
            // TODO: Check that cards can be played based on their position
        }

        boolean isActionValid;
        List<IGameCard> playedCards = cardsToPut.stream()
                .map(cardId -> cards[cardId])
                .collect(Collectors.toList());
        if(playerId == currentTurnPlayerId) {
            isActionValid = cardsToPut.isEmpty() ?
                    ActionValidator.canTake(pile) :
                    ActionValidator.canPlay(playedCards, pile);
        }
        else {
            isActionValid = ActionValidator.canInterrupt(playedCards, pile);
        }
        if(!isActionValid){
            throw new RuntimeException("Exception: player isn't allowed to make the given move");
        }
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
                nextTurnPolicy = NextTurnPolicy.SKIP;
                break;
            case 10:
                nextTurnPolicy = NextTurnPolicy.STEAL;
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
                burnCards(topJokersToBurn);
                if(players.containsKey(victimId)) {
                    IPlayerHand playerHand = players.get(victimId);
                    for(IGameCard gameCard : remainingPileCards) {
                        playerHand.getHandCards().add(gameCard);
                        cardStatuses[gameCard.getUniqueId()].setHolderId(victimId);
                        cardStatuses[gameCard.getUniqueId()].setHeldCardPosition(HeldCardPosition.IN_HAND);
                    }
                }
                else {
                    burnCards(remainingPileCards);
                }
                pile = new LinkedList<>();
                break;
        }
        if(completedFour()) {
            nextTurnPolicy = NextTurnPolicy.STEAL;
            burnCards(pile);
            pile = new LinkedList<>();
            return;
        }
    }

    /**
     * Checks if the latest move completed a set of 4 cards with the same value
     * at the top of the pile.
     * @return True is a set of 4 has been completed, false otherwise.
     */
    private boolean completedFour() {
        return getTopPileSequence().size() >= 4;
    }

    private List<IGameCard> getTopPileSequence() {
        int valueToCompareTo = -1;
        List<IGameCard> topPileSequence = new LinkedList<>();
        for(IGameCard gameCard : pile) {
            if(valueToCompareTo < 0) {
                valueToCompareTo = gameCard.getCardFace().get().getRank();
                topPileSequence.add(gameCard);
            }
            else if(valueToCompareTo != gameCard.getCardFace().get().getRank()) {
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
        logger.info("Burning cards");
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
            currentTurnPlayerId = lastPerformedActionPlayer;
            advancePlayingQueue(currentTurnPlayerId);
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
        if(deck.isEmpty() && players.get(lastPerformedActionPlayer).getNumOfCardsRemaining() == 0) {
            playingQueue.remove(lastPerformedActionPlayer);
            currentTurnPlayerId = playingQueue.getFirst();
        }
    }

    /**
     * Advances the playing queue by 1 step
     */
    private void advancePlayingQueue() {
        playingQueue.addLast(playingQueue.poll());
        currentTurnPlayerId = playingQueue.getFirst();
    }

    /**
     * Advances the playing queue to the selected player
     * @param currentTurnPlayerId The id of the player to advance the queue to
     */
    private void advancePlayingQueue(int currentTurnPlayerId) {
        while (playingQueue.getFirst() != currentTurnPlayerId) {
            playingQueue.addLast(playingQueue.poll());
        }
    }

    /**
     * Deals a player as many cards as needed (if at all) to complete his hand
     * @param playerId The id of the player to deal cards to
     */
    private void dealPlayerCardsIfNeeded(int playerId) {
        IPlayerHand playerInfo = players.get(playerId);
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
        if(!deck.isEmpty()) {
            return false;
        }
        for(Integer playerId : players.keySet()) {
            if(players.get(playerId).getNumOfCardsRemaining() == 0) {
                playingQueue.remove(playerId);
                currentTurnPlayerId = playingQueue.getFirst();
                return true;
            }
        }
        return false;
    }

    /**
     * Converts a list of cards to a string made of their values. Used for logging.
     * @param cardsToPut The list of cards to analyze
     * @return A String representing the sequence of the given cards' values
     */
    private String toCardDescriptions(List<Integer> cardsToPut) {
        String cardValues = "";
        for(int cardId : cardsToPut) {
            cardValues += cards[cardId].getCardFace().get().getRank() + ", ";
        }
        return "[" + cardValues + "]";
    }
}
