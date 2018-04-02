package games.shithead.players;

import java.util.*;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import games.shithead.game.IGameCard;
import games.shithead.game.IPlayerInfo;
import games.shithead.game.InfoProvider;
import games.shithead.game.ShitheadActorSystem;
import games.shithead.messages.AcceptedActionMessage;
import games.shithead.messages.AllocateIdRequest;
import games.shithead.messages.PlayerIdMessage;
import games.shithead.messages.ChooseTableCardsMessage;
import games.shithead.messages.StartCycleMessage;
import games.shithead.messages.ReceivedCardsMessage;
import games.shithead.messages.PlayerActionInfo;
import games.shithead.messages.RegisterPlayerMessage;
import games.shithead.messages.TableCardsSelectionMessage;

public abstract class PlayerActor extends AbstractActor {

    protected int playerId = -1;
    protected Map<Integer, IPlayerInfo> players = new HashMap<>();

	protected List<IGameCard> handCards;
	protected List<IGameCard> revealedTableCards;
	protected List<IGameCard> pendingSelectionCards;

	protected List<IGameCard> pile;

    public PlayerActor(){
        ActorSelection gameActor = ShitheadActorSystem.INSTANCE.getActorSystem()
                .actorSelection(ShitheadActorSystem.getActorUrl(ShitheadActorSystem.GAME_ACTOR_NAME));

        //upon creation all players ask the game actor to allocate player id so it's unique
        gameActor.tell(new AllocateIdRequest(), self());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(PlayerIdMessage.class, this::receiveId)
                .match(ChooseTableCardsMessage.class, this::receiveChooseTableCardsMessage)
                .match(StartCycleMessage.class, this::receiveStartCycleMessage)
                .match(AcceptedActionMessage.class, this::receiveAcceptedAction)
                .match(ReceivedCardsMessage.class, this::receiveCards)
                .matchAny(this::unhandled)
                .build();
    }
    
    public abstract String getName();
    
    public String getLoggingPrefix() {
    	return "[" + playerId + "] " + getName() + ": ";
    }

    protected void updateInfo() {
    	players = InfoProvider.getPlayerInfos();
		handCards = players.get(playerId).getHandCards();
		revealedTableCards = players.get(playerId).getRevealedTableCards();
		pendingSelectionCards = players.get(playerId).getPendingSelectionCards();

    	pile = InfoProvider.getPile();
	}
    
    private void receiveId(PlayerIdMessage idMessage) {
    	this.playerId = idMessage.getPlayerId();
        sender().tell(new RegisterPlayerMessage(playerId, getName()), self());
    }
    
	private void receiveChooseTableCardsMessage(ChooseTableCardsMessage message) {
    	updateInfo();
        List<Integer> chosenRevealedTableCardIds = chooseRevealedTableCards(pendingSelectionCards);
        sender().tell(new TableCardsSelectionMessage(chosenRevealedTableCardIds, this.playerId), self());
	}
    
	protected abstract List<Integer> chooseRevealedTableCards(List<IGameCard> cards);

	private void receiveStartCycleMessage(StartCycleMessage message) {
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

    protected abstract PlayerActionInfo getPlayerMove();
    
	protected abstract void considerInterruption();

	private void receiveAcceptedAction(AcceptedActionMessage message) {
		takeAction();
	}

	private void receiveCards(ReceivedCardsMessage message) {
		for(IGameCard card : message.getCards()) {
			this.handCards.add(card);
		}
	}
}
