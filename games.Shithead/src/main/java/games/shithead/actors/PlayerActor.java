package games.shithead.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import games.shithead.game.GameState;
import games.shithead.game.PlayerTurnInfo;
import games.shithead.gameManagement.NotifyPlayersTurn;
import games.shithead.gameManagement.RegisterPlayer;
import games.shithead.gameManagement.AllocateIdRequest;
import games.shithead.gameManagement.IdMessage;

public abstract class PlayerActor extends AbstractActor {

    private String playerName;
    private int playerId = -1;

    public PlayerActor(String name){
        this.playerName = name;
        ActorSelection gameActor = ShitheadActorSystem.INSTANCE.getActorSystem()
                .actorSelection(ShitheadActorSystem.GAME_ACTOR_PATH);

        //upon creation all players ask the game actor to allocate player id so it's unique
        gameActor.tell(new AllocateIdRequest(), self());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                //after getting back the allocated id, we can register the player
                .match(IdMessage.class, idMessage -> {
                    this.playerId = idMessage.playerId;
                    sender().tell(new RegisterPlayer(playerId, playerName), self());
                })
                .match(GameState.class, this::analyzeGameState)
                .match(NotifyPlayersTurn.class, this::makeMove)
                .matchAny(this::unhandled)
                .build();
    }

    private  void makeMove(NotifyPlayersTurn notification){
        if(playerId!=notification.playerToNotify){
            System.out.println("Got request to make a move with wrong id");
            return;
        }
        sender().tell(getPlayerMove(), self());
    }

    // these two methods are to be implemented by the players
    protected abstract PlayerTurnInfo getPlayerMove();

    public abstract void analyzeGameState(GameState gameState);
}
