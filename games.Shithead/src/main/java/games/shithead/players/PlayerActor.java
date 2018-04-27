package games.shithead.players;

import java.util.*;
import java.util.stream.Collectors;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import games.shithead.game.actors.ShitheadActorSystem;
import games.shithead.game.entities.PlayerActionInfo;
import games.shithead.game.interfaces.IGameCard;
import games.shithead.game.interfaces.IPlayerState;
import games.shithead.game.logging.LoggingUtils;
import games.shithead.messages.*;
import games.shithead.messages.PlayerMoveMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A generic class for players to extend.
 * Implements basic functionality, such as handling the Akka messages, updating and storing the game info.
 * The goal of this class is to hide the technical implementation from the individual players and leave
 * only the implementation of the strategy.
 */
public abstract class PlayerActor extends AbstractActor {

	protected static Logger logger = LogManager.getLogger("Player");

	// The player's id
    protected int playerId = -1;

    /* Used to store mappings between each player's id to their state.
     * This map contains public info only, that is to say the cards
     * that each players holds in his hand have their card faces nullified.
     * Updated before each time a player is supposed to take an action. */
    protected Map<Integer, IPlayerState> playerStates = new HashMap<>();

    /* These fields hold the cards that are in the player's possession
     * at this time. This info is private and is only made available to each
     * player about their own cards.
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
	protected int nexttPlayerTurn;

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
	 * Returns the player's name
	 * @return The player's name
	 */
	public abstract String getName();


	/**
	 * Updates the game info using the InfoProvider class
	 */
    protected void updateInfo(SnapshotMessage snapshotMessage) {
		playerStates = snapshotMessage.getPlayerStates();
		handCards = playerStates.get(playerId).getHandCards();
		visibleTableCards = playerStates.get(playerId).getVisibleTableCards();
		hiddenTableCards = playerStates.get(playerId).getHiddenTableCards();
		pendingSelectionCards = playerStates.get(playerId).getPendingSelectionCards();

		nextMoveId = snapshotMessage.getNextMoveId();
		nexttPlayerTurn = snapshotMessage.getNextPlayerTurnId();
    	pile = snapshotMessage.getPile();
	}

	/**
	 * Handler method for PlayerIdMessage.
	 * @param message A message containing the received player id
	 */
	private void receiveId(PlayerIdMessage message) {
		this.playerId = message.getPlayerId();
    }

	/**
	 * Handler method for ChooseVisibleTableCardsMessage.
	 * Updates the game info, and sends back the visible table card ids, as chosen
	 * by the implementing player.
	 * @param message
	 */
	private void receiveChooseTableCardsMessage(ChooseVisibleTableCardsMessage message) {
        List<Integer> chosenVisibleTableCardIds = chooseVisibleTableCards(
        		message.getCardsPendingSelection(), message.getNumOfVisibleTableCardsToBeChosen());
        sender().tell(new TableCardsSelectionMessage(chosenVisibleTableCardIds), self());
	}

	/**
	 * This method effectively chooses the visible table cards.
	 * To be implemented by each player according to their strategy.
	 * @param cards A list of the cards to choose from
	 * @param numOfVisibleTableCardsToChoose The number of visible table cards to choose from the list
	 * @return The ids of the chosen visible table cards
	 */
	protected abstract List<Integer> chooseVisibleTableCards(List<IGameCard> cards, int numOfVisibleTableCardsToChoose);

	/**
	 * Handler method for SnapshotMessage.
	 * @param message A message representing a request for the player to make a move.
	 */
	private void receiveSnapshotMessage(SnapshotMessage message) {
		takeAction(message);
	}

	/**
	 * Takes the action required of the player at a given moment: Makes a move if it's his turn,
	 * or considers an interruption if it isn't.
	 */
	private void takeAction(SnapshotMessage message) {
		updateInfo(message);
		if(nexttPlayerTurn == playerId) {
			logger.info("Player " + playerId + " is making a move");
			logger.info("Player " + playerId + " state: " + playerStates.get(playerId).toString());
			logger.info("Pile: " + LoggingUtils.cardsToDescriptions(pile));
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
        sender().tell(new PlayerMoveMessage(getPlayerMove(), nextMoveId), self());
    }

	/**
	 * This method effectively calculates the move the player makes.
	 * To be implemented by each player according to their strategy.
	 * @return A PlayerMoveMessage containing the chosen move.
	 */
    protected abstract PlayerActionInfo getPlayerMove();

	/**
	 * Sends the interruption (if one is being made) to the game actor,
	 * as chosen by the implementing player.
	 */
	protected void considerInterruption() {
		List<IGameCard> interruptionCards = getInterruptionCards();
		if(interruptionCards == null || interruptionCards.isEmpty()) {
			return;
		}
		List<Integer> interruptionCardIds = interruptionCards.stream()
				.map(card -> card.getUniqueId())
				.collect(Collectors.toList());
		PlayerActionInfo playerActionInfo = new PlayerActionInfo(interruptionCardIds);
		sender().tell(new PlayerMoveMessage(playerActionInfo, nextMoveId), self());
	}

	/**
	 * This method effectively calculates the move the player makes.
	 * To be implemented by each player according to their strategy.
	 * @return The list of cards the player chose to Interrupt with. An empty list or null indicate
	 * no interruption.
	 */
	protected abstract List<IGameCard> getInterruptionCards();
}
