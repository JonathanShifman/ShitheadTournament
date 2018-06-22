package games.shithead.main;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import games.shithead.game.InitParams;
import games.shithead.game.actors.GameActor;
import games.shithead.game.actors.GameMasterActor;
import games.shithead.game.actors.ShitheadActorSystem;
import games.shithead.messages.InitParamsMessage;
import games.shithead.messages.StartGameMessage;
import games.shithead.constants.ConstantsProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Main {



	public static void main(String[] args) throws InterruptedException, IOException {
		init(generateInitParams(args));
	}

	private static void init(InitParams initParams) throws InterruptedException, IOException {
		ActorSystem actorSystem = ShitheadActorSystem.INSTANCE.getActorSystem();
		ActorRef gameMaterActor = actorSystem.actorOf(Props.create(GameMasterActor.class), ShitheadActorSystem.GAME_MASTER_ACTOR_NAME);
		ActorRef gameActor = actorSystem.actorOf(Props.create(GameActor.class), ShitheadActorSystem.GAME_ACTOR_NAME);

		gameActor.tell(new InitParamsMessage(initParams), ActorRef.noSender());
		Thread.sleep(500);

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

	private static InitParams generateInitParams(String[] args) throws IOException {
		if(args.length >= 2 && args[0].equals("-i")) {
			List<String> initFileLines = Files.readAllLines(Paths.get(args[1]));
			Iterator<String> linesIterator = initFileLines.iterator();

			Map<Integer, String> idsToNames = new HashMap<>();
			for(int i = 0; i < initFileLines.size() - 2; i++) {
				String entry = linesIterator.next();
				String[] splitEntry = entry.split(":");
				int id = Integer.parseInt(splitEntry[0]);
				String name = Arrays.asList(splitEntry).stream()
						.skip(1)
						.collect(Collectors.joining(":"));
				idsToNames.put(id, name);
			}

			List<String> deckCardDescriptions = Arrays.asList(linesIterator.next().split(","));

			List<Integer> playingQueue = Arrays.asList(linesIterator.next().split(",")).stream()
					.map(idString -> Integer.parseInt(idString))
					.collect(Collectors.toList());

			return new InitParams()
					.withIdsToNames(idsToNames)
					.withDeckCards(deckCardDescriptions)
					.withPlayingQueue(playingQueue);
		}
		return new InitParams();
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
