package application.services;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import application.domain.Card;
import application.domain.CardImpl;
import application.domain.Gem;
import application.domain.Noble;
import application.domain.NobleImpl;
import application.util.Util;

/**
 * This reads the cards from a csv file and converts it to objects
 * 
 * @author Tom
 *
 */
public class NobleReader {
	
	private final static int prestigeValue = 3;
	private final static int bonusStartingIndex = 1; // At which index does the list of bonusses start
	private final static String nobleFile = "resources/config/nobles.csv";
	private final static Gem[] gemList = new Gem[] { Gem.DIAMOND, Gem.SAPPHIRE, Gem.EMERALD, Gem.RUBY, Gem.ONYX }; // Follows the same order as in the cardsFile
	
	private List<Noble> allNobles;
	
	public NobleReader() throws IOException {
		this.allNobles = new ArrayList<Noble>();
		this.generateNobles();
	}

	//generate noble array from csv file.
	private void generateNobles() throws FileNotFoundException, IOException {
		try (
				FileInputStream file = new FileInputStream(nobleFile);
				InputStreamReader reader = new InputStreamReader(file);
				CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(2).build();
			){
			String[] nobleRecord;
			while ((nobleRecord = csvReader.readNext()) != null) {
				Noble noble = readNoble(nobleRecord);
				allNobles.add(noble);
				
			}
		}
	}
	
	// get specific noble from stack.
	public Stack<Noble> getNoble(){
		Stack<Noble> nobleArray = new Stack<>();	
		for(Noble noble : allNobles) {
			nobleArray.add(noble);
		}
		return nobleArray;
	}

	
	// read data from noble (illustration and costs, prestege value is always 3.
	private Noble readNoble(String[] nobleRecord) {
		String illustration = nobleRecord[0];
		Map<Gem, Integer> bonusCosts = readBonusCosts(nobleRecord);
		NobleImpl noble = new NobleImpl(prestigeValue, illustration, bonusCosts);
		
		return null;
	}
	
	//generate a map(gem, integer) of the cost of the noble.
	private static Map<Gem, Integer> readBonusCosts(String[] record){
		Map<Gem, Integer> costs = new HashMap<>();
		
		int startingIndex = bonusStartingIndex + gemList.length;
		
		for(int i = 0; i < gemList.length; i++)
		{
			int bonusCost = Util.StringToInt(record[startingIndex + i], 0);
			costs.put(gemList[i], bonusCost);
		}
		
		return costs;
	}

}