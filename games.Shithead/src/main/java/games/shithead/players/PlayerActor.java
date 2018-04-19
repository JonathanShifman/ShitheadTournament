package games.shithead.players;

import java.util.*;
import java.util.stream.Collectors;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import games.shithead.game.IGameCard;
import games.shithead.game.IPlayerHand;
import games.shithead.game.InfoProvider;
import games.shithead.game.ShitheadActorSystem;
import games.shithead.messages.*;
import games.shithead.messages.PlayerActionMessage;

/**
 * A generic class for players to extend.
 * Implements basic functionality, such as handling the Akka messages, updating and storing the game info.
 * The goal of this class is to hide the technical implementation from the individual players and leave
 * only the implementation of the strategy.
 */
public abstract class PlayerActor extends AbstractActor {

	// The player's id
    protected int playerId = -1;

    /* Used to store mappings between each player's id to their hand.
     * This map contains public info only, that is to say the cards
     * that each players holds in his hand have their card faces nullified.
     * Updated before each time a player is supposed to take an action. */
    protected Map<Integer, IPlayerHand> playerHands = new HashMap<>();

    /* These fields hold the card that are in the player's possession
     * at this time. This info is private and is only made available to each
     * player about his own cards.
     * Updated before each time a player is supposed to take an action. */
	protected List<IGameCard> handCards;
	protected List<IGameCard> revealedTableCards;
	protected List<IGameCard> pendingSelectionCards;

	/* Contains the cards that are currently in the pile.
	 * Updated before each time a player is supposed to take an action. */
	protected List<IGameCard> pile;

	// The id of the current move. Used to prevent ambiguity in case an action message arrives too late.
	protected int currentMoveId;

    public PlayerActor(){
        ActorSelection gameActor = ShitheadActorSystem.INSTANCE.getActorSystem()
                .actorSelection(ShitheadActorSystem.getActorUrl(ShitheadActorSystem.GAME_ACTOR_NAME));

        gameActor.tell(new RegisterPlayerMessage(getName()), self());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(PlayerIdMessage.class, this::receiveId)
                .match(ChooseRevealedTableCardsMessage.class, this::receiveChooseTableCardsMessage)
                .match(MoveRequestMessage.class, this::receiveMoveRequestMessage)
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
    protected void updateInfo() {
		currentMoveId = InfoProvider.getCurrentMoveId();

    	playerHands = InfoProvider.getPlayerInfos();
		handCards = playerHands.get(playerId).getHandCards();
		revealedTableCards = playerHands.get(playerId).getRevealedTableCards();
		pendingSelectionCards = playerHands.get(playerId).getPendingSelectionCards();

    	pile = InfoProvider.getPile();
	}

	/**
	 * Handler method for PlayerIdMessage.
	 * @param message A message containing the received player id
	 */
	private void receiveId(PlayerIdMessage message) {
    	this.playerId = message.getPlayerId();
    }

	/**
	 * Handler method for ChooseRevealedTableCardsMessage.
	 * Updates the game info, and sends back the revealed table card ids, as chosen
	 * by the implementing player.
	 * @param message
	 */
	private void receiveChooseTableCardsMessage(ChooseRevealedTableCardsMessage message) {
    	updateInfo();
        List<Integer> chosenRevealedTableCardIds = chooseRevealedTableCards(
        		pendingSelectionCards, message.getRevealedCardsToBeChosen());
        sender().tell(new TableCardsSelectionMessage(chosenRevealedTableCardIds), self());
	}

	/**
	 * This method effectively chooses the revealed table cards.
	 * To be implemented by each player according to their strategy.
	 * @param cards A list of the cards to choose from
	 * @param numOfRevealedTableCardsToChoose The number of revealed table cards to choose from the list
	 * @return The ids of the chosen revealed table cards
	 */
	protected abstract List<Integer> chooseRevealedTableCards(List<IGameCard> cards, int numOfRevealedTableCardsToChoose);

	/**
	 * Handler method for MoveRequestMessage.
	 * @param message A message representing a request for the player to make a move.
	 */
	private void receiveMoveRequestMessage(MoveRequestMessage message) {
		takeAction();
	}

	/**
	 * Takes the action required of the player at a given moment: Makes a move if it's his turn,
	 * or considers an interruption if it isn't.
	 */
	private void takeAction() {
		updateInfo();
		if(InfoProvider.getCurrentPlayerTurn() == playerId) {
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
        sender().tell(getPlayerMove(), self());
    }

	/**
	 * This method effectively calculates the move the player makes.
	 * To be implemented by each player according to their strategy.
	 * @return A PlayerActionMessage containing the chosen move.
	 */
    protected abstract PlayerActionMessage getPlayerMove();

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
		sender().tell(new PlayerActionMessage(interruptionCardIds, currentMoveId), self());
	}

	/**
	 * This method effectively calculates the move the player makes.
	 * To be implemented by each player according to their strategy.
	 * @return The list of cards the player chose to Interrupt with. An empty list or null indicate
	 * no interruption.
	 */
	protected abstract List<IGameCard> getInterruptionCards();
}
