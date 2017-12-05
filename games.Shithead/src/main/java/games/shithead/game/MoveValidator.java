package games.shithead.game;

public class MoveValidator {

    public static boolean validateMove(PlayerTurnInfo turnInfo, int currentPlayer){
        if(turnInfo.getPlayerId()!=currentPlayer && !turnInfo.isInterruption()){
            return false;
        }
        //add more validations here
        return true;
    }
}
