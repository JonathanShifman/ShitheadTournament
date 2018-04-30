package games.shithead.game.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import games.shithead.game.entities.GameState;
import games.shithead.game.interfaces.IPlayerInfo;
import games.shithead.game.interfaces.IPlayerState;
import games.shithead.game.entities.PlayerInfo;
import games.shithead.messages.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * The actor that runs the game flow. Responsible for operating the GameState instance and
 * communicate with the participating players.
 */
public class GameActor extends AbstractActor {

    static Logger logger = LogManager.getLogger("Game");

    // An allocator used to allocate incrementing ids to players
    int playerIdAllocator;

    // A mapping from each player id to the corresponding IPlayerInfo
    private Map<Integer, IPlayerInfo> playerIdsToInfos;

    // A mapping from each player ref to the corresponding IPlayerInfo
    private Map<ActorRef, IPlayerInfo> playerRefsToInfos;

    // The game state used to perform all game operations
    private GameState gameState;

    public GameActor() throws IOException {
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
                .match(PlayerActionMessage.class, this::handleAttemptedAction)
                .matchAny(this::unhandled)
                .build();
    }

    /**
     * Handler method for RegisterPlayerMessage.
     * Registers the new player, initializes the required information, and sends the
     * allocated id to the player.
     * @param message The registration message.
     */
	private void registerPlayer(RegisterPlayerMessage message) {
        logger.debug("Received RegisterPlayerMessage from " + message.getPlayerName());
        if(gameState.isGameStarted()){
            logger.debug("Game has already started, too late for registration");
            return;
        }
        if(playerExists(getSender())){
            logger.debug("Player has already been registered");
            return;
        }

        int playerId = playerIdAllocator++;
        logger.debug("Registering player with allocated id: " + playerId);
        IPlayerInfo playerInfo = new PlayerInfo(playerId, message.getPlayerName(), getSender());
        playerIdsToInfos.put(playerId, playerInfo);
        playerRefsToInfos.put(getSender(), playerInfo);
        gameState.addPlayer(playerId);
        logger.debug("Sending PlayerIdMessage with playerId: " + playerId);
        getSender().tell(new PlayerIdMessage(playerId), self());
    }

    /**
     * Handler method for StartGameMessage.
     * Starts the game and asks all players to choose their visible table cards.
     * @param message The message that tells the game actor to start the game.
     */
    private void startGame(StartGameMessage message) {
        logger.debug("Received StartGameMessage");
        // TODO: Make sure message came from Main
        if(gameState.isGameStarted()){
            logger.debug("Game has already started");
            return;
        }
        if(!gameState.enoughPlayersToStartGame()){
            logger.debug("Not enough players");
            return;
        }
        gameState.startGame();
        sendChooseVisibleTableCardsMessages();
    }

    /**
     * Sends each player a message asking him to choose his visible table cards
     */
    private void sendChooseVisibleTableCardsMessages() {
        logger.debug("Sending choose visible table cards messages");
        playerIdsToInfos.keySet().forEach(playerId -> sendChooseVisibleTableCardsMessage(playerId));
    }

    /**
     * Sends a single player a message asking him to choose his visible table cards
     * @param playerId The id of the player to send the message to
     */
    private void sendChooseVisibleTableCardsMessage(Integer playerId) {
        playerIdsToInfos.get(playerId).getPlayerRef().tell(new ChooseVisibleTableCardsMessage(
                gameState.getPrivateState(playerId).getPendingSelectionCards(),
                gameState.getNumOfVisibleTableCardsAtGameStart(), playerIdsToInfos.size()), self());
    }

    /**
     * Handler method for TableCardsSelectionMessage.
     * Receives the selection, validates it and sends the first SnapshotMessage (effectively starting the game cycle).
     * @param message The message containing the table cards selection.
     */
    private void receiveTableCardsSelection(TableCardsSelectionMessage message) {
        logger.debug("Received TableCardsSelectionMessage");
        if(!playerExists(getSender())) {
            logger.debug("Unregistered Player, ignoring message");
            return;
        }
        int playerId = playerRefsToInfos.get(getSender()).getPlayerId();
        try {
            gameState.performTableCardsSelection(playerId, message.getSelectedCardsIds());
        }
    	catch(Exception e) {
            logger.info(e.getMessage());
            return;
        }
    	if(gameState.allPlayersSelectedTableCards()) {
    	    gameState.startCycle();
            sendSnapshotMessages();
        }
    }

    /**
     * Sends each player the appropriate snapshot message.
     */
    private void sendSnapshotMessages() {
        logger.debug("Sending snapshot messages");
        playerIdsToInfos.keySet().forEach(playerId -> sendSnapshotMessage(playerId));
    }

    /**
     * Sends a single player the appropriate snapshot message
     * @param playerId The id of the player to send the message to
     */
    private void sendSnapshotMessage(Integer playerId) {
        Map<Integer, IPlayerState> playerStates = new HashMap<>();
        playerIdsToInfos.keySet().forEach(id -> {
            if(id == playerId) {
                playerStates.put(id, gameState.getPrivateState(id));
            }
            else {
                playerStates.put(id, gameState.getPublicState(id));
            }
        });
        SnapshotMessage message = new SnapshotMessage(playerStates, gameState.getPile(),
                gameState.getNextMoveId(), gameState.getNextPlayerTurn());
        playerIdsToInfos.get(playerId).getPlayerRef().tell(message, self());
    }

    /**
     * Handler message for PlayerActionMessage.
     * Receives an attempted player action, performs it if valid, advances the playing queue and
     * sends the next snapshot message.
     * @param message The action message
     * @throws InterruptedException
     */
    private void handleAttemptedAction(PlayerActionMessage message) throws InterruptedException {
        logger.debug("Received PlayerActionMessage with move id: " + message.getMoveId());
        if(!playerExists(getSender())) {
            logger.debug("Unregistered Player, ignoring message");
            return;
        }
        int playerId = playerRefsToInfos.get(getSender()).getPlayerId();
        try {
            gameState.performPlayerAction(playerId, message.getPlayerActionInfo(), message.getMoveId());
        }
        catch (Exception e) {
            logger.info(e.getMessage());
            return;
        }
        if(gameState.isGameOver()){
            logger.info("Game over");
            return;
        }
        Thread.sleep(500);
        sendSnapshotMessages();
    }

    /**
     * Distributes the given message to all players.
     * @param message The message to distribute
     */
    private void distributeMessage(Object message) {
        playerIdsToInfos.forEach((id, playerInfo) -> {
            playerInfo.getPlayerRef().tell(message, self());
        });
    }

    /**
     * Checks if a player with the given player id exists.
     * @param playerId The player id to look for
     * @return True if the player exists, false otherwise
     */
    private boolean playerExists(int playerId) {
        return playerIdsToInfos.containsKey(playerId);
    }

    /**
     * Checks if a player with the given player ref exists.
     * @param playerRef The player ref to look for
     * @return True if the player exists, false otherwise
     */
    private boolean playerExists(ActorRef playerRef) {
        return playerRefsToInfos.containsKey(playerRef);
    }
}
