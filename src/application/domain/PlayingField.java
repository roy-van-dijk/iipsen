package application.domain;

import java.util.LinkedHashMap;
import java.util.List;

public interface PlayingField {
	public List<CardRowImpl> getCardRows();
	
	public List<Noble> getNobles();
	
	public LinkedHashMap<Gem, Integer> getTokenGemCount();
}
