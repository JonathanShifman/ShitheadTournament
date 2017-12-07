package games.shithead.deck;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MultiDeck implements IMultiDeck {
	
	private static final int CARDS_PER_DECK = 54;
	private static final int MIN_VALUE = 2;
	private static final int MAX_VALUE = 15;
	private static final int MIN_KIND = 1;
	private static final int MAX_KIND = 4;
	private static final int NUMBER_OF_JOKERS = 2;
	
	private int numberOfDecks;
	private int currentCardFaceIndex;
	private ICardFace[] cardFaces;
	
	public MultiDeck(int numberOfDecks) {
		this.numberOfDecks = numberOfDecks;
		this.currentCardFaceIndex = 0;
		init();
		shuffle();
	}
	
	private void init() {
		cardFaces = new ICardFace[CARDS_PER_DECK * numberOfDecks];
		for(int i = 0; i < numberOfDecks; i++) {
			insertSingleDeck(cardFaces, CARDS_PER_DECK * i);
		}
	}
	
	private void insertSingleDeck(ICardFace[] cardFaces, int indexToStart) {
		int currentIndex = indexToStart;
		for(int value = MIN_VALUE; value <= MAX_VALUE; value++) {
			for(int kind = MIN_KIND; kind <= MAX_KIND; kind++) {
				if(value == MAX_VALUE && kind > NUMBER_OF_JOKERS) {
					break;
				}
				cardFaces[currentIndex] = new CardFace(value, kind);
				currentIndex++;
			}
		}
	}
	
	private void shuffle() {
		
	}

	@Override
	public ICardFace getNextCardFace() {
		if (currentCardFaceIndex >= cardFaces.length) {
			return null;
		}
		ICardFace cardFace = cardFaces[currentCardFaceIndex];
		currentCardFaceIndex++;
		return cardFace;
	}

	@Override
	public List<ICardFace> getNextCardFaces(int numberOfCards) {
		List<ICardFace> cardFaces = new LinkedList<>();
		for(int i = 0; i < numberOfCards; i++) {
			ICardFace cardFace = getNextCardFace();
			if (cardFace != null) {
				cardFaces.add(cardFace);
			}
		}
		return cardFaces;
	}
	
	@Override
	public int getNumberOfDecks() {
		return numberOfDecks;
	}
	
	@Override
	public int getNumberOfCards() {
		return numberOfDecks * CARDS_PER_DECK;
	}

	
}
