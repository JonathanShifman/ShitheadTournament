package games.shithead.main;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import games.shithead.game.GameActor;
import games.shithead.game.GameMasterActor;
import games.shithead.game.ShitheadActorSystem;
import games.shithead.messages.StartGameMessage;
import games.shithead.players.SimplePlayerActor;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Main {

	static Logger logger = LogManager.getLogger(Main.class);

	public static void main(String[] args) throws InterruptedException {
		logger.info("Main class started");
		ActorSystem actorSystem = ShitheadActorSystem.INSTANCE.getActorSystem();
		ActorRef gmActor = actorSystem.actorOf(Props.create(GameMasterActor.class), ShitheadActorSystem.GAME_MASTER_ACTOR_NAME);
		actorSystem.actorOf(Props.create(GameActor.class), ShitheadActorSystem.GAME_ACTOR_NAME);
		
		logger.info("Initializing players");
		// FIXME: Load from config
		int numberOfPlayers = 2;
		for(int i = 0; i < numberOfPlayers; i++) {
			String name = "SimplePlayerActor" + i;
			logger.info("Initializing player " + name);
			actorSystem.actorOf(Props.create(SimplePlayerActor.class), "SimplePlayerActor" + i);
		}

		Thread.sleep(2000);

		logger.info("Sending StartGameMessage to GameMasterActor");
		gmActor.tell(new StartGameMessage(), ActorRef.noSender());

		while (true){
			Thread.sleep(1000);
		}
		
	}

}
