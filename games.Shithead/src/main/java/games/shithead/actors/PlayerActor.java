package games.shithead.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import games.shithead.game.CardStatus;
import games.shithead.game.GameState;
import games.shithead.messages.AllocateIdRequest;
import games.shithead.messages.PlayerIdMessage;
import games.shithead.messages.PlayersOrderMessage;
import games.shithead.messages.PrivateDealMessage;
import games.shithead.messages.PublicDealMessage;
import games.shithead.messages.NotifyPlayersTurnMessage;
import games.shithead.messages.PlayerActionInfo;
import games.shithead.messages.RegisterPlayerMessage;

public abstract class PlayerActor extends AbstractActor {

    private String playerName;
    private int playerId = -1;
    
    private CardStatus[] cardStatuses;

    public PlayerActor(String name){
        this.playerName = name;
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
                .match(PlayersOrderMessage.class, this::receivePlayersOrder)
                .match(NotifyPlayersTurnMessage.class, this::makeMove)
                .matchAny(this::unhandled)
                .build();
    }
    
    private void receiveId(PlayerIdMessage idMessage) {
    	this.playerId = idMessage.playerId;
        sender().tell(new RegisterPlayerMessage(playerId, playerName), self());
    }

    private void makeMove(NotifyPlayersTurnMessage notification){
        if(playerId != notification.playerToNotify){
            System.out.println("Got request to make a move with wrong id");
            return;
        }
        sender().tell(getPlayerMove(), self());
    }

    // these two methods are to be implemented by the players
    protected abstract PlayerActionInfo getPlayerMove();

    public abstract void analyzeGameState(GameState gameState);
}
