package games.shithead.messages;

public class NotifyPlayersTurnMessage {
    public int playerToNotify;

    public NotifyPlayersTurnMessage(int currentPlayer) {
        this.playerToNotify = currentPlayer;
    }
}
