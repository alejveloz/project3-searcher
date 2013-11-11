package edu.ucla.cs.cs144;

/** Class to represent users in memory while parsing **/
public class User {
	
	String userID;
	int rating;
	String location;
	String country;
	
	User()
	{
		userID = null;
		rating = -1;
		location = null;
		country = null;
	}
	
	public boolean isComplete()
	{
		return (userID != null) && (rating != -1) && (location != null) && (country != null);
	}
	
	public String loadString()
	{
		String loadString = "";
		
		
		loadString += "\"" + customSQLEscaped(userID) + "\"";
		loadString += ",";
		
		loadString += rating;
		loadString += ",";
		
		if(location != null)
			loadString += "\"" + customSQLEscaped(location) + "\"";
		else
			loadString += "\\N";
		loadString += ",";
		
		if(country != null)
			loadString += "\"" + customSQLEscaped(country) + "\"";
		else
			loadString += "\\N";
		
		
		return loadString;
	}
	
	public String customSQLEscaped(String string)
	{
		return string.replace(",", "\\,").replace("\"", "\\\"");
	}
}
