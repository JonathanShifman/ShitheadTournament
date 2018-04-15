package games.shithead.game;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import games.shithead.log.Logger;
import games.shithead.messages.*;
//import org.apache.logging.log4j.Logger;
//import org.apache.logging.log4j.LogManager;

import java.util.*;

public class GameActor extends AbstractActor {

//  static Logger logger = LogManager.getLogger(GameActor.class);

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
            Logger.log(getLoggingPrefix() + "Game has already started, too late for registration");
            return;
        }
        if(playerExists(getSender())){
            Logger.log(getLoggingPrefix() + "Player has already been registered");
            return;
        }

        Logger.log(getLoggingPrefix() + "Registering player");
        int playerId = playerIdAllocator++;
        IPlayerInfo playerInfo = new PlayerInfo(playerId, playerRegistration.getPlayerName(), getSender());
        playerIdsToInfos.put(playerId, playerInfo);
        playerRefsToInfos.put(getSender(), playerInfo);
        gameState.addPlayer(playerId);
        Logger.log(getLoggingPrefix() + "Sending PlayerIdMessage with playerId=" + playerId);
        getSender().tell(new PlayerIdMessage(playerId), self());
    }

    @SuppressWarnings("unused")
    private void startGame(StartGameMessage startGameMessage) {
        Logger.log(getLoggingPrefix() + "Received StartGameMessage");
//      Logger.log("Warn visible");
        // TODO: Make sure message came from Main
        if(gameState.isGameStarted()){
            Logger.log(getLoggingPrefix() + "Game has already started");
            return;
        }
        if(!gameState.enoughPlayersToStartGame()){
            Logger.log(getLoggingPrefix() + "Not enough players");
            return;
        }
        gameState.startGame();
        int revealedCardsAtGameStart = gameState.getRevealedCardsAtGameStart();
        distributeMessage(new ChooseRevealedTableCardsMessage(revealedCardsAtGameStart));
    }
    
    private void receiveTableCardsSelection(TableCardsSelectionMessage message) {
        Logger.log(getLoggingPrefix() + "Received TableCardsSelectionMessage");
        if(!playerExists(getSender())) {
            Logger.log(getLoggingPrefix() + "Unregistered Player, ignoring message");
            return;
        }
        try {
            gameState.performTableCardsSelection(playerRefsToInfos.get(getSender()).getPlayerId(), message.getSelectedCardsIds());
        }
    	catch(Exception e) {
            Logger.log(getLoggingPrefix() + e.getMessage());
            return;
        }
    	if(gameState.allPlayersSelectedTableCards()) {
    	    gameState.startCycle();
            distributeMessage(new MoveRequestMessage());
        }
    }

    private void handleAttemptedAction(PlayerActionInfo actionInfo) throws InterruptedException {
        Logger.log(getLoggingPrefix() + "Received PlayerActionInfo");
        if(!playerExists(getSender())) {
            Logger.log(getLoggingPrefix() + "Unregistered Player, ignoring message");
            return;
        }
        int playerId = playerRefsToInfos.get(getSender()).getPlayerId();
        try {
            gameState.performPlayerAction(playerId, actionInfo.getCardsToPut(), actionInfo.getMoveId());
        }
        catch (Exception e) {
            Logger.log(getLoggingPrefix() + e.getMessage());
            return;
        }
        if(gameState.isGameOver()){
            distributeMessage(new GameResult(-1, ""));
            Logger.log(getLoggingPrefix() + "Game over");
            return;
        }
        Thread.sleep(500);
        distributeMessage(new MoveRequestMessage());
    }

    private void distributeMessage(Object message) {
        playerIdsToInfos.forEach((id, playerInfo) -> {
            playerInfo.getPlayerRef().tell(message, self());
        });
    }

    private boolean playerExists(int playerId) {
        return playerIdsToInfos.containsKey(playerId);
    }

    private boolean playerExists(ActorRef playerRef) {
        return playerRefsToInfos.containsKey(playerRef);
    }
}
