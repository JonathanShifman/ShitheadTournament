package games.shithead.main;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import games.shithead.actors.GameActor;
import games.shithead.actors.GameMasterActor;
import games.shithead.actors.ShitheadActorSystem;
import games.shithead.gameManagement.StartGame;

public class Main {

	public static void main(String[] args) throws InterruptedException {
		ActorSystem actorSystem = ShitheadActorSystem.INSTANCE.getActorSystem();
		ActorRef gmActor = actorSystem.actorOf(Props.create(GameMasterActor.class), ShitheadActorSystem.GAME_MASTER_ACTOR_NAME);
		actorSystem.actorOf(Props.create(GameActor.class), ShitheadActorSystem.GAME_ACTOR_NAME);

		Thread.sleep(5000);

		gmActor.tell(new StartGame(), ActorRef.noSender());

		while (true){
			Thread.sleep(1000);
		}
		//runSeriesOfGames(1);
	}

	private static void runSeriesOfGames(int numberOfGames) {
		for(int i = 0; i < numberOfGames; i++) {
//			IShitheadGame game = new ShitheadGame(4);
//			game.run();
		}
	}

}
