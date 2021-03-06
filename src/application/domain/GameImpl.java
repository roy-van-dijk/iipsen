package application.domain;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import application.services.SaveGameDAO;
import application.util.Logger;
import application.util.Logger.Verbosity;

// TODO: Auto-generated Javadoc
/**
 * The Class GameImpl.
 *
 * @author Sanchez
 */
public class GameImpl extends UnicastRemoteObject implements Reinitializable, Game, Serializable {
	
	private static final long serialVersionUID = -2852281344739846301L;
	
	private GameState gameState;
	
	private transient Registry registry;
	
	private PlayingFieldImpl playingField;
	
	private int currentPlayerIdx;
	private int roundNr;
	private int maxPlayers;
	private boolean reserveCardInventoryFull;

	private List<Player> players; // Contains a list of PlayerImpl on server
	private Player winningPlayer;
	
	private transient Map<GameObserver, Player> observers;
	

	private EndTurnImpl endTurn;


	/**
	 * Instantiates a new game impl.
	 *
	 * @param maxPlayers the max players
	 * @throws RemoteException the remote exception
	 */
	public GameImpl(int maxPlayers) throws RemoteException {
		this.maxPlayers = maxPlayers;
		
		this.roundNr = 0;
		this.currentPlayerIdx = -1;
		
		this.players = new ArrayList<Player>();
		this.observers = new LinkedHashMap<GameObserver, Player>();
		
		//this.debugCreate4Players();
		
		this.playingField = new PlayingFieldImpl(this.maxPlayers);
		
		this.endTurn = new EndTurnImpl(this);
	}
	
	
	/* (non-Javadoc)
	 * @see application.domain.Game#nextTurn()
	 */
	public void nextTurn() throws RemoteException
	{
		System.out.println("[DEBUG] GameImpl::nextTurn()::Next turn started");
		currentPlayerIdx++;
		if(currentPlayerIdx >= players.size() || currentPlayerIdx < 0) {
			currentPlayerIdx = 0;
		}
		
		roundNr++;
		
		try {
			playingField.newTurn();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.playingField.getTempHand().updatePlayer(this.getCurrentPlayer());
		System.out.printf("[DEBUG] GameImpl::nextTurn()::Current player: %s(ID: %d)\n", this.getCurrentPlayer().getName(), currentPlayerIdx);
		this.notifyObservers();
	}

	/* (non-Javadoc)
	 * @see application.domain.Game#isDisabled(application.domain.GameObserver)
	 */
	public boolean isDisabled(GameObserver o) throws RemoteException
	{
		Player player = observers.get(o);
		return !(player.getName().equals(this.getCurrentPlayer().getName()));
	}
	
	/* (non-Javadoc)
	 * @see application.domain.Game#saveGame()
	 */
	@Override
	public void saveGame() throws RemoteException
	{
		try {
			SaveGameDAO.getInstance().saveGameToFile(this);
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see application.domain.Game#findSelectableCards(application.domain.MoveType)
	 */
	@Override
	public void findSelectableCards(MoveType moveType) throws RemoteException {
		this.playingField.getTempHand().setMoveType(moveType);
		
		if(moveType == MoveType.PURCHASE_CARD) {
			this.getCurrentPlayer().findSelectableCardsFromReserve();
		}
		playingField.findSelectableCardsFromField();
	}
	
	/* (non-Javadoc)
	 * @see application.domain.Game#reserveCardInventoryFull()
	 */
	//TODO not yet tested
	public boolean reserveCardInventoryFull() throws RemoteException
	{
		System.out.println("reserve card inventory full aantal: " + this.getCurrentPlayer().getReservedCards().size());
		if(this.getCurrentPlayer().getReservedCards().size() == 3)
		{
			reserveCardInventoryFull = true;
		}
		else 
		{
			reserveCardInventoryFull = false;
		}
		return reserveCardInventoryFull;
	}
	
	/* (non-Javadoc)
	 * @see application.domain.Game#setTokensSelectable(application.domain.MoveType)
	 */
	@Override
	public void setTokensSelectable(MoveType moveType) throws RemoteException {
		this.playingField.setTokensSelectable(moveType);
	}


	/* (non-Javadoc)
	 * @see application.domain.Game#getCurrentPlayerIdx()
	 */
	public int getCurrentPlayerIdx() throws RemoteException {
		return currentPlayerIdx;
	}

	/* (non-Javadoc)
	 * @see application.domain.Game#getRoundNr()
	 */
	public int getRoundNr() throws RemoteException {
		return roundNr;
	}

	/* (non-Javadoc)
	 * @see application.domain.Game#getPlayers()
	 */
	public List<Player> getPlayers() throws RemoteException {
		return players;
	}
	
	/**
	 * Sets the players.
	 *
	 * @param players the new players
	 */
	public void setPlayers(List<Player> players) {
		this.players = players;
	}

	/* (non-Javadoc)
	 * @see application.domain.Game#getPlayingField()
	 */
	public PlayingField getPlayingField() throws RemoteException {
		return playingField;
	}

	

	/* (non-Javadoc)
	 * @see application.domain.Game#getCurrentPlayer()
	 */
	public Player getCurrentPlayer() throws RemoteException {
		return players.get(currentPlayerIdx);
	}

	/**
	 * Gets the max players.
	 *
	 * @return the max players
	 * @throws RemoteException the remote exception
	 */
	public int getMaxPlayers() throws RemoteException {
		return maxPlayers;
	}

	/**
	 * Gets the game state.
	 *
	 * @return GameSate
	 * @throws RemoteException the remote exception
	 */
	public GameState getGameState() throws RemoteException {
		return gameState;
	}
	
	/**
	 * Notify observers.
	 *
	 * @throws RemoteException the remote exception
	 */
	public synchronized void notifyObservers() throws RemoteException
	{
		System.out.println("[DEBUG] GameImpl::notifyObservers()::Notifying all game observers of change");
		for(GameObserver o : observers.keySet())
		{
			try {
				o.modelChanged(this);
			} catch (RemoteException e) {
				Logger.log(String.format("GameImpl::notifyObservers()::Lost connection to observer: %s", o.toString()), Verbosity.DEBUG);
				this.observers.remove(o);
				this.terminateGame();
			}	
		}
	}

	/* (non-Javadoc)
	 * @see application.domain.Game#addObserver(application.domain.GameObserver)
	 */
	@Override
	public synchronized void addObserver(GameObserver o, Player player) throws RemoteException {
		this.observers.put(o, player);
		this.notifyObservers();
	}

	/* (non-Javadoc)
	 * @see application.domain.Game#removeObserver(application.domain.GameObserver)
	 */
	@Override
	public synchronized void removeObserver(GameObserver o) throws RemoteException {
		this.observers.remove(o);
		this.notifyObservers();
	}
	
	/* (non-Javadoc)
	 * @see application.domain.Game#updatePlayingFieldAndPlayerView()
	 */
	@Override
	public void cleanUpTurn() throws RemoteException {
		playingField.getTempHand().emptyHand();
	}

	/* (non-Javadoc)
	 * @see application.domain.Game#updatePlayingFieldAndPlayerView()
	 */
	public void updatePlayingFieldAndPlayerView() throws RemoteException {
		for(CardRow cardRow : playingField.getCardRows()) {
			cardRow.updateView();
		}
		this.getCurrentPlayer().updatePlayerView();
	}
	
	/* (non-Javadoc)
	 * @see application.domain.Game#cleanUpSelections()
	 */
	@Override
	public void cleanUpSelections() throws RemoteException {
		this.playingField.getTempHand().emptyHand();
		
		for(CardRow cardRow : playingField.getCardRows()) {
			cardRow.clearSelectableCards();
		}
		playingField.newTurn();
		this.getCurrentPlayer().clearSelectableCards();
		this.notifyObservers();
	}
	
	/* (non-Javadoc)
	 * @see application.domain.Game#addCardToTempFromReserve(application.domain.Card)
	 */
	@Override
	public void reserveCardFromDeck(int cardRowIdx) throws RemoteException {
		CardRow cardRow = this.playingField.getCardRows().get(cardRowIdx);
		Card card = cardRow.getCardDeck().top();
		this.addCardToTempHand(card, this.playingField.getTempHand());
		card.setReservedFromDeck(true);
		
		this.getPlayingField().setDeckSelected(cardRow);
		this.updatePlayingFieldAndPlayerView();	
		this.notifyObservers();
	}
	
	/* (non-Javadoc)
	 * @see application.domain.Game#addCardToTempFromField(int, int)
	 */
	@Override
	public void addCardToTempFromField(int cardRowIdx, int cardIdx) throws RemoteException {
		CardRow cardRow = this.playingField.getCardRows().get(cardRowIdx);
		Card card = cardRow.getCardSlots()[cardIdx];
		Logger.log("GameImpl::addCardToTempFromField::Card = " + card, Verbosity.DEBUG);
		TempHand tempHand = this.playingField.getTempHand();
		
		this.addCardToTempHand(card, tempHand);
		
		if(tempHand.getMoveType() == MoveType.RESERVE_CARD)
		{
			this.playingField.setDeckDeselected();
		}
		this.updatePlayingFieldAndPlayerView();
		this.notifyObservers();
	}
	
	/* (non-Javadoc)
	 * @see application.domain.Game#addCardToTempFromReserve(int)
	 */
	@Override
	public void addCardToTempFromReserve(int cardIdx) throws RemoteException {
		Card card = this.getCurrentPlayer().getReservedCards().get(cardIdx);
		TempHand tempHand = this.getPlayingField().getTempHand();
		
		this.addCardToTempHand(card, tempHand);
		
		this.updatePlayingFieldAndPlayerView();
		this.notifyObservers();
	}

	/* (non-Javadoc)
	 * @see application.domain.Game#getEndTurn()
	 */
	public EndTurn getEndTurn() throws RemoteException {
		return endTurn;
	}
	
	/**
	 * Adds the card to temp hand.
	 *
	 * @param card the card
	 * @param tempHand the temp hand
	 * @throws RemoteException the remote exception
	 */
	private void addCardToTempHand(Card card, TempHand tempHand) throws RemoteException {	
		MoveType moveType = tempHand.getMoveType();
		System.out.println("GameImpl::addCardToTemp()::Card = " + card);
		if(moveType == MoveType.PURCHASE_CARD) {
			tempHand.selectCardToBuy(card);
		} else if(moveType == MoveType.RESERVE_CARD) {
			tempHand.selectCardToReserve(card);
		}
	}

	/* (non-Javadoc)
	 * @see application.domain.Game#addTokenToTemp(application.domain.Gem)
	 */
	@Override
	public void addTokenToTemp(Gem gemType) throws RemoteException {
		this.playingField.addTokenToTemp(gemType);
		this.notifyObservers();
	}
	
	/* (non-Javadoc)
	 * @see application.domain.Game#removeTokenFromTemp(application.domain.Gem)
	 */
	@Override
	public void removeTokenFromTemp(Gem gemType) throws RemoteException {
		this.playingField.removeTokenFromTemp(gemType);
		this.notifyObservers();
	}
	
	/**
	 * close the game for everyone.
	 *
	 * @throws RemoteException the remote exception
	 */
	public synchronized void terminateGame() throws RemoteException
	{	
		this.gameState = GameState.CLOSING;
		this.disconnectAllPlayers();
		
		UnicastRemoteObject.unexportObject(this, true);
		try {
			this.registry.unbind("Lobby");
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		UnicastRemoteObject.unexportObject(this.registry, true);
		System.out.println("[DEBUG] GameImpl::terminateGame()::Server terminated.");
	}

	/**
	 * disconnect the observers from the game.
	 *
	 * @throws RemoteException the remote exception
	 */
	private void disconnectAllPlayers() throws RemoteException {
		System.out.println("[DEBUG] GameImpl::disconnectAllPlayers()::Disconnecting all observers.");
		for(GameObserver o : observers.keySet())
		{
			if(this.gameState == GameState.CLOSING) {
				o.disconnect(gameState);
			} else if(this.gameState == GameState.FINISHED) {
				o.showWinScreen(gameState, winningPlayer.getName());
			}
		}
	}
	
	/**
	 * game over, player has won.
	 *
	 * @param winningPlayer the winning player
	 * @throws RemoteException the remote exception
	 */
	@Override
	public void playerHasWon(Player winningPlayer) throws RemoteException {
		this.winningPlayer = winningPlayer;
		this.gameState = GameState.FINISHED;
		this.disconnectAllPlayers();
		
		UnicastRemoteObject.unexportObject(this, true);
		try {
			this.registry.unbind("game");
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		UnicastRemoteObject.unexportObject(this.registry, true);
		System.out.println("[DEBUG] GameImpl::terminateGame()::Server terminated.");
	}

	/* (non-Javadoc)
	 * @see application.domain.Game#getWinningPlayer()
	 */
	@Override
	public Player getWinningPlayer() {
		return winningPlayer;
	}

	/**
	 * Sets the registry.
	 *
	 * @param registry the new registry
	 */
	public void setRegistry(Registry registry) {
		this.registry = registry;
	}
	
	/* (non-Javadoc)
	 * @see application.domain.Game#anyCardsPurchasable()
	/**
	 * check if the current player can afford any cards from playingfield or the reserved cards from the player
	 * if the player can buy a card, return true, else return false.
	 */
	public boolean anyCardsPurchasable() throws RemoteException {
		Player player = this.getCurrentPlayer();
		List<CardRow> playingFieldCardRows = getPlayingField().getCardRows();

		for(CardRow cardRow : playingFieldCardRows)
		{
			for(Card card : cardRow.getCardSlots()) 
			{
				if(player.canAffordCard(card.getCosts()))
				{
					return true;
				}
			}
		}
		
		for(Card card : player.getReservedCards())
		{
			if(player.canAffordCard(card.getCosts()))
			{
				return true;
			}
		}
		return false;
	}


	/* (non-Javadoc)
	 * @see application.domain.Reinitializable#reinitializeObservers()
	 */
	@Override
	public void reinitializeObservers() {
		this.observers = new LinkedHashMap<GameObserver, Player>();
		this.playingField.reinitializeObservers();
		
		for(int i = 0; i < players.size(); i++)
		{
			PlayerImpl player = (PlayerImpl) players.get(i);
			player.reinitializeObservers();
		}
	}
}
