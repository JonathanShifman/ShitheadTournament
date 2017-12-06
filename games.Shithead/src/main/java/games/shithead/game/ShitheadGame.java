//package games.shithead.game;
//
//import games.shithead.moves.IMove;
//import games.shithead.players.IShitheadPlayer;
//import games.shithead.players.SimpleShitheadPlayer;
//
//public class ShitheadGame implements IShitheadGame {
//
//	private IShitheadPlayer[] players;
//	private int currentTurnPlayer;
//	private int activeValue;
//	private int playersFinished;
//
//	public ShitheadGame(int numberOfPlayers) {
//		players = new IShitheadPlayer[numberOfPlayers];
//		for(int i = 0; i < numberOfPlayers; i++) {
//			players[i] = new SimpleShitheadPlayer();
//		}
//
//		currentTurnPlayer = 0;
//		activeValue = 0;
//		playersFinished = 0;
//	}
//
//	@Override
//	public void run() {
//		dealCards();
//
//		while(playersFinished < players.length - 1) {
//			IMove attemptedMove = players[currentTurnPlayer].makeMove();
//			if(validMove(attemptedMove, currentTurnPlayer)) {
//				makeMove(attemptedMove, currentTurnPlayer);
//				notifyPlayers(attemptedMove, currentTurnPlayer);
//				checkIfPlayerWon(currentTurnPlayer);
//				advanceTurn();
//			}
//		}
//
//	}
//
//	private boolean validMove(IMove move, int playerId) {
//		return true;
//	}
//
//	private void makeMove(IMove move, int playerId) {
//
//	}
//
//	private void notifyPlayers(IMove attemptedMove, int currentTurnPlayer) {
//		// TODO Auto-generated method stub
//
//	}
//
//	private void checkIfPlayerWon(int currentTurnPlayer) {
//		// TODO Auto-generated method stub
//
//	}
//
//	private void advanceTurn() {
//		// TODO Auto-generated method stub
//
//	}
//
//	private void dealCards() {
//		// TODO Auto-generated method stub
//
//	}
//
//}
