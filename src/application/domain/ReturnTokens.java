package application.domain;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import application.views.ReturnTokensView;

public class ReturnTokens {
	private TokenList tokenListNew;
	private List<Token> removedTokens;
	
	private Player player;
	private PlayingField playingField;
	
	private ReturnTokensView view;
	
	private boolean allowConfirm;

	public ReturnTokens(PlayingField playingField, Player player)
	{
		this.player = player;
		this.playingField = playingField;
		
		this.allowConfirm = false;
		this.tokenListNew = player.getTokenList();
		
		this.removedTokens = new ArrayList<>();
	}
	
	private Token getTokenFromGemType(List<Token> tokenArray, Gem gemType)
	{
		for(Token token : tokenArray)
		{
			if(token.getGemType() == gemType) return token;
		}
		return null;
	}

	public void removeToken(Gem gemType) {
		if(tokenListNew.getAll().size() > 10 && tokenListNew.getTokenGemCount().get(gemType) > 0) {
			Token token = this.getTokenFromGemType(tokenListNew.getAll(), gemType);
			tokenListNew.remove(token);
			removedTokens.add(token);
		}
		validateNewTokens();
	}

	public void addToken(Gem gemType)  {
		Token token = this.getTokenFromGemType(removedTokens, gemType);
		if(token != null) {
			tokenListNew.add(token);
			removedTokens.remove(token);
		}
		validateNewTokens();
	}
	
	public void notifyView() {
		view.modelChanged(this);
	}
	
	public void validateNewTokens()
	{
		if(tokenListNew.getAll().size() == 10) {
			this.allowConfirm = true;
		} else {
			this.allowConfirm = false;
		}
		notifyView();
	}

	public void confirmButton() throws RemoteException {
		if(allowConfirm)
		{
			for(Token token : removedTokens)
			{
				player.removeToken(token); // TODO: only problem: updates UI 3x quickly
				playingField.addToken(token);
			}
		}
	}

	public TokenList getTokenListNew() {
		return tokenListNew;
	}


	public void registrate(ReturnTokensView view) {
		this.view = view;
		this.view.modelChanged(this);
	}


	public boolean isAllowConfirm() {
		return allowConfirm;
	}
	
}
