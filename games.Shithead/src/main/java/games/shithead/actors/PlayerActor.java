package games.shithead.actors;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import games.shithead.game.IGameCard;
import games.shithead.messages.AcceptedActionInfo;
import games.shithead.messages.AllocateIdRequest;
import games.shithead.messages.PlayerIdMessage;
import games.shithead.messages.PrivateDealMessage;
import games.shithead.messages.PublicDealMessage;
import games.shithead.messages.ReceivedCardsMessage;
import games.shithead.messages.PlayerActionInfo;
import games.shithead.messages.RegisterPlayerMessage;

public abstract class PlayerActor extends AbstractActor {

    private int playerId = -1;
    private int numberOfPlayers = -1;
    private Deque<Integer> playingQueue = null;

    //FIXME: Getters
    protected List<IGameCard> handCards;
    protected List<IGameCard> revealedTableCards;

    public PlayerActor(){
        ActorSelection gameActor = ShitheadActorSystem.INSTANCE.getActorSystem()
                .actorSelection(ShitheadActorSystem.getActorUrl(ShitheadActorSystem.GAME_MASTER_ACTOR_NAME));

        //upon creation all players ask the game actor to allocate player id so it's unique
        gameActor.tell(new AllocateIdRequest(), self());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                //after getting back the allocated id, we can register the player
                .match(PlayerIdMessage.class, this::receiveId)
                .match(PrivateDealMessage.class, this::receivePrivateDeal)
                .match(PublicDealMessage.class, this::receivePublicDeal)
                .match(AcceptedActionInfo.class, this::receiveAcceptedAction)
                .match(ReceivedCardsMessage.class, this::receiveCards)
                .matchAny(this::unhandled)
                .build();
    }
    
    public abstract String getName();
    
    private void receiveId(PlayerIdMessage idMessage) {
    	this.playerId = idMessage.playerId;
        sender().tell(new RegisterPlayerMessage(playerId, getName()), self());
    }
    
	private void receivePrivateDeal(PrivateDealMessage message) {
        //FIXME: LinkedList?
        this.handCards = new ArrayList<IGameCard>();
        this.revealedTableCards = new ArrayList<IGameCard>();
        
        chooseRevealedTableCards(message.getCards());
        
        //TODO: send revealed table cards
	}
    
	protected abstract void chooseRevealedTableCards(List<IGameCard> cards);

	private void receivePublicDeal(PublicDealMessage message) {
		this.numberOfPlayers = message.getNumberOfPlayers();
		this.playingQueue = message.getPlayingQueue();
		
		handlePublicDeal();
		
		if(isPlayersTurn()) {
			attemptMove();
		}
		else {
			considerInterruption();
		}
	}

	private boolean isPlayersTurn() {
		return playingQueue.getFirst() == playerId;
	}

	protected abstract void handlePublicDeal();
	
    private void attemptMove(){
        sender().tell(getPlayerMove(), self());
    }

    protected abstract PlayerActionInfo getPlayerMove();
    
	protected abstract void considerInterruption();

	private void receiveAcceptedAction(AcceptedActionInfo message) {
		updateCards();
		updatePlayingQueue();
	}

	private void updateCards() {
		// TODO Auto-generated method stub
		
	}
    
	private void updatePlayingQueue() {
		// TODO Auto-generated method stub
		
	}

	private void receiveCards(ReceivedCardsMessage message) {
		for(IGameCard card : message.getCards()) {
			this.handCards.add(card);
		}
	}
}
