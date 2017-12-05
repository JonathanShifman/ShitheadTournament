package games.shithead.deck;

import java.util.ArrayList;
import java.util.List;

public class MultiDeck implements IMultiDeck {
	
	private static final int CARDS_PER_DECK = 54;
	private static final int MIN_VALUE = 2;
	private static final int MAX_VALUE = 15;
	private static final int MIN_KIND = 1;
	private static final int MAX_KIND = 4;
	private static final int NUMBER_OF_JOKERS = 2;
	
	private int numberOfDecks;
	private int currentCard;
	private ICard[] cards;
	
	public MultiDeck(int numberOfDecks) {
		this.numberOfDecks = numberOfDecks;
		this.currentCard = 0;
		init();
		shuffle();
	}
	
	private void init() {
		cards = new ICard[CARDS_PER_DECK * numberOfDecks];
		for(int i = 0; i < numberOfDecks; i++) {
			insertSingleDeck(cards, CARDS_PER_DECK * i);
		}
	}
	
	private void insertSingleDeck(ICard[] cards, int indexToStart) {
		int currentIndex = indexToStart;
		for(int value = MIN_VALUE; value <= MAX_VALUE; value++) {
			for(int kind = MIN_KIND; kind <= MAX_KIND; kind++) {
				if(value == MAX_VALUE && kind > NUMBER_OF_JOKERS) {
					break;
				}
				cards[currentIndex] = new Card(value, kind, currentIndex);
				currentIndex++;
			}
		}
	}
	
	private void shuffle() {
		
	}

	@Override
	public ICard getNextCard() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ICard> getNextCards(int numberOfCards) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
