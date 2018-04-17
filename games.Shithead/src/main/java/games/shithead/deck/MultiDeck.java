package games.shithead.deck;

import java.util.*;

public class MultiDeck implements IMultiDeck {

	private static final int MIN_REGULAR_VALUE = 2;
	private static final int MAX_REGULAR_VALUE = 14;
	private static final int NUMBER_OF_KINDS = 4;
	private static final int JOKER_VALUE = 15;
	private static final int NUMBER_OF_JOKERS = 2;
	private static final int CARDS_PER_DECK = (MAX_REGULAR_VALUE - MIN_REGULAR_VALUE + 1) * NUMBER_OF_KINDS +
			NUMBER_OF_JOKERS;
	
	private int numberOfDecks;
	private int currentCardFaceIndex;
	private List<ICardFace> cardFaces;

	/**
	 * Initializes a new shuffled multi-deck, consisting of a given number of
	 * regular 54 card decks.
	 * @param numberOfDecks The number of card decks to include in the multi-deck
	 */
	public MultiDeck(int numberOfDecks) {
		this.numberOfDecks = numberOfDecks;
		this.currentCardFaceIndex = 0;
		init();
		shuffle();
	}
	
	private void init() {
		cardFaces = new ArrayList<>(CARDS_PER_DECK * numberOfDecks);
		for(int deckIndex = 0; deckIndex < numberOfDecks; deckIndex++) {
			insertSingleDeck(deckIndex);
		}
	}
	
	private void insertSingleDeck(int deckIndex) {
		int currentIndex = deckIndex * CARDS_PER_DECK;

		// Insert all non joker cards
		for(int currentValue = MIN_REGULAR_VALUE; currentValue <= MAX_REGULAR_VALUE; currentValue++) {
			for(int currentKind = 1; currentKind <= NUMBER_OF_KINDS; currentKind++) {
				cardFaces.add(currentIndex, new CardFace(currentValue, currentKind));
				currentIndex++;
			}
		}

		// Insert jokers
		for(int currentKind = 1; currentKind <= NUMBER_OF_JOKERS; currentKind++) {
			cardFaces.add(currentIndex, new CardFace(JOKER_VALUE, currentKind));
			currentIndex++;
		}
	}
	
	private void shuffle() {
		Collections.shuffle(cardFaces);
	}

	@Override
	public ICardFace getNextCardFace() {
		if (currentCardFaceIndex >= cardFaces.size()) {
			return null;
		}
		return cardFaces.get(currentCardFaceIndex++);
	}

	@Override
	public List<ICardFace> getNextCardFaces(int numberOfCards) {
		List<ICardFace> cardFaces = new LinkedList<>();
		for(int i = 0; i < numberOfCards; i++) {
			ICardFace cardFace = getNextCardFace();
			if (cardFace != null) {
				cardFaces.add(cardFace);
			}
			else {
				break;
			}
		}
		return cardFaces;
	}
	
	@Override
	public int getNumberOfDecks() {
		return numberOfDecks;
	}
	
	@Override
	public int getNumberOfInitialCards() {
		return numberOfDecks * CARDS_PER_DECK;
	}

	@Override
	public boolean isEmpty() {
		return currentCardFaceIndex >= cardFaces.size();
	}


}
