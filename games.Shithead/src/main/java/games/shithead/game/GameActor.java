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
    private Map<Integer, IPlayerInfo> playerIdsToInfos;
    private Map<ActorRef, IPlayerInfo> playerRefsToInfos;

    private GameState gameState;

    public GameActor() {
        playerIdAllocator = 1;
        gameState = new GameState();
        InfoProvider.setGameState(gameState);
        playerIdsToInfos = new HashMap<>();
        playerRefsToInfos = new HashMap<>();
    }
    
    private String getLoggingPrefix() {
    	return "GameActor: ";
    }

    public Receive createReceive() {
        return receiveBuilder()
                .match(RegisterPlayerMessage.class, this::registerPlayer)
                .match(StartGameMessage.class, this::startGame)
                .match(TableCardsSelectionMessage.class, this::receiveTableCardsSelection)
                .match(PlayerActionInfo.class, this::handleAttemptedAction)
                .matchAny(this::unhandled)
                .build();
    }

	private void registerPlayer(RegisterPlayerMessage playerRegistration) {
        Logger.log(getLoggingPrefix() + "Received RegisterPlayerMessage");
        if(gameState.isGameStarted()){
            // Too late for registration
            return;
        }
        Logger.log(getLoggingPrefix() + "Registering player");
        int playerId = playerIdAllocator++;
        IPlayerInfo playerInfo = new PlayerInfo(playerId, playerRegistration.getPlayerName(), getSender());
        playerIdsToInfos.put(playerId, playerInfo);
        playerRefsToInfos.put(getSender(), playerInfo);
        gameState.addPlayer(playerId);
        Logger.log(getLoggingPrefix() + "Sending PlayerIdMessage with playerId=" + playerIdAllocator);
        getSender().tell(new PlayerIdMessage(playerId), self());
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
        distributeMessage(new ChooseTableCardsMessage());
    }
    
    private void receiveTableCardsSelection(TableCardsSelectionMessage message) {
        Logger.log(getLoggingPrefix() + "Received TableCardsSelectionMessage");
    	gameState.performTableCardsSelection(message.getPlayerId(), message.getSelectedCardsIds());
    	if(gameState.allPlayersSelectedTableCards()) {
    	    gameState.startCycle();
            distributeMessage(new StartCycleMessage());
        }
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
        distributeMessage(acceptedActionMessage);
	}

    private void notifyGameResult() {
        distributeMessage(new GameResult());
    }

    private void distributeMessage(Object message) {
        playerIdsToInfos.forEach((id, playerInfo) -> {
            playerInfo.getPlayerRef().tell(message, self());
        });
    }
}
