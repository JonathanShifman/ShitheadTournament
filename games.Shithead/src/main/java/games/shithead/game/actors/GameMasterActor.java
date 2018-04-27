package games.shithead.game.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import games.shithead.messages.GameResult;
import games.shithead.messages.StartGameMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The game master actor. Responsible for letting the game actor know when it's time to start the game.
 */
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
        ActorSelection gameActor = ShitheadActorSystem.INSTANCE.getActorSystem()
                .actorSelection(ShitheadActorSystem.getActorUrl(ShitheadActorSystem.GAME_ACTOR_NAME));
        gameActor.tell(startGame, self());
    }

    private void logGameResult(GameResult result) {
        System.out.println("Player " + result.getLosingPlayerName() + " (" + result.getLosingPlayerId() + ") lost");
    }

}
