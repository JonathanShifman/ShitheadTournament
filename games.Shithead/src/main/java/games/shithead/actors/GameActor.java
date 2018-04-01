package games.shithead.actors;

import akka.actor.AbstractActor;
import games.shithead.deck.ICardFace;
import games.shithead.deck.IMultiDeck;
import games.shithead.deck.MultiDeck;
import games.shithead.game.*;
import games.shithead.log.Logger;
import games.shithead.messages.*;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

public class GameActor extends AbstractActor {

    private Random rnd = new Random();
    private boolean isGameStarted = false;
    
    private int playerIdAllocator = 1;
    private Map<Integer, IPlayerInfo> players = new HashMap<>();
    
    private IMultiDeck deck;
    private CardStatus[] cardStatuses; //For efficient mapping of cards to players
    private IGameCard[] cards; //For efficient mapping of cards to players
    private int cardUniqueIdAllocator = 0;
    
    //Queue of ids of players defining the order of their turns
    private Deque<Integer> playingQueue = new LinkedBlockingDeque<>();
    
    //Updated as the game progresses, and holds the players' ids in the order they finished their games
    private List<Integer> finalStandings = new LinkedList<>();
    
    private int currentTurnPlayerId = -1;
    private ICardFace currentTopCard = null;

    private int playersPendingTableCardsSelection;
    
    private String getLoggingPrefix() {
    	return "GameActor: ";
    }

    public Receive createReceive() {
        return receiveBuilder()
                .match(AllocateIdRequest.class, this::allocateId)
                .match(RegisterPlayerMessage.class, this::registerPlayer)
                .match(StartGameMessage.class, this::startGame)
                .match(TableCardsSelectionMessage.class, this::receiveTableCardsSelection)
                .match(PlayerActionInfo.class, this::handleAction)
                .matchAny(this::unhandled)
                .build();
    }

    private void allocateId(AllocateIdRequest request) {
    	System.out.println(getLoggingPrefix() + "Received AllocateIdRequest");
    	System.out.println(getLoggingPrefix() + "Sending PlayerIdMessage with playerId=" + playerIdAllocator);
        getSender().tell(new PlayerIdMessage(playerIdAllocator++), self());
    }

	private void registerPlayer(RegisterPlayerMessage playerRegistration) {
    	System.out.println(getLoggingPrefix() + "Received RegisterPlayerMessage");
        if(isGameStarted){
            //too late for registration
            return;
        }
        //FIXME: Save player's name as well
    	System.out.println(getLoggingPrefix() + "Registering player");
        players.put(playerRegistration.playerId, new PlayerInfo(getSender()));
    }

    private void startGame(StartGameMessage gameStarter) {
    	Logger.log(getLoggingPrefix() + "Received StartGameMessage");
    	
        if(players.size() <= 1){
            System.out.println(getLoggingPrefix() + "Not enough players, waiting...");
            return;
        }
        
        isGameStarted = true;
        initDecks();
        dealInitialCards();
    }

	private void initDecks() {
        //try to match deck size to number of players - change if it's not working well
		System.out.println(getLoggingPrefix() + "Initializing deck");
        deck = new MultiDeck((int) Math.ceil((double)players.size()/4));
        cardStatuses = new CardStatus[deck.getNumberOfCards()];
        cards = new IGameCard[deck.getNumberOfCards()];
        for(int i = 0; i < cardStatuses.length; i++) {
        	cardStatuses[i] = CardStatus.DECK;
        }
    }

    private void dealInitialCards() {
        this.playersPendingTableCardsSelection = this.players.size();
        //Deal each player his initial cards
        players.forEach((id, playerInfo) -> {
        	List<IGameCard> cardsToDeal = new ArrayList<IGameCard>();
        	List<ICardFace> cardFaces = deck.getNextCardFaces(3);
        	for(ICardFace cardFace : cardFaces) {
        	    final int newUniqueId = cardUniqueIdAllocator++;
        		IGameCard gameCard = new GameCard(cardFace, newUniqueId);
        		cardStatuses[newUniqueId] = new CardStatus(id, HeldCardPosition.PENDING_SELECTION);
        		cards[newUniqueId] = gameCard;
                cardsToDeal.add(gameCard);
        	}
        	System.out.println(getLoggingPrefix() + "Sending PrivateDealMessage to player " + id);
        	playerInfo.getPlayerRef().tell(new PrivateDealMessage(cardsToDeal), self());
        });
	}
    
    private void receiveTableCardsSelection(TableCardsSelectionMessage message) {
    	Logger.log(getLoggingPrefix() + "Received TableCardsSelectionMessage");
        //Validate selection
        //If invalid, return cards to deck
        IPlayerInfo playerInfo = players.get(message.getPlayerId());
        for(int selectedCardId : message.getSelectedCardsIds()) {
            cardStatuses[selectedCardId].setHolderId(message.getPlayerId());
            cardStatuses[selectedCardId].setHeldCardPosition(HeldCardPosition.TABLE_REVEALED);
            playerInfo.getRevealedTableCardIds().add(selectedCardId);
        }
        for(int i = 0; i < cards.length; i++) {
            CardStatus cardStatus = cardStatuses[i];
            IGameCard card = cards[i];
            if(cardStatus.getHolderId() == message.getPlayerId() && cardStatus.getHeldCardPosition() == HeldCardPosition.PENDING_SELECTION) {
                cardStatus.setHeldCardPosition(HeldCardPosition.IN_HAND);
                playerInfo.getHandCardIds().add(card.getUniqueId());
            }
        }
    	playersPendingTableCardsSelection--;
    	if(playersPendingTableCardsSelection == 0) {
    	    determinePlayersOrder();
    	    distributePublicDeal();
        }
    }

