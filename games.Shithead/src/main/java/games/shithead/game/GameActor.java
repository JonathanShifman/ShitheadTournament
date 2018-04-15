package games.shithead.game;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import games.shithead.messages.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class GameActor extends AbstractActor {

    static Logger logger = LogManager.getLogger(GameActor.class);

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

    public Receive createReceive() {
        return receiveBuilder()
                .match(RegisterPlayerMessage.class, this::registerPlayer)
                .match(StartGameMessage.class, this::startGame)
                .match(TableCardsSelectionMessage.class, this::receiveTableCardsSelection)
                .match(PlayerActionMessage.class, this::handleAttemptedAction)
                .matchAny(this::unhandled)
                .build();
    }

	private void registerPlayer(RegisterPlayerMessage playerRegistration) {
        logger.info("Received RegisterPlayerMessage");
        if(gameState.isGameStarted()){
            logger.info("Game has already started, too late for registration");
            return;
        }
        if(playerExists(getSender())){
            logger.info("Player has already been registered");
            return;
        }

        logger.info("Registering player");
        int playerId = playerIdAllocator++;
        IPlayerInfo playerInfo = new PlayerInfo(playerId, playerRegistration.getPlayerName(), getSender());
        playerIdsToInfos.put(playerId, playerInfo);
        playerRefsToInfos.put(getSender(), playerInfo);
        gameState.addPlayer(playerId);
        logger.info("Sending PlayerIdMessage with playerId=" + playerId);
        getSender().tell(new PlayerIdMessage(playerId), self());
    }

    @SuppressWarnings("unused")
    private void startGame(StartGameMessage startGameMessage) {
        logger.info("Received StartGameMessage");
        // TODO: Make sure message came from Main
        if(gameState.isGameStarted()){
            logger.info("Game has already started");
            return;
        }
        if(!gameState.enoughPlayersToStartGame()){
            logger.info("Not enough players");
            return;
        }
        gameState.startGame();
        int revealedCardsAtGameStart = gameState.getRevealedCardsAtGameStart();
        distributeMessage(new ChooseRevealedTableCardsMessage(revealedCardsAtGameStart));
    }
    
    private void receiveTableCardsSelection(TableCardsSelectionMessage message) {
        logger.info("Received TableCardsSelectionMessage");
        if(!playerExists(getSender())) {
            logger.info("Unregistered Player, ignoring message");
            return;
        }
        try {
            gameState.performTableCardsSelection(playerRefsToInfos.get(getSender()).getPlayerId(), message.getSelectedCardsIds());
        }
    	catch(Exception e) {
            logger.info(e.getMessage());
            return;
        }
    	if(gameState.allPlayersSelectedTableCards()) {
    	    gameState.startCycle();
            distributeMessage(new MoveRequestMessage());
        }
    }

    private void handleAttemptedAction(PlayerActionMessage actionInfo) throws InterruptedException {
        logger.info("Received PlayerActionMessage");
        if(!playerExists(getSender())) {
            logger.info("Unregistered Player, ignoring message");
            return;
        }
        int playerId = playerRefsToInfos.get(getSender()).getPlayerId();
        try {
            gameState.performPlayerAction(playerId, actionInfo.getCardsToPut(), actionInfo.getMoveId(), actionInfo.getVictimId());
        }
        catch (Exception e) {
            logger.info(e.getMessage());
            return;
        }
        if(gameState.isGameOver()){
            distributeMessage(new GameResult(-1, ""));
            logger.info("Game over");
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
