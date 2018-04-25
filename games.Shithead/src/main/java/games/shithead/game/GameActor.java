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
        playerIdsToInfos = new HashMap<>();
        playerRefsToInfos = new HashMap<>();
    }

    public Receive createReceive() {
        return receiveBuilder()
                .match(RegisterPlayerMessage.class, this::registerPlayer)
                .match(StartGameMessage.class, this::startGame)
                .match(TableCardsSelectionMessage.class, this::receiveTableCardsSelection)
                .match(PlayerMoveMessage.class, this::handleAttemptedAction)
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
        sendChooseRevealedTableCardsMessages();
    }

    private void sendChooseRevealedTableCardsMessages() {
        playerIdsToInfos.keySet().forEach(id -> sendChooseRevealedTableCardsMessage(id));
    }

    private void sendChooseRevealedTableCardsMessage(Integer id) {
        playerIdsToInfos.get(id).getPlayerRef().tell(new ChooseRevealedTableCardsMessage(
                gameState.getPrivateHand(id).getPendingSelectionCards(), gameState.getRevealedCardsAtGameStart()), self());
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
            sendPostMoveMessages();
        }
    }

    private void sendPostMoveMessages() {
        playerIdsToInfos.keySet().forEach(playerId -> sendPostMoveMessage(playerId));
    }

    private void sendPostMoveMessage(Integer playerId) {
        Map<Integer, IPlayerHand> playerHands = new HashMap<>();
        playerIdsToInfos.keySet().forEach(id -> {
            if(id == playerId) {
                playerHands.put(id, gameState.getPrivateHand(id));
            }
            else {
                playerHands.put(id, gameState.getPublicHand(id));
            }
        });
        PostMoveMessage message = new PostMoveMessage(playerHands, gameState.getPile(),
                gameState.getCurrentMoveId(), gameState.getCurrentPlayerTurn());
        playerIdsToInfos.get(playerId).getPlayerRef().tell(message, self());
    }

    private void handleAttemptedAction(PlayerMoveMessage actionInfo) throws InterruptedException {
        logger.info("Received PlayerMoveMessage");
        if(!playerExists(getSender())) {
            logger.info("Unregistered Player, ignoring message");
            return;
        }
        int playerId = playerRefsToInfos.get(getSender()).getPlayerId();
        try {
            gameState.performPlayerAction(playerId, actionInfo.getPlayerActionInfo(), actionInfo.getMoveId());
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
        sendPostMoveMessages();
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
