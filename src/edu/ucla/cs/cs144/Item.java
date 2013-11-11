package edu.ucla.cs.cs144;

import java.text.SimpleDateFormat;
import java.util.Date;

/** Class to represent items in memory while parsing **/
public class Item {
	
	int itemID;
	String name;
	String buyPrice; // Consider converting to different representation 
	String firstMinimumBid; // Consider converting to different representation 
	Date started;
	Date ends;
	String description;
	int numBids;
	String currentBidAmount; // Consider converting to different representation 
	String userID;  //for database purposes

	SimpleDateFormat sqlFormat; // For formating into SQL friendly dates
	
	Item()
	{
		itemID = -1;
		name = null;
		buyPrice = null;
		firstMinimumBid = null;
		started = null;
		ends = null;
		description = null;
		numBids = 0;
		currentBidAmount = null;
		userID = null;
		
		sqlFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}
	
	public String loadString()
	{
		String loadString = "";
		
		
		loadString += itemID;
		loadString += ",";
		
		loadString += "\"" + customSQLEscaped(name) + "\"";
		loadString += ",";
		
		if(buyPrice != null)
			loadString += buyPrice;
		else
			loadString += "\\N";
		loadString += ",";

		loadString += firstMinimumBid;
		loadString += ",";
		
		loadString += "\"" + createSQLDate(started) + "\"";
		loadString += ",";
		
		loadString += "\"" + createSQLDate(ends) + "\"";
		loadString += ",";
		
		loadString += "\"" + customSQLEscaped(description) + "\"";
		loadString += ","; 
		
		loadString += numBids;
		loadString += ",";
		
		loadString += currentBidAmount;
		loadString += ",";
		
		
		return loadString;
	}
	
	public String createSQLDate(Date javaDate)
	{
		return sqlFormat.format(javaDate);
	}
	
	public String customSQLEscaped(String string)
	{
		return string.replace(",", "\\,").replace("\"", "\\\"");
	}
}
