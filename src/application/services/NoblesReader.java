package application.services;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import application.domain.Gem;
import application.domain.Noble;
import application.domain.NobleImpl;
import application.util.Util;

/**
 * This reads the nobles from a csv file and converts it to objects
 * 
 * @author Tom
 * 
 */
public class NoblesReader {
	
	private final static int prestigeValue = 3;
	private final static int bonusStartingIndex = 1; // At which index does the list of bonusses start
	private final static String noblesFile = "resources/config/nobles.csv";
	private final static Gem[] gemList = new Gem[] { Gem.DIAMOND, Gem.SAPPHIRE, Gem.EMERALD, Gem.RUBY, Gem.ONYX }; // Follows the same order as in the noblesFile
	
	private List<Noble> allNobles;
	
	/**
	 * Instantiates a new nobles reader.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public NoblesReader() throws IOException {
		this.allNobles = new ArrayList<Noble>();
		this.generateNobles();
	}

	/**
	 * Generate nobles.
	 *
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	//generate noble array from csv file.
	private void generateNobles() throws FileNotFoundException, IOException {
		try (
				InputStream file = this.getClass().getClassLoader().getResourceAsStream(noblesFile);
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
	
	/**
	 * Gets the nobles.
	 *
	 * @return the nobles
	 */
	// get stack of nobles
	public Stack<Noble> getNobles() {
		Stack<Noble> nobleArray = new Stack<>();	
		for(Noble noble : allNobles) {
			nobleArray.add(noble);
		}
		return nobleArray;
	}

	
	/**
	 * Read noble.
	 *
	 * @param nobleRecord the noble record
	 * @return the noble
	 */
	// read data from noble (illustration and costs, prestige value is defined as constant.
	private Noble readNoble(String[] nobleRecord) throws RemoteException {
		String illustration = nobleRecord[0];
		Map<Gem, Integer> bonusCosts = readBonusCosts(nobleRecord);
		Noble noble = new NobleImpl(prestigeValue, illustration, bonusCosts);
		return noble;
	}
	
	/**
	 * Read bonus costs.
	 *
	 * @param record the record
	 * @return the map
	 */
	//generate a map(gem, integer) of the cost of the noble.
	private static Map<Gem, Integer> readBonusCosts(String[] record){
		Map<Gem, Integer> costs = new LinkedHashMap<>();
		
		for(int i = 0; i < gemList.length; i++)
		{
			int bonusCost = Util.StringToInt(record[bonusStartingIndex + i], 0);
			costs.put(gemList[i], bonusCost);
		}
		
		return costs;
	}

}
