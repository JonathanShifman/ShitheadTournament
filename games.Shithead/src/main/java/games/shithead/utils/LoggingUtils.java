package games.shithead.utils;

import games.shithead.deck.CardDescriptionGenerator;
import games.shithead.game.interfaces.IGameCard;

import java.util.List;
import java.util.stream.Collectors;

public class LoggingUtils {

    /**
     * Converts a list of cards to a string made of their minimal descriptions (only ranks). Used for logging.
     * @param cards The list of cards to analyze
     * @return A String representing the given cards' descriptions
     */
    public static String cardsToMinDescriptions(List<IGameCard> cards) {
        String cardDescriptions = cards.stream()
                .map(card -> CardDescriptionGenerator.cardFaceToMinimalDescription(card.getCardFace().orElse(null)))
                .collect(Collectors.joining(", "));
        return "[" + cardDescriptions + "]";
    }

    public static String cardsToFullDescriptions(List<IGameCard> cards) {
        String cardDescriptions = cards.stream()
                .map(card -> CardDescriptionGenerator.cardToFullDescription(card.getCardFace().orElse(null), card.getUniqueId()))
                .collect(Collectors.joining(", "));
        return "[" + cardDescriptions + "]";
    }

}
