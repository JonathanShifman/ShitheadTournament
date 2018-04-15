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

public abstract class PlayerActor extends AbstractActor {

    protected int playerId = -1;
    protected Map<Integer, IPlayerHand> players = new HashMap<>();

	protected List<IGameCard> handCards;
	protected List<IGameCard> revealedTableCards;
	protected List<IGameCard> pendingSelectionCards;

	protected List<IGameCard> pile;

	protected int nextMoveId;

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
    
    public abstract String getName();
    
    protected void updateInfo() {
		nextMoveId = InfoProvider.getNextMoveId();

    	players = InfoProvider.getPlayerInfos();
		handCards = players.get(playerId).getHandCards();
		revealedTableCards = players.get(playerId).getRevealedTableCards();
		pendingSelectionCards = players.get(playerId).getPendingSelectionCards();

    	pile = InfoProvider.getPile();
	}
    
    private void receiveId(PlayerIdMessage idMessage) {
    	this.playerId = idMessage.getPlayerId();
    }
    
	private void receiveChooseTableCardsMessage(ChooseRevealedTableCardsMessage message) {
    	updateInfo();
        List<Integer> chosenRevealedTableCardIds = chooseRevealedTableCards(
        		pendingSelectionCards, message.getRevealedCardsToBeChosen());
        sender().tell(new TableCardsSelectionMessage(chosenRevealedTableCardIds), self());
	}
    
	protected abstract List<Integer> chooseRevealedTableCards(List<IGameCard> cards, int numOfRevealedTableCardsToChoose);

	private void receiveMoveRequestMessage(MoveRequestMessage message) {
		takeAction();
	}
	
	private void takeAction() {
		updateInfo();
		if(InfoProvider.getCurrentPlayerTurn() == playerId) {
			makeMove();
		}
		else {
			considerInterruption();
		}
	}
	
    private void makeMove(){
        sender().tell(getPlayerMove(), self());
    }

    protected abstract PlayerActionMessage getPlayerMove();
    
	protected void considerInterruption() {
		List<IGameCard> interruptionCards = getInterruptionCards();
		if(interruptionCards == null) {
			return;
		}
		List<Integer> interruptionCardIds = interruptionCards.stream()
				.map(card -> card.getUniqueId())
				.collect(Collectors.toList());
		sender().tell(new PlayerActionMessage(interruptionCardIds, nextMoveId), self());
	}

	protected abstract List<IGameCard> getInterruptionCards();
}
