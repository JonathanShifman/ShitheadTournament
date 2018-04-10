package games.shithead.game;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class InfoProvider {

    private static GameState gameState;

    protected static void setGameState(GameState gameState) {
        InfoProvider.gameState = gameState;
    }

    public static int getNumberOfPlayers() {
        return gameState.getNumberOfPlayers();
    }

    public static int getCurrentPlayerTurn() {
        return gameState.getCurrentPlayerTurn();
    }

    public static List<IGameCard> getPile() {
        return new LinkedList<>(gameState.getPile());
    }

    public static Map<Integer,IPlayerHand> getPlayerInfos() {
        Map<Integer, IPlayerHand> players = gameState.getPlayers();
        Map<Integer, IPlayerHand> playersCopy = new HashMap<>();
        players.forEach((id, playerInfo) -> {
            playersCopy.put(id, copyPlayerInfo(playerInfo));
        });
        return playersCopy;
    }

    private static IPlayerHand copyPlayerInfo(IPlayerHand playerInfo) {
        IPlayerHand playerInfoCopy = new PlayerHand();
        copyList(playerInfo.getHandCards(), playerInfoCopy.getHandCards());
        copyList(playerInfo.getRevealedTableCards(), playerInfoCopy.getRevealedTableCards());
        copyList(playerInfo.getHiddenTableCards(), playerInfoCopy.getHiddenTableCards());
        copyList(playerInfo.getPendingSelectionCards(), playerInfoCopy.getPendingSelectionCards());
        return playerInfoCopy;
    }

    private static void copyList(List<IGameCard> sourceList, List<IGameCard> destinationList) {
        destinationList.addAll(sourceList);
    }

    public static int getNextMoveId() {
        return gameState.getCurrentMoveId();
    }
}
