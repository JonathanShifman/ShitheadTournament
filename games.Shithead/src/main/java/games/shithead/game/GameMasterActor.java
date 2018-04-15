package games.shithead.game;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import games.shithead.messages.GameResult;
import games.shithead.messages.StartGameMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GameMasterActor extends AbstractActor {

    static Logger logger = LogManager.getLogger(GameMasterActor.class);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StartGameMessage.class, this::startGame)
                .match(GameResult.class, this::logGameResult)
                .matchAny(this::unhandled)
                .build();
    }

    private void startGame(StartGameMessage startGame) {
        logger.info("Received StartGameMessage");
    	
        ActorSelection gameActor = ShitheadActorSystem.INSTANCE.getActorSystem()
                .actorSelection(ShitheadActorSystem.getActorUrl(ShitheadActorSystem.GAME_ACTOR_NAME));
        logger.info("Sending StartGameMessage to GameActor");
        gameActor.tell(startGame, self());
    }

    private void logGameResult(GameResult result) {
        System.out.println("Player " + result.getLosingPlayerName() + " (" + result.getLosingPlayerId() + ") lost");
    }

}
