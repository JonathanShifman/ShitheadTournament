package games.shithead.game.validation;

/**
 * An enum representing all the possible results of an attempted player action.
 */
public enum ActionValidationResult {
    /**
     * The action is valid, and the played value is accepted based on the pile contents.
     * Play should proceed as usual.
     */
    PROCEED,

    /**
     * The action is valid, but the played value is not accepted based on the pile contents.
     * The player that made the action should take the pile.
     */
    TAKE,

    /**
     * The action is valid, but whether or not it will be accepted is unknown at the time of
     * attempting the action. This result can only be returned when a player is validating an
     * action involving cards with classified card faces.
     */
    UNKNOWN,

    /**
     * The attempted action is invalid.
     */
    FOUL,
}
