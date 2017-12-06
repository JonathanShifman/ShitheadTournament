package games.shithead.messages;

public class RegisterPlayerMessage {

    public int playerId;
    public String playerName;

    public RegisterPlayerMessage(int playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
    }
}
