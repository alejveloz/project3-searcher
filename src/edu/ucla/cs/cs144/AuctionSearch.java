package edu.ucla.cs.cs144;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.text.SimpleDateFormat;

import edu.ucla.cs.cs144.DbManager;
import edu.ucla.cs.cs144.SearchConstraint;
import edu.ucla.cs.cs144.SearchResult;

public class AuctionSearch implements IAuctionSearch {

	LuceneSearchEngine luceneSearchEngine;

	SimpleDateFormat inputFormat;
	SimpleDateFormat sqlFormat;
	
	/* 
         * You will probably have to use JDBC to access MySQL data
         * Lucene IndexSearcher class to lookup Lucene index.
         * Read the corresponding tutorial to learn about how to use these.
         *
         * Your code will need to reference the directory which contains your
	 * Lucene index files.  Make sure to read the environment variable 
         * $LUCENE_INDEX with System.getenv() to build the appropriate path.
	 *
	 * You may create helper functions or classes to simplify writing these
	 * methods. Make sure that your helper functions are not public,
         * so that they are not exposed to outside of this class.
         *
         * Any new classes that you create should be part of
         * edu.ucla.cs.cs144 package and their source files should be
         * placed at src/edu/ucla/cs/cs144.
         *
         */
	 
	public AuctionSearch()
	{
		try {
			luceneSearchEngine = new LuceneSearchEngine();
		} catch (IOException e) {
			System.out.println("IOException creating Lucene Search Engine");
			e.printStackTrace();
		}
		
		inputFormat = new SimpleDateFormat("MMM-dd-yy HH:mm:ss");
		sqlFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}
	
