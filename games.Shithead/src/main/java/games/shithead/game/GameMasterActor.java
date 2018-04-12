package games.shithead.game;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import games.shithead.log.Logger;
import games.shithead.messages.GameResult;
import games.shithead.messages.StartGameMessage;

public class GameMasterActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StartGameMessage.class, this::startGame)
                .match(GameResult.class, this::logGameResult)
                .matchAny(this::unhandled)
                .build();
    }

    private void startGame(StartGameMessage startGame) {
    	Logger.log(getLoggingPrefix() + "Received StartGameMessage");
    	
        ActorSelection gameActor = ShitheadActorSystem.INSTANCE.getActorSystem()
                .actorSelection(ShitheadActorSystem.getActorUrl(ShitheadActorSystem.GAME_ACTOR_NAME));
        Logger.log(getLoggingPrefix() + "Sending StartGameMessage to GameActor");
        gameActor.tell(startGame, self());
    }

    private void logGameResult(GameResult result) {
        System.out.println("Player " + result.getLosingPlayerName() + " (" + result.getLosingPlayerId() + ") lost");
    }
    
    private String getLoggingPrefix() {
    	return "GameMasterActor: ";
    }

}
