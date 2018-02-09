package games.shithead.actors;

import akka.actor.AbstractActor;
import games.shithead.deck.ICardFace;
import games.shithead.deck.IMultiDeck;
import games.shithead.deck.MultiDeck;
import games.shithead.game.*;
import games.shithead.log.Logger;
import games.shithead.messages.AllocateIdRequest;
import games.shithead.messages.PlayerIdMessage;
import games.shithead.messages.PlayerActionInfo;
import games.shithead.messages.RegisterPlayerMessage;
import games.shithead.messages.StartGameMessage;
import games.shithead.messages.TableCardsSelectionMessage;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

public class GameActor extends AbstractActor {

    private Random rnd = new Random();
    private boolean isGameStarted = false;
    
    private int playerIdAllocator = 1;
    private Map<Integer, PlayerInfo> players = new HashMap<>();
    
    private IMultiDeck deck;
    private CardStatus[] cardStatuses; //For efficient mapping of cards to players
    private int cardUniqueIdAllocator = 0;
    
    //Queue of ids of players defining the order of their turns
    private Deque<Integer> playingQueue = new LinkedBlockingDeque<>();
    
    //Updated as the game progresses, and holds the players' ids in the order they finished their games
    private List<Integer> finalStandings = new LinkedList<>();
    
    private int currentTurnPlayerId = -1;
    private ICardFace currentTopCard = null;
    
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
        deck = new MultiDeck((int) Math.ceil(players.size()/4));
        cardStatuses = new CardStatus[deck.getNumberOfCards()];
        for(int i = 0; i < cardStatuses.length; i++) {
        	cardStatuses[i] = CardStatus.DECK;
        }
    }

    private void dealInitialCards() {
        players.forEach((id, playerInfo) -> {
        	List<ICardFace> cardFaces = deck.getNextCardFaces(6);
        	for(ICardFace cardFace : cardFaces) {
        		IGameCard gameCard = new GameCard(cardFace, cardUniqueIdAllocator);
        		cardStatuses[cardUniqueIdAllocator++] = new CardStatus(id, HeldCardPosition.TO_BE_DETERMINED);
        	}
        	//Send PrivateDealMessage
        });
	}
    
    private void receiveTableCardsSelection(TableCardsSelectionMessage message) {
    	//Validate selection
    	//If invalid, return cards to deck
    	//Put cards in appropriate lists
    	//Increase count of players who made their choice. 
    	//If all did, determine players order and distribute full deal
    }

	private void distributeDealtCards() {
		//Send public deal (with players order) to all players
		//Start waiting for actions  
	}

    private void determinePlayersOrder() {
    	List<Integer> playerIds = new ArrayList<>(players.keySet());
        playerIds.sort((id1, id2) -> rnd.nextBoolean() ? 1 : rnd.nextBoolean() ? 0 : -1);
        playingQueue.addAll(playerIds);
        currentTurnPlayerId = playingQueue.poll();
	}

    private void handleAction(PlayerActionInfo actionInfo) {
        boolean isActionValid = ActionValidator.validateAction(actionInfo, currentTurnPlayerId);
        if(!isActionValid){
            System.out.println("Player " + actionInfo.getPlayerId() + " made an illegal action");
            return;
        }

        //perform move here
        performAcceptedAction(actionInfo);
        distributeAcceptedAction();
        //send ReceivedCardsMessage

        boolean gameIsOver = checkGameOver();
        if(gameIsOver){
            notifyGameResult();
        }else {
            playingQueue.addLast(currentTurnPlayerId);
            currentTurnPlayerId = playingQueue.poll();
        }
    }

	private void performAcceptedAction(PlayerActionInfo turnInfo) {
        //FIXME: implement the move itself
    }

    private void distributeAcceptedAction() {
		// TODO Auto-generated method stub
	}

    private boolean checkGameOver() {
        //FIXME: implement check if game is over
        return false;
    }

    private void notifyGameResult() {
        //FIXME: send game result to all players
    }
}
