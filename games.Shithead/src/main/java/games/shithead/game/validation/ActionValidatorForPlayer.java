package games.shithead.game.validation;

import games.shithead.game.interfaces.IGameCard;
import games.shithead.game.interfaces.IPlayerState;

import java.util.List;

/**
 * This class is used by the players to validate potential actions
 */
public class ActionValidatorForPlayer extends ActionValidationBase {

    public static ActionValidationResult validateAction(IPlayerState playerState, List<IGameCard> cardsToPlay, List<IGameCard> pile){
        return validateAction(playerState, cardsToPlay, pile, ValidatorIdentity.PLAYER);
    }
}
