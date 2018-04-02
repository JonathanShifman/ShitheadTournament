package games.shithead.main;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import games.shithead.game.GameActor;
import games.shithead.game.GameMasterActor;
import games.shithead.players.RandomPlayerActor;
import games.shithead.game.ShitheadActorSystem;
import games.shithead.log.Logger;
import games.shithead.messages.StartGameMessage;
import games.shithead.players.SimplePlayerActor;

public class Main {

	public static void main(String[] args) throws InterruptedException {
		ActorSystem actorSystem = ShitheadActorSystem.INSTANCE.getActorSystem();
		ActorRef gmActor = actorSystem.actorOf(Props.create(GameMasterActor.class), ShitheadActorSystem.GAME_MASTER_ACTOR_NAME);
		actorSystem.actorOf(Props.create(GameActor.class), ShitheadActorSystem.GAME_ACTOR_NAME);
		
		Logger.log("Main: Initializing players");
		int numberOfPlayers = 2;
		for(int i = 0; i < numberOfPlayers; i++) {
			String name = "SimplePlayerActor" + i;
			Logger.log("Main: Initializing player " + name);
			actorSystem.actorOf(Props.create(SimplePlayerActor.class), "SimplePlayerActor" + i);
		}
		

		Thread.sleep(2000);

		Logger.log("Main: Sending StartGameMessage to GameMasterActor");
		gmActor.tell(new StartGameMessage(), ActorRef.noSender());

		while (true){
			Thread.sleep(1000);
		}
		
	}

}
