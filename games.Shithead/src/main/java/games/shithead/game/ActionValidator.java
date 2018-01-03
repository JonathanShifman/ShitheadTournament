package games.shithead.game;

import games.shithead.messages.PlayerActionInfo;

public class ActionValidator {

    public static boolean validateAction(PlayerActionInfo turnInfo, int currentPlayer){
        if(turnInfo.getPlayerId()!=currentPlayer && !turnInfo.isInterruption()){
            return false;
        }
        //add more validations here
        return true;
    }
}
