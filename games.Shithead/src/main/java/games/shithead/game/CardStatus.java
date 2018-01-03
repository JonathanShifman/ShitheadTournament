package games.shithead.game;

public class CardStatus {

	public static final CardStatus DECK = new CardStatus(-1);
	public static final CardStatus PILE = new CardStatus(-2);
	public static final CardStatus BURNT = new CardStatus(-3);

	private final int holderId;
	
	public CardStatus(int holderId) {
		this.holderId = holderId;
	}
	
	public boolean isHeldByAPlayer() {
		return holderId > 0;
	}
	
	public int getHolderId() {
		return holderId;
	}
	
}
