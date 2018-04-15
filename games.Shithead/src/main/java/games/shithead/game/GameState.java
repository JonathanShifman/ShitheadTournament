package games.shithead.game;

import games.shithead.deck.ICardFace;
import games.shithead.deck.IMultiDeck;
import games.shithead.deck.MultiDeck;
import games.shithead.log.Logger;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

public class GameState {

    private Random rnd = new Random();

    //True if the time for registration is over, and the participants have been determined
    private boolean isGameStarted;

    //Maps player ids to their hands (the cards in their possession)
    private Map<Integer, IPlayerHand> players;

    private IMultiDeck deck;
    private CardStatus[] cardStatuses;
    private IGameCard[] cards;
    private int cardUniqueIdAllocator = 0;

    private int playersPendingTableCardsSelection;

    private int currentMoveId = 1;

    private int lastPerformedActionPlayer = -1;
    private boolean shouldAwardTurnToLastPerformedActionPlayer = false;
    private boolean shouldSkipOne = false;

    private List<IGameCard> pile;

    //Queue of ids of players defining the order of their turns
    private Deque<Integer> playingQueue = new LinkedBlockingDeque<>();
    private int currentTurnPlayerId = -1;

    private final int HAND_CARDS_AT_GAME_START = 3;
    private final int REVEALED_TABLE_CARDS_AT_GAME_START = 3;
    private final int HIDDEN_TABLE_CARDS_AT_GAME_START = 0;

    public GameState() {
        isGameStarted = false;
        players = new HashMap<>();
    }

    private String getLoggingPrefix() {
        return "GameState: ";
    }

