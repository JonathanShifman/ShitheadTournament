package games.shithead.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import games.shithead.gameManagement.GameResult;
import games.shithead.gameManagement.StartGame;

public class GameMasterActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StartGame.class, this::startGame)
                .match(GameResult.class, this::logGameResult)
                .matchAny(this::unhandled)
                .build();
    }

    private void startGame(StartGame startGame) {
        ActorSelection gameActor = ShitheadActorSystem.INSTANCE.getActorSystem()
                .actorSelection(ShitheadActorSystem.getActorUrl(ShitheadActorSystem.GAME_ACTOR_NAME));

        gameActor.tell(startGame, self());
    }

    private void logGameResult(GameResult result) {
        System.out.println("Player " + result.losingPlayerName + " (" + result.losingPlayerId + ") lost");
    }

}
