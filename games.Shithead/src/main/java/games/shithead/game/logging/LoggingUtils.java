package games.shithead.game.logging;

import games.shithead.deck.CardDescriptionGenerator;
import games.shithead.game.interfaces.IGameCard;

import java.util.List;
import java.util.stream.Collectors;

public class LoggingUtils {

    /**
     * Converts a list of cards to a string made of their values. Used for logging.
     * @param cards The list of cards to analyze
     * @return A String representing the sequence of the given cards' values
     */
    public static String cardsToDescriptions(List<IGameCard> cards) {
        String cardDescriptions = cards.stream()
                .map(card -> CardDescriptionGenerator.cardFaceToMinimalDescription(card.getCardFace().orElse(null)))
                .collect(Collectors.joining(", "));
        return "[" + cardDescriptions + "]";
    }

}
