package games.shithead.players;

import java.util.*;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import games.shithead.game.actors.ShitheadActorSystem;
import games.shithead.game.entities.PlayerActionInfo;
import games.shithead.game.interfaces.IGameCard;
import games.shithead.game.interfaces.IPlayerState;
import games.shithead.utils.ConstantsProvider;
import games.shithead.utils.LoggingUtils;
import games.shithead.messages.*;
import games.shithead.messages.PlayerActionMessage;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * A generic class for players to extend.
 * Implements basic functionality, such as handling the Akka messages, updating and storing the game info.
 * The goal of this class is to hide the technical implementation from the individual players and leave
 * only the implementation of the strategy.
 */
public abstract class PlayerActor extends AbstractActor {

	// The logger is not static because a separate instance is required for each player instance
	protected Logger logger;

	// The player's id
    protected int playerId = -1;

    /* Used to store mappings between each player's id to their state.
     * This map contains public info only, that is to say the cards
     * that other players holds in their hand have their card faces nullified.
     * Updated before each time a player is supposed to take an action. */
    protected Map<Integer, IPlayerState> playerStates = new HashMap<>();

    /* These fields hold the cards that are in the player's possession
     * at this time.
     * Updated before each time a player is supposed to take an action. */
	protected List<IGameCard> handCards;
	protected List<IGameCard> visibleTableCards;
	protected List<IGameCard> hiddenTableCards;
	protected List<IGameCard> pendingSelectionCards;

	/* Contains the cards that are currently in the pile.
	 * Updated before each time a player is supposed to take an action. */
	protected List<IGameCard> pile;

	// The id of the next move. Used to prevent ambiguity in case an action message arrives too late.
	protected int nextMoveId;

	// The id of the player whose turn it is to play nwo.
	protected int nextPlayerTurn;

    public PlayerActor(){
        ActorSelection gameActor = ShitheadActorSystem.INSTANCE.getActorSystem()
                .actorSelection(ShitheadActorSystem.getActorUrl(ShitheadActorSystem.GAME_ACTOR_NAME));

        gameActor.tell(new RegisterPlayerMessage(getName()), self());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(PlayerIdMessage.class, this::receiveId)
                .match(ChooseVisibleTableCardsMessage.class, this::receiveChooseTableCardsMessage)
                .match(SnapshotMessage.class, this::receiveSnapshotMessage)
                .matchAny(this::unhandled)
                .build();
    }

	/**
	 * @return The player's name
	 */
	public abstract String getName();


	/**
	 * Updates the game info from the contents of the SnapshotMessage
	 */
    protected void updateInfo(SnapshotMessage snapshotMessage) {
		playerStates = snapshotMessage.getPlayerStates();
		handCards = playerStates.get(playerId).getHandCards();
		visibleTableCards = playerStates.get(playerId).getVisibleTableCards();
		hiddenTableCards = playerStates.get(playerId).getHiddenTableCards();
		pendingSelectionCards = playerStates.get(playerId).getPendingSelectionCards();

		nextMoveId = snapshotMessage.getNextMoveId();
		nextPlayerTurn = snapshotMessage.getNextPlayerTurnId();
    	pile = snapshotMessage.getPile();
	}

	/**
	 * Handler method for PlayerIdMessage.
	 * @param message A message containing the received player id
	 */
	private void receiveId(PlayerIdMessage message) {
		this.playerId = message.getPlayerId();
		addAppender(playerId);
    }

	private void addAppender(int playerId) {
		final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		final Configuration config = ctx.getConfiguration();

		Layout layout = PatternLayout.newBuilder()
				.withPattern("%d{HH:mm:ss.SSS} %-5level %c{1} - %msg%n")
				.build();

		Appender appender = FileAppender.newBuilder()
				.withName("player" + playerId + "appender")
				.withFileName(System.getenv(ConstantsProvider.SYSTEM_ENV_VAR_NAME) + "\\games.Shithead\\log\\player" + playerId + ".log")
				.withAppend(false)
				.withLayout(layout)
				.build();
		appender.start();
		config.addAppender(appender);
		AppenderRef ref = AppenderRef.createAppenderRef("File", null, null);
		AppenderRef[] refs = new AppenderRef[] {ref};

		LoggerConfig loggerConfig = LoggerConfig.createLogger(false, Level.INFO, "player" + playerId,
				"true", refs, null, config, null );
		loggerConfig.addAppender(appender, null, null);
		config.addLogger("player" + playerId, loggerConfig);
		ctx.updateLoggers();
		logger = ctx.getLogger("player" + playerId);
	}

	/**
	 * Handler method for ChooseVisibleTableCardsMessage.
	 * Sends back the visible table card ids, as chosen by the implementing player.
	 * @param message
	 */
	private void receiveChooseTableCardsMessage(ChooseVisibleTableCardsMessage message) {
        List<Integer> chosenVisibleTableCardIds = chooseVisibleTableCards(
        		message.getCardsPendingSelection(), message.getNumOfVisibleTableCardsToBeChosen(),
				message.getNumOfPlayers());
        sender().tell(new TableCardsSelectionMessage(chosenVisibleTableCardIds), self());
	}

	/**
	 * This method effectively chooses the visible table cards.
	 * To be implemented by each player according to their strategy.
	 * @param cardsToChooseFrom A list of the cards to choose from
	 * @param numOfVisibleTableCardsToChoose The number of visible table cards to choose from the list
	 * @param numOfPlayers The number of players in the game
	 * @return The ids of the chosen visible table cards
	 */
	protected abstract List<Integer> chooseVisibleTableCards(List<IGameCard> cardsToChooseFrom, int numOfVisibleTableCardsToChoose, int numOfPlayers);

	/**
	 * Handler method for SnapshotMessage.
	 * @param message A message representing a request for the player to make a move.
	 */
	private void receiveSnapshotMessage(SnapshotMessage message) {
		updateInfo(message);
		takeAction();
	}

	/**
	 * Takes the action required of the player at a given moment: Makes a move if it's his turn,
	 * or considers an interruption if it isn't.
	 */
	private void takeAction() {
		if(nextPlayerTurn == playerId) {
			logger.info("Player " + playerId + " state: " + playerStates.get(playerId).toString());
			logger.info("Pile: " + LoggingUtils.cardsToMinDescriptions(pile));
			makeMove();
		}
		else {
			considerInterruption();
		}
	}

	/**
	 * Sends the move to the game actor, as chosen by the implementing player.
	 */
    private void makeMove(){
        sender().tell(new PlayerActionMessage(getPlayerMove(), nextMoveId), self());
    }

	/**
	 * This method effectively calculates the move the player makes.
	 * To be implemented by each player according to their strategy.
	 * @return A PlayerActionInfo containing the chosen action.
	 */
    protected abstract PlayerActionInfo getPlayerMove();

	/**
	 * Sends the interruption (if one is being made) to the game actor,
	 * as chosen by the implementing player.
	 */
	protected void considerInterruption() {
		PlayerActionInfo playerInterruption = getPlayerInterruption();
		if(playerInterruption == null || playerInterruption.getCardsToPut().isEmpty()) {
			return;
		}
		sender().tell(new PlayerActionMessage(playerInterruption, nextMoveId), self());
	}

	/**
	 * This method effectively calculates the interruption the player makes, if he makes one at all.
	 * To be implemented by each player according to their strategy.
	 * A return value of null or an action with an empty cards list indicate no interruption.
	 * @return The PlayerActionInfo containing the chosen interruption.
	 */
	protected abstract PlayerActionInfo getPlayerInterruption();
}
