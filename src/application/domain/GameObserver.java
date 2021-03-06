package application.domain;

import java.rmi.Remote;
import java.rmi.RemoteException;

// TODO: Auto-generated Javadoc
/**
 * An asynchronous update interface for receiving notifications
 * about Game information as the Game is constructed.
 */
public interface GameObserver extends Remote {
	
	/**
	 * This method is called when information about an Game
	 * which was previously requested using an asynchronous
	 * interface becomes available.
	 *
	 * @param game
	 * @throws RemoteException
	 */
	public void modelChanged(Game game) throws RemoteException;

	/**
	 * game is closing.
	 *
	 * @param gameState
	 * @throws RemoteException
	 */
	public void disconnect(GameState gameState) throws RemoteException;

	/**
	 * This method is called when information about an Game
	 * which was previously requested using an asynchronous
	 * interface becomes available.
	 *
	 * @param gameState
	 * @param winningPlayer
	 * @throws RemoteException
	 */
	public void showWinScreen(GameState gameState, String winningPlayer) throws RemoteException;

}
