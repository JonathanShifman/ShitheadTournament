package games.shithead.game;

import akka.actor.ActorRef;
import games.shithead.deck.CardFace;
import games.shithead.deck.ICardFace;
import games.shithead.deck.IMultiDeck;
import games.shithead.deck.MultiDeck;
import games.shithead.log.Logger;
import games.shithead.messages.ReceivedCardsMessage;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

public class GameState {

    private Random rnd = new Random();

    private boolean isGameStarted;

    private Map<Integer, IPlayerInfo> players;

    private IMultiDeck deck;
    private CardStatus[] cardStatuses;
    private IGameCard[] cards;
    private int cardUniqueIdAllocator = 0;

    private int playersPendingTableCardsSelection;

    private int lastPerformedActionPlayer = -1;
    private boolean shouldAwardTurnToLastPerformedActionPlayer = false;
    private boolean shouldSkipOne = false;

    private List<IGameCard> pile;

    //Queue of ids of players defining the order of their turns
    private Deque<Integer> playingQueue = new LinkedBlockingDeque<>();
    private int currentTurnPlayerId = -1;

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

    public Map<Integer, IPlayerInfo> getPlayers() {
        return players;
    }

    public int getNumberOfPlayers() {
        return players.size();
    }

    public void addPlayer(int playerId) {
        PlayerInfo playerInfo = new PlayerInfo();
        players.put(playerId, playerInfo);
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
        players.forEach((id, playerInfo) -> {
            List<ICardFace> cardFaces = deck.getNextCardFaces(3);
            for(ICardFace cardFace : cardFaces) {
                final int newUniqueId = cardUniqueIdAllocator++;
                IGameCard gameCard = new GameCard(cardFace, newUniqueId);
                cardStatuses[newUniqueId] = new CardStatus(id, HeldCardPosition.PENDING_SELECTION);
                cards[newUniqueId] = gameCard;
            }
        });
        playersPendingTableCardsSelection = getNumberOfPlayers();
    }

    public void performTableCardsSelection(int playerId, List<Integer> selectedCardsIds) {
        IPlayerInfo playerInfo = players.get(playerId);
        for(int selectedCardId : selectedCardsIds) {
            cardStatuses[selectedCardId].setHolderId(playerId);
            cardStatuses[selectedCardId].setHeldCardPosition(HeldCardPosition.TABLE_REVEALED);
            playerInfo.getRevealedTableCards().add(cards[selectedCardId]);
        }
        for(int i = 0; i < cards.length; i++) {
            CardStatus cardStatus = cardStatuses[i];
            IGameCard card = cards[i];
            if(cardStatus.getHolderId() == playerId && cardStatus.getHeldCardPosition() == HeldCardPosition.PENDING_SELECTION) {
                cardStatus.setHeldCardPosition(HeldCardPosition.IN_HAND);
                playerInfo.getHandCards().add(card);
            }
        }
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

    public boolean attemptPlayerAction(int playerId, List<Integer> cardsToPut, boolean isInterruption) {
        List<IGameCard> playedCards = cardsToPut.stream()
                .map(cardId -> cards[cardId])
                .collect(Collectors.toList());
        boolean isActionValid = cardsToPut.isEmpty() ?
                ActionValidator.canTake(pile) :
                ActionValidator.canPlay(playedCards, pile);
        if(!isActionValid){
            System.out.println("Player " + playerId + " made an illegal action");
            return false;
        }
        performPlayerAction(playerId, cardsToPut, isInterruption);
        return true;
    }

    public void performPlayerAction(int playerId, List<Integer> cardsToPut, boolean isInterruption) {
        Logger.log(getLoggingPrefix() + "Performing action: cards " + toCardDescriptions(cardsToPut) + " by player " + playerId);
        IPlayerInfo playerInfo = players.get(playerId);
        if(!cardsToPut.isEmpty()) {
            List<IGameCard> cardsToRemoveFromHand = new LinkedList<>();
            for (int cardId : cardsToPut) {
                cardStatuses[cardId] = CardStatus.PILE;
                cardsToRemoveFromHand.add(cards[cardId]);
                pile.add(cards[cardId]);
            }
            playerInfo.getHandCards().removeAll(cardsToRemoveFromHand);
            dealPlayerCardsIfNeeded(playerId);
            updateSpecialEffects(pile.get(0).getCardFace().get().getValue());
        }
        else { // Take pile
            for(IGameCard gameCard : pile) {
                playerInfo.getHandCards().add(gameCard);
                cardStatuses[gameCard.getUniqueId()] = new CardStatus(playerId, HeldCardPosition.IN_HAND);
            }
            pile = new LinkedList<>();
        }
        lastPerformedActionPlayer = playerId;
        updatePlayerTurn();
    }

    private String toCardDescriptions(List<Integer> cardsToPut) {
        String cardValues = "";
        for(int cardId : cardsToPut) {
            cardValues += cards[cardId].getCardFace().get().getValue() + ", ";
        }
        return "[" + cardValues + "]";
    }

    private void updateSpecialEffects(int pileTopValue) {
        switch (pileTopValue) {
            case 8:
                shouldSkipOne = true;
                break;
            case 10:
                shouldAwardTurnToLastPerformedActionPlayer = true;
                burnPile();
                break;
            case 15:
                shouldAwardTurnToLastPerformedActionPlayer = true;
                burnPile();
        }
    }

    private void burnPile() {
        for(IGameCard gameCard : pile) {
            cardStatuses[gameCard.getUniqueId()] = CardStatus.BURNT;
        }
        pile = new LinkedList<>();
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
        IPlayerInfo playerInfo = players.get(playerId);
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

    public boolean checkGameOver() {
        if(!deck.isEmpty()) {
            return false;
        }
        for(Integer playerId : players.keySet()) {
            if(players.get(playerId).getHandCards().size() == 0) {
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
}