    public boolean enoughPlayersToStartGame() {
        return players.size() > 1;
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

    public void addPlayer(int playerId) {
        PlayerHand playerHand = new PlayerHand();
        players.put(playerId, playerHand);
    }

    public void startGame() {
        isGameStarted = true;
        pile = new ArrayList<>();
        initDecks();
        dealInitialCards();
    }

    private void initDecks() {
        //Try to match deck size to number of players
        System.out.println(getLoggingPrefix() + "Initializing deck");
        deck = new MultiDeck((int) Math.ceil((double)players.size()/4));
        cardStatuses = new CardStatus[deck.getNumberOfCards()];
        cards = new IGameCard[deck.getNumberOfCards()];
        for(int i = 0; i < cardStatuses.length; i++) {
            cardStatuses[i] = CardStatus.DECK;
        }
    }

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

    public void performTableCardsSelection(int playerId, List<Integer> selectedCardsIds) {
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
        // FIXME: Only complete selection if all cards are valid (otherwise lists should remain unchanged)
        for(int selectedCardId : selectedCardsIds) {
            if(selectedCardId >= cardStatuses.length) {
                throw new RuntimeException("Exception: Card id doesn't exist");
            }
            CardStatus cardStatus = cardStatuses[selectedCardId];
            if(cardStatus.getHolderId() != playerId || cardStatus.getHeldCardPosition() != HeldCardPosition.PENDING_SELECTION) {
                throw new RuntimeException("Exception: Card doesn't belong to player, or isn't pending selection");
            }
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

    public boolean allPlayersSelectedTableCards() {
        return playersPendingTableCardsSelection == 0;
    }



    private void determinePlayersOrder() {
        List<Integer> playerIds = new ArrayList<>(players.keySet());
        playerIds.sort((id1, id2) -> rnd.nextBoolean() ? 1 : rnd.nextBoolean() ? 0 : -1);
        playingQueue.addAll(playerIds);
        currentTurnPlayerId = playingQueue.getFirst();
        Logger.log(getLoggingPrefix() + "Players order: " + playingQueue.toString());
    }

    public void startCycle() {
        determinePlayersOrder();
    }

    private void validateAction(int playerId, List<Integer> cardsToPut, int moveId) {
        Logger.log(getLoggingPrefix() + "Attempting action: cards " + toCardDescriptions(cardsToPut) + " by player " + playerId);
        if(!players.containsKey(playerId)) {
            throw new RuntimeException("Exception: Unregistered player");
        }
        if(moveId != currentMoveId) {
            throw new RuntimeException("Exception: Move didn't have current move id");
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

    public void performPlayerAction(int playerId, List<Integer> cardsToPut, int moveId, int victimId) {
        validateAction(playerId, cardsToPut, moveId);
        Logger.log(getLoggingPrefix() + "Performing action");
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
        updateCurrentMoveId();
    }

    public int getCurrentMoveId() {
        return currentMoveId;
    }

    private void updateCurrentMoveId() {
        currentMoveId++;
    }

    private String toCardDescriptions(List<Integer> cardsToPut) {
        String cardValues = "";
        for(int cardId : cardsToPut) {
            cardValues += cards[cardId].getCardFace().get().getValue() + ", ";
        }
        return "[" + cardValues + "]";
    }

    private void updateSpecialEffects(int victimId) {
        if(completedFour()) {
            shouldAwardTurnToLastPerformedActionPlayer = true;
            burnCards(pile);
            pile = new LinkedList<>();
            return;
        }
        switch (pile.get(0).getCardFace().get().getValue()) {
            case 8:
                shouldSkipOne = true;
                break;
            case 10:
                shouldAwardTurnToLastPerformedActionPlayer = true;
                burnCards(pile);
                pile = new LinkedList<>();
                break;
            case 15:
                shouldAwardTurnToLastPerformedActionPlayer = true;
                boolean collectingTopJokers = true;
                List<IGameCard> topJokersToBurn = new LinkedList<>();
                List<IGameCard> remainingPileCards = new LinkedList<>();
                for(IGameCard gameCard : pile) {
                    if(collectingTopJokers) {
                        if(gameCard.getCardFace().get().getValue() == 15) {
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
    }

    private boolean completedFour() {
        if(pile.size() < 4) {
            return false;
        }
        int valueToCompareTo = -1;
        int count = 0;
        for(IGameCard gameCard : pile) {
            count++;
            if(valueToCompareTo < 0) {
                valueToCompareTo = gameCard.getCardFace().get().getValue();
                continue;
            }
            if(valueToCompareTo != gameCard.getCardFace().get().getValue()) {
                return false;
            }
            if(count == 4) {
                break;
            }
        }
        return true;

    }

    private void burnCards(List<IGameCard> cardsToBurn) {
        Logger.log(getLoggingPrefix() + "Burning cards");
        for(IGameCard gameCard : cardsToBurn) {
            cardStatuses[gameCard.getUniqueId()] = CardStatus.BURNT;
        }
    }

    private void updatePlayerTurn() {
        if(shouldAwardTurnToLastPerformedActionPlayer) {
            shouldAwardTurnToLastPerformedActionPlayer = false;
            currentTurnPlayerId = lastPerformedActionPlayer;
            advancePlayingQueue(currentTurnPlayerId);
            return;
        }
        if(shouldSkipOne) {
            shouldSkipOne = false;
            advancePlayingQueue();
        }
        advancePlayingQueue();
    }

    private void advancePlayingQueue() {
        playingQueue.addLast(playingQueue.poll());
        currentTurnPlayerId = playingQueue.getFirst();
    }

    private void advancePlayingQueue(int currentTurnPlayerId) {
        while (playingQueue.getFirst() != currentTurnPlayerId) {
            playingQueue.addLast(playingQueue.poll());
        }
    }

    private void dealPlayerCardsIfNeeded(int playerId) {
        IPlayerHand playerInfo = players.get(playerId);
        int neededCards = 3 - playerInfo.getHandCards().size();
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

    public int getCurrentPlayerTurn() {
        return currentTurnPlayerId;
    }

    public List<IGameCard> getPile() {
        return pile;
    }

    public int getRevealedCardsAtGameStart() {
        return HAND_CARDS_AT_GAME_START;
    }
}
