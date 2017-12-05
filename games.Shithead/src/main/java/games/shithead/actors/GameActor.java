package games.shithead.actors;

import akka.actor.AbstractActor;
import games.shithead.deck.ICard;
import games.shithead.deck.IMultiDeck;
import games.shithead.deck.MultiDeck;
import games.shithead.game.*;
import games.shithead.gameManagement.NotifyPlayersTurn;
import games.shithead.gameManagement.RegisterPlayer;
import games.shithead.gameManagement.StartGame;
import games.shithead.gameManagement.AllocateIdRequest;
import games.shithead.gameManagement.IdMessage;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;

public class GameActor extends AbstractActor {

    private int idAllocator = 1;

    private boolean isGameStarted = false;
    private Map<Integer, PlayerInfo> players = new HashMap<>();
    private IMultiDeck deck;
    //queue of ids of players defining the order of their turns
    private Deque<Integer> playingQueue = new LinkedBlockingDeque<>();
    private int currentPlayer = -1;

    private ICard currentTopCard = null;

    public Receive createReceive() {
        return receiveBuilder()
                .match(AllocateIdRequest.class, this::allocateId)
                .match(RegisterPlayer.class, this::registerPlayer)
                .match(StartGame.class, this::startGame)
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

    private void startGame(StartGame gameStarter) {
        if(players.size()<=1){
            System.out.println("Not enough players, waiting...");
            return;
        }
        isGameStarted = true;
        initDecks();
    }

    private void initDecks() {
        //try to match deck size to number of players - change if it's not working well
        deck = new MultiDeck((int) Math.ceil(players.size()/4));
        players.forEach((id, playerInfo) -> {
            playerInfo.getHandCards().addAll(deck.getNextCards(3));
            playerInfo.getHiddenTableCards().addAll(deck.getNextCards(3));
            playerInfo.getRevealedTableCards().addAll(deck.getNextCards(3));
            playingQueue.add(id);
        });


        //FIXME: fix so that player chooses the 3 cards that are revealed out of 6 he drew

        currentTopCard = deck.getNextCard();
        while(currentTopCard.isSpecialCard()){
            currentTopCard = deck.getNextCard();
        }
        sendStateOfGameToPlayers();

        currentPlayer = playingQueue.poll();
        notifyPlayerTurn(currentPlayer);
    }

    private void registerPlayer(RegisterPlayer playerRegistration) {
        if(isGameStarted){
            //too late for registration
            return;
        }
        players.put(playerRegistration.playerId, new PlayerInfo(getSender()));
    }

    private void notifyPlayerTurn(int playerToNotify) {
        players.get(playerToNotify).getPlayerRef().tell(new NotifyPlayersTurn(playerToNotify), self());
        //maybe start timer or something so if one player crashes it doesn't stop the game
    }

    private void sendStateOfGameToPlayers(){
        players.forEach((id, playerInfo)-> {
            playerInfo.getPlayerRef().tell(new GameState(id, players, currentTopCard), self());
        });
    }
}
