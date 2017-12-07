package games.shithead.actors;

import akka.actor.AbstractActor;
import games.shithead.deck.ICardFace;
import games.shithead.deck.IMultiDeck;
import games.shithead.deck.MultiDeck;
import games.shithead.game.*;
import games.shithead.messages.AllocateIdRequest;
import games.shithead.messages.PlayerIdMessage;
import games.shithead.messages.NotifyPlayersTurnMessage;
import games.shithead.messages.RegisterPlayerMessage;
import games.shithead.messages.StartGameMessage;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

public class GameActor extends AbstractActor {

    private Random rnd = new Random();
    private boolean isGameStarted = false;
    
    private int playerIdAllocator = 1;
    private Map<Integer, PlayerInfo> players = new HashMap<>();
    
    private IMultiDeck deck;
    private int[] cardDatabase; 
    //For efficient mapping of cards to players
    private int cardUniqueIdAllocator = 0;
    
    //Queue of ids of players defining the order of their turns
    private Deque<Integer> playingQueue = new LinkedBlockingDeque<>();
    
    //Updated as the game progresses, and holds the players' ids in the order they finished their games
    private List<Integer> finalStandings = new LinkedList<>();
    
    private int currentTurnPlayerId = -1;
    private ICardFace currentTopCard = null;

    public Receive createReceive() {
        return receiveBuilder()
                .match(AllocateIdRequest.class, this::allocateId)
                .match(RegisterPlayerMessage.class, this::registerPlayer)
                .match(StartGameMessage.class, this::startGame)
                .match(PlayerTurnInfo.class, this::handleTurn)
                .matchAny(this::unhandled)
                .build();
    }

    private void allocateId(AllocateIdRequest request) {
        getSender().tell(new PlayerIdMessage(playerIdAllocator++), self());
    }

	private void registerPlayer(RegisterPlayerMessage playerRegistration) {
        if(isGameStarted){
            //too late for registration
            return;
        }
        //FIXME: Save player's name as well
        players.put(playerRegistration.playerId, new PlayerInfo(getSender()));
    }

    private void startGame(StartGameMessage gameStarter) {
        if(players.size() <= 1){
            System.out.println("Not enough players, waiting...");
            return;
        }
        
        isGameStarted = true;
        initDecks();
        dealInitialCards();
        //Wait for table cards selection
        distributeDealtCards();
        determinePlayersOrder();
        distributePlayersOrder();
        startCycle();
    }

	private void initDecks() {
        //try to match deck size to number of players - change if it's not working well
        deck = new MultiDeck((int) Math.ceil(players.size()/4));
        cardDatabase = new int[deck.getNumberOfCards()];
    }

    private void dealInitialCards() {
        players.forEach((id, playerInfo) -> {
        	dealCardsToPlayer(deck.getNextCardFaces(3), playerInfo.getHandCards(), id);
        	dealCardsToPlayer(deck.getNextCardFaces(3), playerInfo.getHiddenTableCards(), id);
        	dealCardsToPlayer(deck.getNextCardFaces(3), playerInfo.getRevealedTableCards(), id);
        });
	}
    
    private void dealCardsToPlayer(List<ICardFace> cardFaces, List<IGameCard> listToAddCardsTo, int playerId) {
    	for(ICardFace cardFace : cardFaces) {
    		IGameCard gameCard = new GameCard(cardFace, cardUniqueIdAllocator);
    		cardDatabase[cardUniqueIdAllocator++] = playerId;
    		listToAddCardsTo.add(gameCard);
    	}
    }

	private void distributeDealtCards() {
		// TODO Auto-generated method stub
		
	}

    private void determinePlayersOrder() {
    	List<Integer> playerIds = new ArrayList<>(players.keySet());
        playerIds.sort((id1, id2) -> rnd.nextBoolean() ? 1 : rnd.nextBoolean() ? 0 : -1);
        playingQueue.addAll(playerIds);
        currentTurnPlayerId = playingQueue.poll();
	}

	private void distributePlayersOrder() {
		// TODO Auto-generated method stub
		
	}

	private void startCycle() {
		while (playingQueue.size() > 1) {
			//Play
		}
		finalStandings.add(playingQueue.removeFirst());
		//Finish game
	}

    private void notifyPlayerTurn(int playerToNotify) {
        players.get(playerToNotify).getPlayerRef().tell(new NotifyPlayersTurnMessage(playerToNotify), self());
        //maybe start timer or something so if one player crashes it doesn't stop the game
    }

    private void handleTurn(PlayerTurnInfo turnInfo) {
        boolean isMoveValid = MoveValidator.validateMove(turnInfo, currentTurnPlayerId);
        if(!isMoveValid){
            System.out.println("Player " + turnInfo.getPlayerId() + " made an illegal move");
            return;
        }

        //perform move here
        performMove(turnInfo);

        sendStateOfGameToPlayers();

        boolean gameIsOver = checkGameOver();
        if(gameIsOver){
            notifyGameResult();
        }else {
            playingQueue.addLast(currentTurnPlayerId);
            currentTurnPlayerId = playingQueue.poll();
            notifyPlayerTurn(currentTurnPlayerId);
        }
    }

    private void performMove(PlayerTurnInfo turnInfo) {
        //FIXME: implement the move itself
    }

    private void sendStateOfGameToPlayers(){
        players.forEach((id, playerInfo)-> {
            playerInfo.getPlayerRef().tell(new GameState(id, players, currentTopCard), self());
        });
    }

    private void notifyGameResult() {
        //FIXME: send game result to all players
    }

    private boolean checkGameOver() {
        //FIXME: implement check if game is over
        return false;
    }
}
