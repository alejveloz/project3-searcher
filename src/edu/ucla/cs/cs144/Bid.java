package edu.ucla.cs.cs144;

import java.text.SimpleDateFormat;
import java.util.Date;

/** Class to represent bids in memory while parsing **/
public class Bid {

	int itemID;
	Date time;
	String userID;
	String amount;

	SimpleDateFormat sqlFormat; // For formating into SQL friendly dates
	
	Bid()
	{
		itemID = -1;
		time = null;
		userID = null;
		amount = null;

		sqlFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}
	
	public String loadString()
	{
		String loadString = "";
		
		
		loadString += itemID;
		loadString += ",";
		
		loadString += "\"" + createSQLDate(time) + "\"";
		loadString += ",";
		
		loadString += "\"" + customSQLEscaped(userID) + "\"";
		loadString += ",";
		
		loadString += amount;
		
		
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
