package games.shithead.actors;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import games.shithead.game.IGameCard;
import games.shithead.messages.AcceptedActionMessage;
import games.shithead.messages.AllocateIdRequest;
import games.shithead.messages.PlayerIdMessage;
import games.shithead.messages.PrivateDealMessage;
import games.shithead.messages.PublicDealMessage;
import games.shithead.messages.ReceivedCardsMessage;
import games.shithead.messages.PlayerActionInfo;
import games.shithead.messages.RegisterPlayerMessage;
import games.shithead.messages.TableCardsSelectionMessage;

public abstract class PlayerActor extends AbstractActor {

    private Optional<Integer> playerId = Optional.empty();
    private int numberOfPlayers = -1;
    private Deque<Integer> playingQueue = null;
    private int currentPlayerTurn = -1;

    //FIXME: Getters
    protected List<IGameCard> handCards;
    protected List<IGameCard> revealedTableCards;

    public PlayerActor(){
        ActorSelection gameActor = ShitheadActorSystem.INSTANCE.getActorSystem()
                .actorSelection(ShitheadActorSystem.getActorUrl(ShitheadActorSystem.GAME_ACTOR_NAME));

        //upon creation all players ask the game actor to allocate player id so it's unique
        System.out.println(getLoggingPrefix() + "Sending AllocateIdRequest");
        gameActor.tell(new AllocateIdRequest(), self());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                //after getting back the allocated id, we can register the player
                .match(PlayerIdMessage.class, this::receiveId)
                .match(PrivateDealMessage.class, this::receivePrivateDeal)
                .match(PublicDealMessage.class, this::receivePublicDeal)
                .match(AcceptedActionMessage.class, this::receiveAcceptedAction)
                .match(ReceivedCardsMessage.class, this::receiveCards)
                .matchAny(this::unhandled)
                .build();
    }
    
    public abstract String getName();
    
    public String getLoggingPrefix() {
    	return "[" + playerId.orElse(-1) + "] " + getName() + ": ";
    }
    
    private void receiveId(PlayerIdMessage idMessage) {
    	System.out.println(getLoggingPrefix() + "Received PlayerIdMessage");
    	this.playerId = Optional.of(idMessage.getPlayerId());
    	
    	System.out.println(getLoggingPrefix() + "Sending RegisterPlayerMessage");
        sender().tell(new RegisterPlayerMessage(playerId.get(), getName()), self());
    }
    
	private void receivePrivateDeal(PrivateDealMessage message) {
    	System.out.println(getLoggingPrefix() + "Received PrivateDealMessage");
    	
        //FIXME: LinkedList?
        this.handCards = new ArrayList<IGameCard>();
        this.revealedTableCards = new ArrayList<IGameCard>();
        
        List<Integer> chosenRevealedTableCardIds = chooseRevealedTableCards(message.getCards());
    	System.out.println(getLoggingPrefix() + "Chosen revealed table card ids: [0, 1, 2]"); //FIXME: Real ids

    	System.out.println(getLoggingPrefix() + "Sending TableCardsSelectionMessage");
        sender().tell(new TableCardsSelectionMessage(chosenRevealedTableCardIds), self());
	}
    
	protected abstract List<Integer> chooseRevealedTableCards(List<IGameCard> cards);

	private void receivePublicDeal(PublicDealMessage message) {
		this.numberOfPlayers = message.getNumberOfPlayers();
		this.playingQueue = message.getPlayingQueue();
		this.currentPlayerTurn = playingQueue.getFirst();
		
		handlePublicDeal();
		takeAction();
	}

	protected abstract void handlePublicDeal();
	
	private void takeAction() {
		if(isPlayersTurn()) {
			attemptMove();
		}
		else {
			considerInterruption();
		}
	}

	private boolean isPlayersTurn() {
		return playingQueue.getFirst() == playerId.get();
	}
	
    private void attemptMove(){
        sender().tell(getPlayerMove(), self());
    }

    protected abstract PlayerActionInfo getPlayerMove();
    
	protected abstract void considerInterruption();

	private void receiveAcceptedAction(AcceptedActionMessage message) {
		if(message.getPlayerId() == playerId.get()) {
			removeCards(message.getCards());
		}
		currentPlayerTurn = message.getNextPlayerTurn();
		
		takeAction();
	}

	private void removeCards(List<IGameCard> cards) {
		//FIXME: More efficient
		for(IGameCard card : cards) {
			removeCard(card.getUniqueId());
		}
		
	}

	private void removeCard(int uniqueId) {
		//FIXME: Code duplication
		for(IGameCard card : handCards) {
			if(card.getUniqueId() == uniqueId) {
				handCards.remove(card);
				return;
			}
		}
		for(IGameCard card : revealedTableCards) {
			if(card.getUniqueId() == uniqueId) {
				revealedTableCards.remove(card);
				return;
			}
		}
		
	}

	private void receiveCards(ReceivedCardsMessage message) {
		for(IGameCard card : message.getCards()) {
			this.handCards.add(card);
		}
	}
}
