package games.shithead.main;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import games.shithead.game.actors.GameActor;
import games.shithead.game.actors.GameMasterActor;
import games.shithead.game.actors.ShitheadActorSystem;
import games.shithead.messages.StartGameMessage;
import games.shithead.utils.ConstantsProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class Main {

	public static void main(String[] args) throws InterruptedException, IOException {
		init();
	}

	private static void init() throws InterruptedException, IOException {
		ActorSystem actorSystem = ShitheadActorSystem.INSTANCE.getActorSystem();
		ActorRef gameMaterActor = actorSystem.actorOf(Props.create(GameMasterActor.class), ShitheadActorSystem.GAME_MASTER_ACTOR_NAME);
		actorSystem.actorOf(Props.create(GameActor.class), ShitheadActorSystem.GAME_ACTOR_NAME);

		/* This number is meaningless for the game, and has nothing to do with the player id (which is given later).
		 * Only used to ensure every Akka actor has a unique name. */
		int serialNumberAllocator = 1;
		for(String playerClassName : Files.lines(Paths.get(System.getenv(ConstantsProvider.SYSTEM_ENV_VAR_NAME) +
				"\\games.Shithead\\config\\players.txt")).collect(Collectors.toList())){
			initializePlayer(actorSystem, playerClassName, serialNumberAllocator);
			serialNumberAllocator++;
		}

		Thread.sleep(2000);

		gameMaterActor.tell(new StartGameMessage(), ActorRef.noSender());

		while (true){
			Thread.sleep(1000);
		}
	}

	private static void initializePlayer(ActorSystem actorSystem, String playerClassName, int serialNumber) {
		String name = playerClassName + serialNumber;
		try {
			actorSystem.actorOf(Props.create(
					Class.forName("games.shithead.players." + playerClassName)), name);
		}
		catch (Exception e) {
			System.out.println("Error initializing player " + playerClassName);
		}
	}

}
