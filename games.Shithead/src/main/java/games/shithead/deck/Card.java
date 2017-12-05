package games.shithead.deck;

public class Card implements ICard {

	private int value;
	private int kind;
	private int uniqueId;
	private boolean isSpecialCard;
	
	public Card(int value, int kind, int uniqueId) {
		this.value = value;
		this.kind = kind;
		this.uniqueId = uniqueId;
		this.isSpecialCard = determineIfSpecial();
	}

	public Card(ICard cardToCopyFrom) {
		this.value = cardToCopyFrom.getValue();
		this.kind = cardToCopyFrom.getKind();
		this.uniqueId = cardToCopyFrom.getUniqueId();
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
	public int getUniqueId() {
		return uniqueId;
	}

	@Override
	public boolean isSpecialCard() {
		return isSpecialCard;
	}
}
