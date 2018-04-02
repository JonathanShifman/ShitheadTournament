package games.shithead.game;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import games.shithead.game.*;
import games.shithead.messages.*;
import games.shithead.log.Logger;

import java.util.*;

public class GameActor extends AbstractActor {

    int playerIdAllocator;
    private Map<Integer, ActorRef> playerRefs;

    private GameState gameState;

    public GameActor() {
        playerIdAllocator = 1;
        gameState = new GameState();
        InfoProvider.setGameState(gameState);
        playerRefs = new HashMap<>();
    }
    
    private String getLoggingPrefix() {
    	return "GameActor: ";
    }

    public Receive createReceive() {
        return receiveBuilder()
                .match(AllocateIdRequest.class, this::allocateId)
                .match(RegisterPlayerMessage.class, this::registerPlayer)
                .match(StartGameMessage.class, this::startGame)
                .match(TableCardsSelectionMessage.class, this::receiveTableCardsSelection)
                .match(PlayerActionInfo.class, this::handleAttemptedAction)
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
        if(gameState.isGameStarted()){
            //too late for registration
            return;
        }
        //FIXME: Save player's name as well
    	System.out.println(getLoggingPrefix() + "Registering player");
        playerRefs.put(playerRegistration.playerId, getSender());
        gameState.addPlayer(playerRegistration.playerId);
    }

    @SuppressWarnings("unused")
    private void startGame(StartGameMessage gameStarter) {
    	Logger.log(getLoggingPrefix() + "Received StartGameMessage");
        if(!gameState.enoughPlayersToStartGame()){
            System.out.println(getLoggingPrefix() + "Not enough players");
            return;
        }
        gameState.startGame();
        sendChooseTableCardsMessages();
    }

    private void sendChooseTableCardsMessages() {
        playerRefs.forEach((id, playerRef) -> {
        	System.out.println(getLoggingPrefix() + "Sending ChooseTableCardsMessage to player " + id);
            playerRef.tell(new ChooseTableCardsMessage(), self());
        });
	}
    
    private void receiveTableCardsSelection(TableCardsSelectionMessage message) {
    	Logger.log(getLoggingPrefix() + "Received TableCardsSelectionMessage");
    	gameState.performTableCardsSelection(message.getPlayerId(), message.getSelectedCardsIds());
    	if(gameState.allPlayersSelectedTableCards()) {
    	    gameState.startCycle();
    	    sendStartCycleMessages();
        }
    }

    private void sendStartCycleMessages() {
        playerRefs.forEach((id, playerRef) -> {
            System.out.println(getLoggingPrefix() + "Sending StartCycleMessage to player " + id);
            playerRef.tell(new StartCycleMessage(), self());
        });
    }

    private void handleAttemptedAction(PlayerActionInfo actionInfo) {
        gameState.attemptPlayerAction(actionInfo.getPlayerId(), actionInfo.getCardsToPut(), actionInfo.isInterruption());
        if(gameState.checkGameOver()){
            notifyGameResult();
            Logger.log(getLoggingPrefix() + "Game over");
            return;
        }
        distributeAcceptedAction(actionInfo.getPlayerId());
    }

    private void distributeAcceptedAction(int playerId) {
        AcceptedActionMessage acceptedActionMessage;
        acceptedActionMessage = new AcceptedActionMessage(playerId, gameState.getCurrentPlayerTurn());
        playerRefs.forEach((id, playerRef) -> {
            playerRef.tell(acceptedActionMessage, self());
        });
	}

    private void notifyGameResult() {
        playerRefs.forEach((id, playerRef) -> {
            playerRef.tell(new GameResult(), self());
        });
    }
}
