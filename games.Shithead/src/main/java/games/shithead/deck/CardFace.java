package games.shithead.deck;

public class CardFace implements ICardFace {

	private final int value;
	private final int kind;
	private boolean isSpecialCard;
	
	public CardFace(int value, int kind) {
		this.value = value;
		this.kind = kind;
		this.isSpecialCard = determineIfSpecial();
	}

	public CardFace(ICardFace cardToCopyFrom) {
		this.value = cardToCopyFrom.getValue();
		this.kind = cardToCopyFrom.getKind();
		this.isSpecialCard = cardToCopyFrom.isSpecialCard();
	}

	private boolean determineIfSpecial() {
		//FIXME: check if special card
		return false;
	}

	@Override
	public int getValue() {
		return value;
	}

	@Override
	public int getKind() {
		return kind;
	}

	@Override
	public boolean isSpecialCard() {
		return isSpecialCard;
	}
}
