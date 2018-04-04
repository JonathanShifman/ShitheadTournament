package games.shithead.game;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import games.shithead.log.Logger;
import games.shithead.messages.*;
//import org.apache.logging.log4j.Logger;
//import org.apache.logging.log4j.LogManager;

import java.util.*;

public class GameActor extends AbstractActor {

//    static Logger logger = LogManager.getLogger(GameActor.class);

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
        Logger.log(getLoggingPrefix() + "Received AllocateIdRequest");
        Logger.log(getLoggingPrefix() + "Sending PlayerIdMessage with playerId=" + playerIdAllocator);
        getSender().tell(new PlayerIdMessage(playerIdAllocator++), self());
    }

	private void registerPlayer(RegisterPlayerMessage playerRegistration) {
        Logger.log(getLoggingPrefix() + "Received RegisterPlayerMessage");
        if(gameState.isGameStarted()){
            //too late for registration
            return;
        }
        //FIXME: Save player's name as well
        Logger.log(getLoggingPrefix() + "Registering player");
        playerRefs.put(playerRegistration.playerId, getSender());
        gameState.addPlayer(playerRegistration.playerId);
    }

    @SuppressWarnings("unused")
    private void startGame(StartGameMessage gameStarter) {
        Logger.log(getLoggingPrefix() + "Received StartGameMessage");
//        Logger.log("Warn visible");
        if(!gameState.enoughPlayersToStartGame()){
            Logger.log(getLoggingPrefix() + "Not enough players");
            return;
        }
        gameState.startGame();
        sendChooseTableCardsMessages();
    }

    private void sendChooseTableCardsMessages() {
        playerRefs.forEach((id, playerRef) -> {
            Logger.log(getLoggingPrefix() + "Sending ChooseTableCardsMessage to player " + id);
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
            Logger.log(getLoggingPrefix() + "Sending StartCycleMessage to player " + id);
            playerRef.tell(new StartCycleMessage(), self());
        });
    }

    private void handleAttemptedAction(PlayerActionInfo actionInfo) throws InterruptedException {
        gameState.attemptPlayerAction(actionInfo.getPlayerId(), actionInfo.getCardsToPut());
        if(gameState.checkGameOver()){
            notifyGameResult();
            Logger.log(getLoggingPrefix() + "Game over");
            return;
        }
        Thread.sleep(500);
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