    private void determinePlayersOrder() {
    	List<Integer> playerIds = new ArrayList<>(players.keySet());
        playerIds.sort((id1, id2) -> rnd.nextBoolean() ? 1 : rnd.nextBoolean() ? 0 : -1);
        playingQueue.addAll(playerIds);
        currentTurnPlayerId = playingQueue.getFirst();
        Logger.log(getLoggingPrefix() + "Players order: " + playingQueue.toString());
	}

    private void distributePublicDeal() {
        PublicDealMessage publicDealMessage = new PublicDealMessage(players.size(), playingQueue);
        players.forEach((id, playerInfo) -> {
            List<IGameCard> deal = new LinkedList<>();
            for(int cardId : playerInfo.getHandCardIds()) {
                deal.add(cards[cardId]);
            }
            publicDealMessage.setDeal(id, deal);
        });
        players.forEach((id, playerInfo) -> {
            Logger.log(getLoggingPrefix() + "Sending public deal to player " + id);
            playerInfo.getPlayerRef().tell(publicDealMessage, self());
        });
    }

    private void handleAction(PlayerActionInfo actionInfo) {
        Logger.log(getLoggingPrefix() + "Received attempted action");
        boolean isActionValid = ActionValidator.validateAction(actionInfo, currentTurnPlayerId);
        if(!isActionValid){
            System.out.println("Player " + actionInfo.getPlayerId() + " made an illegal action");
            return;
        }

        //perform move here
        performAcceptedAction(actionInfo);
        boolean gameIsOver = checkGameOver();
        if(gameIsOver){
            notifyGameResult();
            Logger.log(getLoggingPrefix() + "Game over");
            return;
        }

        ReceivedCardsMessage receivedCardsMessage = prepareReceivedCardsMessage(actionInfo.getPlayerId());
        playingQueue.addLast(playingQueue.poll());
        currentTurnPlayerId = playingQueue.getFirst();
        distributeAcceptedAction(actionInfo);
        players.get(actionInfo.getPlayerId()).getPlayerRef().tell(receivedCardsMessage, self());
    }

    private void performAcceptedAction(PlayerActionInfo actionInfo) {
        Logger.log(getLoggingPrefix() + "Performing action: cards " + actionInfo.getCardsToPut().toString() + " by player " + actionInfo.getPlayerId());
        IPlayerInfo playerInfo = players.get(actionInfo.getPlayerId());
        List<Integer> cardsToRemoveFromHand = new LinkedList<>();
        for(int cardId : actionInfo.getCardsToPut()) {
            cardStatuses[cardId] = CardStatus.PILE;
            cardsToRemoveFromHand.add(cardId);
        }
        playerInfo.getHandCardIds().removeAll(cardsToRemoveFromHand);
    }

    private ReceivedCardsMessage prepareReceivedCardsMessage(int playerId) {
        IPlayerInfo playerInfo = players.get(playerId);
        int neededCards = 3 - playerInfo.getHandCardIds().size();
        ReceivedCardsMessage receivedCardsMessage = new ReceivedCardsMessage();
        if(neededCards > 0) {
            List<ICardFace> cardFaces = deck.getNextCardFaces(neededCards);
            for(ICardFace cardFace : cardFaces) {
                final int newUniqueId = cardUniqueIdAllocator++;
                IGameCard gameCard = new GameCard(cardFace, newUniqueId);
                cardStatuses[newUniqueId] = new CardStatus(playerId, HeldCardPosition.IN_HAND);
                cards[newUniqueId] = gameCard;
                playerInfo.getHandCardIds().add(newUniqueId);
                receivedCardsMessage.addCard(gameCard);
            }
        }
        return receivedCardsMessage;
    }

    private void distributeAcceptedAction(PlayerActionInfo acceptedActionInfo) {
        AcceptedActionMessage acceptedActionMessage = new AcceptedActionMessage(acceptedActionInfo, currentTurnPlayerId);
        Logger.log(getLoggingPrefix() + "Distributing accepted action");
        players.forEach((id, playerInfo) -> {
            playerInfo.getPlayerRef().tell(acceptedActionMessage, self());
        });
	}

    private boolean checkGameOver() {
        if(!deck.isEmpty()) {
            return false;
        }
        for(Integer playerId : players.keySet()) {
            if(players.get(playerId).getHandCardIds().size() == 0) {
                return true;
            }
        }
        return false;
    }

    private void notifyGameResult() {
        players.forEach((id, playerInfo) -> {
            playerInfo.getPlayerRef().tell(new GameResult(), self());
        });
    }
}
