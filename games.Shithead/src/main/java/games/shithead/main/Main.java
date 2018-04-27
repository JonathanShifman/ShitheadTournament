package games.shithead.main;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import games.shithead.game.actors.GameActor;
import games.shithead.game.actors.GameMasterActor;
import games.shithead.game.actors.ShitheadActorSystem;
import games.shithead.messages.StartGameMessage;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Main {

	static Logger logger = LogManager.getLogger("Game");

	public static void main(String[] args) throws InterruptedException {
		logger.info("Main class started");
		ActorSystem actorSystem = ShitheadActorSystem.INSTANCE.getActorSystem();
		ActorRef gmActor = actorSystem.actorOf(Props.create(GameMasterActor.class), ShitheadActorSystem.GAME_MASTER_ACTOR_NAME);
		actorSystem.actorOf(Props.create(GameActor.class), ShitheadActorSystem.GAME_ACTOR_NAME);
		
		logger.info("Initializing players");
		// FIXME: Load from config
		int numberOfPlayers = 3;
		for(int i = 0; i < numberOfPlayers; i++) {
			String className = "SimplePlayerActor";
			String name = className + (i+1);
			logger.info("Initializing player " + name);
			try {
				actorSystem.actorOf(Props.create(
						Class.forName("games.shithead.players." + className)), name);
			}
			catch (Exception e) {
				logger.error(e.getMessage());
			}
		}

		Thread.sleep(2000);

		logger.info("Sending StartGameMessage to GameMasterActor");
		gmActor.tell(new StartGameMessage(), ActorRef.noSender());

		while (true){
			Thread.sleep(1000);
		}
		
	}

}
