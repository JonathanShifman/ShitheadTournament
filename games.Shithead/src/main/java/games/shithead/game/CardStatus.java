package games.shithead.game;

public class CardStatus {

	public static final CardStatus DECK = new CardStatus(-1);
	public static final CardStatus PILE = new CardStatus(-2);
	public static final CardStatus BURNT = new CardStatus(-3);

	private int holderId;
	private HeldCardPosition heldCardPosition;
	
	public CardStatus(int holderId, HeldCardPosition heldCardPosition) {
		this.holderId = holderId;
		this.heldCardPosition = heldCardPosition;
	}
	
	private CardStatus(int holderId) {
		this.holderId = holderId;
		this.heldCardPosition = null;
	}
	
	public boolean isHeldByAPlayer() {
		return holderId > 0;
	}
	
	public int getHolderId() {
		return holderId;
	}
	
	public HeldCardPosition getHeldCardPosition() {
		return heldCardPosition;
	}

	public void setHeldCardPosition(HeldCardPosition heldCardPosition) {
		this.heldCardPosition = heldCardPosition;
	}

	public void setHolderId(int holderId) {
		this.holderId = holderId;
	}
}
