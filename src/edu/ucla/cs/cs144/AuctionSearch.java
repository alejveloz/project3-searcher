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
				endTimeConstraint = constraint.getValue(); 
			}
			else if(constraint.getFieldName().equals(FieldName.BidderId))
			{
				bidderConstraints.add(constraint.getValue());
			}
		}
		
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
		List<SearchResult> luceneResults = new ArrayList<SearchResult>();
		if(luceneQuery != null)
		{
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
		
		
		// Narrow down one constraint at a time
		
		// First by seller ID
		// SELECT id, name, ends, buy_price FROM Item, ItemSeller WHERE Item.id = ItemSeller.iid AND ItemSeller.uid = [SellerId] AND ....
		
		// This gives us all items sold by the specified seller
		// Result1(id, name, ends, buy_price)
		
		// Next narrow by buy price and end time
		// SELECT (id, name) FROM Result1 WHERE ends = [EndTime] AND buy_price = [BuyPrice] ...
		
		// This gives us all items sold by specified seller with specified end time and specified buy price
		// Result2(id, name)
		
		// It's likley that this will narrow it down to all desired items, but finally we must check they satisfy bidder constraints
		// We'll take the results of the above query and process each one
		
		// For each remaining item
		// Get all the bidders on the item
		// Create an array list of all the bidders
		// For each Bidder constraint, verify it is contained in the array list
		// If any Bidder constraint is not satisfied, disregard this item
		// If all Bidder constraints are satisfied, add this Item to the search results
		
		// Build SearchResults[] mySQLResults
		
		
		// Create the intersection of luceneResults[] and mySQLResults[]
		
		// Return the specified offset/number of results
		
		return new SearchResult[0];
	}

	public String getXMLDataForItemId(String itemId) {
		// TODO: Your code here!
		return null;
	}
	
	public String echo(String message) {
		return message;
	}
}