	public SearchResult[] basicSearch(String query, int numResultsToSkip, int numResultsToReturn) 
	{
		
		try {
			// Assume input "query" is already in space separated keyword form
			// We simply perform a search on the string 
			Hits hits = luceneSearchEngine.performBasicSearch(query);
			
			List<SearchResult> results = new ArrayList<SearchResult>();
			
			for(int i = numResultsToSkip; i < hits.length(); i++)
			{
				// Grab the corresponding document
				Document doc = hits.doc(i);
				
				// Create and add a result to our array list
				SearchResult result = new SearchResult(doc.get("id"), doc.get("name"));
				results.add(result);
				
				// Break if we've fulfilled the desired numResultsToReturn
				// Note that this handles the requirement:
				//  "In case numResultsToReturn is 0, return ALL matching items starting from numResultsToSkip"
				//  because this comparison will never be made until results.size() equals at least 1
				if(results.size() == numResultsToReturn)
					break;
			}
			
			return results.toArray(new SearchResult[results.size()]);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		// If we failed due to exceptions, return empty results
		return new SearchResult[0];
	}

	public SearchResult[] advancedSearch(SearchConstraint[] constraints, int numResultsToSkip, int numResultsToReturn) 
	{
		// Strategy:
		// Iterate through constraints and build Lucene and MySQL queries in parallel
		// Execute both queries and keep the intersection of the results (AND)
		// Return the results based on return offset/num constraints
		
		List<String> luceneConstraints = new ArrayList<String>();		
		List<String> bidderConstraints = new ArrayList<String>();
		String sellerConstraint = null;
		String buyPriceConstraint = null;
		String endTimeConstraint = null;
		
		for(int i = 0; i < constraints.length; i++)
		{
			SearchConstraint constraint = constraints[i];
			
			// Lucene constraints
			if(constraint.getFieldName().equals(FieldName.ItemName))
			{
				luceneConstraints.add("name:\"" + constraint.getValue() + "\"");
			}
			else if(constraint.getFieldName().equals(FieldName.Category))
			{
				luceneConstraints.add("categories:\"" + constraint.getValue() + "\"");
			}
			else if(constraint.getFieldName().equals(FieldName.Description))
			{
				luceneConstraints.add("description:\"" + constraint.getValue() + "\"");
			}
			// MySQL Constraints
			else if(constraint.getFieldName().equals(FieldName.SellerId))
			{
				// If there is more than one seller constraint, return no results
				if(sellerConstraint != null)
					return new SearchResult[0];
				sellerConstraint = constraint.getValue();
			}
			else if(constraint.getFieldName().equals(FieldName.BuyPrice))
			{
				// If there is more than one buy price constraint, return no results
				if(buyPriceConstraint != null)
					return new SearchResult[0];
				buyPriceConstraint = constraint.getValue();
			}
			else if(constraint.getFieldName().equals(FieldName.EndTime))
			{
				// If there is more than one end time constraint, return no results
				if(endTimeConstraint != null)
					return new SearchResult[0];
				
				String dateString = constraint.getValue();
				Date parsed = new Date();
		    	
		        try {
					parsed = inputFormat.parse(dateString);
				} catch (java.text.ParseException e) {
		            System.out.println("ERROR: Cannot parse \"" + dateString + "\"");
		    		return new SearchResult[0];
				}
		            
		    	endTimeConstraint = sqlFormat.format(parsed);
			}
			else if(constraint.getFieldName().equals(FieldName.BidderId))
			{
				bidderConstraints.add(constraint.getValue());
			}
		}
		
		// Keep note of the existence of constraint types
		boolean luceneConstraintsExist;
		boolean sqlConstraintsExist;
		
		if(luceneConstraints.size() > 0)
			luceneConstraintsExist = true;
		else
			luceneConstraintsExist = false;
		
		if(bidderConstraints.size() > 0 || sellerConstraint != null || buyPriceConstraint != null || endTimeConstraint != null)
			sqlConstraintsExist = true;
		else
			sqlConstraintsExist = false;
		
		// Build a lucene query
		// 	name:"Mariott" AND description:"Comfortable" ...
		String luceneQuery = null;
		for(int i = 0; i < luceneConstraints.size(); i++)
		{
			if(i == 0)
				luceneQuery = luceneConstraints.get(i);
			else
				luceneQuery += " AND " + luceneConstraints.get(i);
		}

		
		// Build List<SearchResults> luceneResults if we've built a Lucene query
		List<SearchResult> luceneResults = null;
		if(luceneQuery != null)
		{
			luceneResults = new ArrayList<SearchResult>();
			
			try {
				Hits hits = luceneSearchEngine.performAdvancedSearch(luceneQuery);
				
				for(int i = 0; i < hits.length(); i++)
				{
					// Grab the corresponding document
					Document doc = hits.doc(i);
					
					// Create and add a result to our array list
					SearchResult result = new SearchResult(doc.get("id"), doc.get("name"));
					luceneResults.add(result);
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
		// List<SearchResult> luceneResults now has all results of lucene query
		
		// Next we handle MySQL results
		
		// Create seller ID query
		// This gives us all items sold by the specified seller
		// Result1(id, name, ends, buy_price)
		String sellerQuery = null;
		if(sellerConstraint != null)
		{
			sellerQuery = "SELECT id, name, ends, buy_price FROM Item, ItemSeller WHERE Item.id = ItemSeller.iid";
			sellerQuery += " AND ItemSeller.uid LIKE \"" + sellerConstraint + "\"";
		}
		
		// Create the SQL query
		String mainQuery = "";
		
		// If there are no constraints on buy price, end time, or seller query, get all items
		if(buyPriceConstraint == null && endTimeConstraint == null && sellerQuery == null)
		{
			mainQuery = "SELECT id, name FROM Item";
		}
		// If there are constraints on buy price and/or end time constraint, refine query
		else if(buyPriceConstraint != null || endTimeConstraint != null)
		{
			mainQuery = "Select id, name FROM ";
			
			// The table we select from depends on whether there is a seller constraint
			if(sellerConstraint != null)
			{
				mainQuery += "(" + sellerQuery + ")";
			}
			else
			{
				mainQuery += "Item";
			}
			
			// Now we consider buy price and/or end time constraints
			String buyPriceMySQLCondition = null;
			if(buyPriceConstraint != null)
				buyPriceMySQLCondition = "buy_price = " + buyPriceConstraint;
			
			String endTimeMySQLCondition = null;
			if(endTimeConstraint != null)
				endTimeMySQLCondition = "ends = '" + endTimeConstraint + "'";
		
			// Build the remainder of the query
			mainQuery += " WHERE ";
			
			// Three possibilities to append the conditions
			if(buyPriceMySQLCondition != null && endTimeMySQLCondition == null)
			{
				mainQuery += buyPriceMySQLCondition;
			}
			else if(buyPriceMySQLCondition == null && endTimeMySQLCondition != null)
			{
				mainQuery += endTimeMySQLCondition;
			}
			else
			{
				mainQuery += buyPriceMySQLCondition + " AND " + endTimeMySQLCondition;
			}
		}
		// Then the only remaining constraint is a seller constraint
		else
		{
			mainQuery = sellerQuery;
		}
		
		

		// Build List<SearchResults> mySQLResults based on main query and bidder analysis
		List<SearchResult> mySQLResults = new ArrayList<SearchResult>();
		
		// Create connection and statement variables
		Connection conn = null;
		Statement stmt = null;

        // Execute the query
    	try {
    		conn = DbManager.getConnection(true);
			stmt = conn.createStatement();
    	
			// Execute the query
	    	ResultSet rs = stmt.executeQuery(mainQuery);

	    	// Process each result
	    	while (rs.next()) 
	    	{
	    		int id = rs.getInt("id");
	    		String name = rs.getString("name");
	    		
	    		// If there exist bidder constraints, analyze them. Otherwise add the result
	    		if(bidderConstraints.size() > 0)
	    		{
	    			// Get all the bidders on the item
		    		Statement subStmt = conn.createStatement();
					ResultSet biddersRs = subStmt.executeQuery("SELECT uid FROM Bid WHERE iid =" + id);
					
					
		    		// Create an array list of all the bidders
					ArrayList<String> bidders = new ArrayList<String>();
					while(biddersRs.next())
					{
						bidders.add(biddersRs.getString("uid"));
					}
					
		    		// For each Bidder constraint, verify it is contained in the array list
					boolean constraintFailed = false;
					for(int i = 0; i < bidderConstraints.size(); i++)
					{
						// If any Bidder constraint is not satisfied, mark bidder constraint as failed
						// We will disregard an item that has failed any bidder constraint
						if(!bidders.contains(bidderConstraints.get(i)))
							constraintFailed = true;
					}
					
		    		// If we didn't fail any bidder constraints, add this Item to the search results
					if(!constraintFailed)
					{
						SearchResult result = new SearchResult(Integer.toString(id), name);
		    			mySQLResults.add(result);
					}
					// Otherwise continue to the next possible item
					else
					{
						continue;
					}
	    		}
	    		else
	    		{
	    			SearchResult result = new SearchResult(Integer.toString(id), name);
	    			mySQLResults.add(result);
	    		}
	    		
	    	}
    		
    	
    	} catch (SQLException ex) {
    		System.out.println(ex);
    		return new SearchResult[0];
    	}
    	
		
		// Create the intersection of List<SearchResult> luceneResults List<SearchResult> mySQLResults
    	List<SearchResult> fullResults = new ArrayList<SearchResult>();
    	
    	// If there are no lucene constraints, use mysql results
    	if(!luceneConstraintsExist)
    	{
    		fullResults = mySQLResults;
    	}
    	// If lucene constraints exist and there are no sql constraints, lucene results are valid
    	else if(!sqlConstraintsExist)
    	{
    		fullResults = luceneResults;
    	}
    	// Otherwise we must calculate the intersection
    	else
    	{
    		for(int i=0; i < luceneResults.size(); i++)
    		{
    			for(int j=0; j < mySQLResults.size(); j++)
    			{
    				SearchResult lucene = luceneResults.get(i);
    				SearchResult mySQL = mySQLResults.get(j);
    				
    				// Compare current lucene result and mysql result
    				if(lucene.getItemId().equals(mySQL.getItemId()) && lucene.getName().equals(mySQL.getName()))
    				{
    					fullResults.add(lucene);
    				}
    			}
    		}
    	}
    	
		// Return the specified offset/number of results
    	List<SearchResult> results = new ArrayList<SearchResult>();
		
		for(int i = numResultsToSkip; i < fullResults.size(); i++)
		{
			// Add the current item to our subset list
			results.add(fullResults.get(i));
			
			// Break if we've fulfilled the desired numResultsToReturn
			// Note that this handles the requirement:
			//  "In case numResultsToReturn is 0, return ALL matching items starting from numResultsToSkip"
			//  because this comparison will never be made until results.size() equals at least 1
			if(results.size() == numResultsToReturn)
				break;
		}
		
		return results.toArray(new SearchResult[results.size()]);
	}

	public String getXMLDataForItemId(String itemId) {
		
		
		// This requires a series of SQL queries to get all the data required to compose one item
		//  Item query to get general info				
		//  ItemSeller query to get seller's ID			
		//  User query to get Seller's info		
		//  ItemCategory query to get categories		
		//  Bid query to get all bidders			
		//  Series of User queries to get bidder info
		
		
		// Create connection and statement variables
		Connection conn = null;
		Statement stmt = null;
		
		// Objects to build
		Item item = null;
		String sellerId = null;
		User seller = null;
		ArrayList<String> categories = null;
		ArrayList<Bid> bids = null;
		ArrayList<User> bidders = null;
		
		// Execute the queries
    	try {
    		
    		conn = DbManager.getConnection(true);
			stmt = conn.createStatement();

			// Item query to get general info
	    	ResultSet itemRS = stmt.executeQuery("SELECT * FROM Item WHERE id =" + itemId);
	    	while (itemRS.next()) 
	    	{
	    		// Check if we're somehow analyzing a second matching item
	    		if(item != null)
	    		{
	    			System.out.println("Database inconsistency, duplicate Items");
	    			break;
	    		}
	    		
	    		item = new Item();
	    		
	    		item.itemID = itemRS.getInt("id");
	    		item.name = itemRS.getString("name");
	    		item.buyPrice = Float.toString(itemRS.getFloat("buy_price"));
	    		item.firstMinimumBid = Float.toString(itemRS.getFloat("first_bid"));
	    		
	    		try {
					item.started = sqlFormat.parse(itemRS.getString("started"));
		    		item.ends = sqlFormat.parse(itemRS.getString("ends"));
				} catch (java.text.ParseException e) {
					System.out.println("Error parsing date");
					e.printStackTrace();
				}
	    		
	    		item.description = itemRS.getString("description");
	    		item.numBids = itemRS.getInt("number_of_bids");
	    		item.currentBidAmount = Float.toString(itemRS.getFloat("currently"));
	    	}
			
			// ItemSeller query to get seller's ID
	    	ResultSet sellerIdRS = stmt.executeQuery("SELECT uid FROM ItemSeller WHERE iid =" + itemId);
	    	while (sellerIdRS.next()) 
	    	{
	    		// Check if we're somehow analyzing a second matching seller
	    		if(sellerId != null)
	    		{
	    			System.out.println("Database inconsistency, duplicate seller id");
	    			break;
	    		}
	    		
	    		sellerId = sellerIdRS.getString("uid");
	    	}
			
			// User query to get Seller's info
	    	ResultSet sellerRS = stmt.executeQuery("SELECT * from User WHERE id LIKE \"" + sellerId + "\"");
	    	while (sellerRS.next()) 
	    	{
	    		// Check if we're somehow analyzing a second matching user
	    		if(seller != null)
	    		{
	    			System.out.println("Database inconsistency, duplicate seller User");
	    			break;
	    		}
	    		
	    		seller = new User();
	    		
	    		seller.userID = sellerRS.getString("id");
	    		seller.rating = sellerRS.getInt("rating");
	    		seller.location = sellerRS.getString("location");
	    		seller.country = sellerRS.getString("country");
	    	}
	    	
			// ItemCategory query to get categories
	    	ResultSet categoriesRS = stmt.executeQuery("SELECT category FROM ItemCategory WHERE iid = " + itemId);
	    	categories = new ArrayList<String>();
	    	while (categoriesRS.next()) 
	    	{
	    		categories.add(categoriesRS.getString("category"));
	    	}
	    	
			// Bid query to get all bids
	    	ResultSet bidsRS = stmt.executeQuery("SELECT * FROM Bid WHERE iid = " + itemId);
	    	bids = new ArrayList<Bid>();
	    	while (bidsRS.next()) 
	    	{
	    		Bid bid = new Bid();
	    		
	    		bid.itemID = bidsRS.getInt("iid");
	    		
	    		try {
					bid.time = sqlFormat.parse(bidsRS.getString("time"));
				} catch (java.text.ParseException e) {
					System.out.println("Error parsing date");
					e.printStackTrace();
				}
	    		
	    		bid.userID = bidsRS.getString("uid");
	    		bid.amount = Float.toString(bidsRS.getFloat("amount"));
	    		    		
	    		bids.add(bid);
	    	}
			
			// Series of User queries to get bidder info
	    	bidders = new ArrayList<User>();
	    	for(int i = 0; i < bids.size(); i++)
	    	{
	    		ResultSet bidderRS = stmt.executeQuery("SELECT * FROM User WHERE id LIKE \"" + bids.get(i).userID + "\"");
	    		User bidder = null;
	    		while (bidderRS.next()) 
		    	{
		    		// Check if we're somehow analyzing a second matching user
		    		if(bidder != null)
		    		{
		    			System.out.println("Database inconsistency, duplicate bidder User");
		    			break;
		    		}
		    		
		    		bidder = new User();
		    		
		    		bidder.userID = bidderRS.getString("id");
		    		bidder.rating = bidderRS.getInt("rating");
		    		bidder.location = bidderRS.getString("location");
		    		bidder.country = bidderRS.getString("country");
		    	}
	    		bidders.add(bidder);	
	    	}
	    	
    	} catch (SQLException ex) {
    		System.out.println(ex);
    		return null;
    	}
		
    	// With the following items populated, building the XML response should be trivial
		//  Item item = null;
		//  String sellerId = null;
		//  User seller = null;
		//  ArrayList<String> categories = null;
		//  ArrayList<String> bidderIds = null;
		//  ArrayList<User> bidders = null;
    	
    	
    	XMLBuilder xmlBuilder = new XMLBuilder();
		return xmlBuilder.itemXMLString(item, seller, categories, bids, bidders);
	}
	
	public String echo(String message) {
		return message;
	}
}
