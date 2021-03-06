package application.domain;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Row of cards that can be seen on the playing field.
 *
 * @author Sanchez
 */
public interface CardRow extends Remote {

	/**
	 * Removes the passed card from the card row.
	 *
	 * @param card
	 * @throws RemoteException
	 */
	public void removeCard(Card card) throws RemoteException;

	/**
	 * Adds an observer to the card row that other classes can listen to.
	 *
	 * @param observer the observer
	 * @throws RemoteException
	 */
	public void addObserver(CardRowObserver observer) throws RemoteException;

	/**
	 * Gets the current cards in this card row.
	 *
	 * @return Card[]
	 * @throws RemoteException
	 */
	public Card[] getCardSlots() throws RemoteException;

	/**
	 * Gets the card deck this card row draws cards from.
	 *
	 * @return CardDeck
	 * @throws RemoteException
	 */
	public CardDeck getCardDeck() throws RemoteException;

	/**
	 * Gets all the cards in this card row that the current player can afford.
	 *
	 * @return List<Card>
	 * @throws RemoteException
	 */
	public List<Card> getSelectableCards() throws RemoteException;

	/**
	 * Notifies all observers of this card row of any changes.
	 *
	 * @throws RemoteException the remote exception
	 */
	public void updateView() throws RemoteException;

	/**
	 * Removes selectable (affordable) markings from all cards in the row.
	 *
	 * @throws RemoteException
	 */
	public void clearSelectableCards() throws RemoteException;

	/**
	 * Finds all selectable (affordable) cards in this row.
	 *
	 * @param moveType
	 * @param player
	 * @throws RemoteException
	 */
	public void findSelectableCards(MoveType moveType, Player player) throws RemoteException;
	
	/**
	 * Gets the index.
	 *
	 * @return the index
	 * @throws RemoteException the remote exception
	 */
	public int getIndex() throws RemoteException;

}
