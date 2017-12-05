package games.shithead.main;

import games.shithead.game.IShitheadGame;
import games.shithead.game.ShitheadGame;

public class Main {

	public static void main(String[] args) {
		runSeriesOfGames(1);
	}

	private static void runSeriesOfGames(int numberOfGames) {
		for(int i = 0; i < numberOfGames; i++) {
			IShitheadGame game = new ShitheadGame(4);
			game.run();
		}
	}

}
