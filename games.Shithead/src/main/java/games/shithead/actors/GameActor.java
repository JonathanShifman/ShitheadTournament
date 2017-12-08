package games.shithead.actors;

import akka.actor.AbstractActor;
import games.shithead.deck.ICardFace;
import games.shithead.deck.IMultiDeck;
import games.shithead.deck.MultiDeck;
import games.shithead.game.*;
import games.shithead.messages.*;

import java.util.*;
import java.util.concurrent.*;

import static akka.pattern.PatternsCS.ask;

public class GameActor extends AbstractActor {

    public static final int HAND_SELECTION_TIMEOUT = 1000;
    private int idAllocator = 1;
    private Random rnd = new Random();

    private boolean isGameStarted = false;
    private Map<Integer, PlayerInfo> players = new HashMap<>();
    
    private IMultiDeck deck;
    private int[] cardDatabase; //For efficient card mapping
    private int cardUniqueIdAllocator = 0;
    
    //queue of ids of players defining the order of their turns
    private Deque<Integer> playingQueue = new LinkedBlockingDeque<>();
    private int currentPlayer = -1;

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
        getSender().tell(new IdMessage(idAllocator++), self());
    }

    private void handleTurn(PlayerTurnInfo turnInfo) {
        boolean isMoveValid = MoveValidator.validateMove(turnInfo, currentPlayer);
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
            playingQueue.addLast(currentPlayer);
            currentPlayer = playingQueue.poll();
            notifyPlayerTurn(currentPlayer);
        }
    }

    private void notifyGameResult() {
        //FIXME: send game result to all players
    }

    private boolean checkGameOver() {
        //FIXME: implement check if game is over
        return false;
    }

    private void performMove(PlayerTurnInfo turnInfo) {
        //FIXME: implement the move itself
    }

    private void startGame(StartGameMessage gameStarter) {
        if(players.size()<=1){
            System.out.println("Not enough players, waiting...");
            return;
        }
        isGameStarted = true;
        initDecks();
        dealInitialCards();
        sendStateOfGameToPlayers();
        //FIXME: fix so that player chooses the 3 cards that are revealed out of 6 he drew
        sendStateOfGameToPlayers();
        determinePlayersOrder();
        notifyPlayerTurn(currentPlayer);
    }

	private void initDecks() {
        //try to match deck size to number of players - change if it's not working well
        deck = new MultiDeck((int) Math.ceil(players.size()/4));
        cardDatabase = new int[deck.getNumberOfCards()];
    }

    private void dealInitialCards() {
        CountDownLatch dealingLatch = new CountDownLatch(players.size());
        players.forEach((id, playerInfo) -> {
        	dealCardsToPlayer(deck.getNextCardFaces(3), playerInfo.getHiddenTableCards(), id);

        	List<IGameCard> cardsForChoosing = new ArrayList<>(6);
        	dealCardsToPlayer(deck.getNextCardFaces(6), cardsForChoosing, id);

        	HandSelectionRequest request = new HandSelectionRequest(cardsForChoosing);
            CompletionStage<Object> replyFuture = ask(playerInfo.getPlayerRef(), request, HAND_SELECTION_TIMEOUT);
            replyFuture.thenAccept(replyObj -> {
                HandSelectionReply reply = (HandSelectionReply) replyObj;
                playerInfo.getHandCards().addAll(reply.getHandCards());
                playerInfo.getRevealedTableCards().addAll(reply.getFaceupCards());
                dealingLatch.countDown();
            }).exceptionally(throwable -> {
                playerInfo.getHandCards().addAll(cardsForChoosing.subList(0,3));
                playerInfo.getRevealedTableCards().addAll(cardsForChoosing.subList(4,6));
                dealingLatch.countDown();
                return null;
            });
        });

        try {
            dealingLatch.await(HAND_SELECTION_TIMEOUT * players.size(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            System.out.println("error thrown while waiting for card selection");
        }
    }
    
    private void dealCardsToPlayer(List<ICardFace> cardFaces, List<IGameCard> listToAddCardsTo, int playerId) {
    	for(ICardFace cardFace : cardFaces) {
    		IGameCard gameCard = new GameCard(cardFace, cardUniqueIdAllocator);
    		cardDatabase[cardUniqueIdAllocator++] = playerId;
    		listToAddCardsTo.add(gameCard);
    	}
    }

    private void determinePlayersOrder() {
    	List<Integer> playerIds = new ArrayList<>(players.keySet());
        playerIds.sort((id1, id2) -> rnd.nextBoolean() ? 1 : rnd.nextBoolean() ? 0 : -1);
        playingQueue.addAll(playerIds);
        currentPlayer = playingQueue.poll();
	}

	private void registerPlayer(RegisterPlayerMessage playerRegistration) {
        if(isGameStarted){
            //too late for registration
            return;
        }
        players.put(playerRegistration.playerId, new PlayerInfo(getSender()));
    }

    private void notifyPlayerTurn(int playerToNotify) {
        players.get(playerToNotify).getPlayerRef().tell(new NotifyPlayersTurnMessage(playerToNotify), self());
        //maybe start timer or something so if one player crashes it doesn't stop the game
    }

    private void sendStateOfGameToPlayers(){
        players.forEach((id, playerInfo)-> {
            playerInfo.getPlayerRef().tell(new GameState(id, players, currentTopCard), self());
        });
    }
}
