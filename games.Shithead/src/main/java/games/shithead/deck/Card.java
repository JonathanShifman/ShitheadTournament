package games.shithead.deck;

public class Card implements ICard {

	private final int value;
	private final int kind;
	private final int uniqueId;
	
	public Card(int value, int kind, int uniqueId) {
		this.value = value;
		this.kind = kind;
		this.uniqueId = uniqueId;
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
	
}
