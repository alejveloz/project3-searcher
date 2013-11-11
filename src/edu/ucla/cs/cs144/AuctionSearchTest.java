package edu.ucla.cs.cs144;

import java.util.Calendar;
import java.util.Date;

import edu.ucla.cs.cs144.AuctionSearch;
import edu.ucla.cs.cs144.SearchResult;
import edu.ucla.cs.cs144.SearchConstraint;
import edu.ucla.cs.cs144.FieldName;

public class AuctionSearchTest {
	public static void main(String[] args1)
	{
		AuctionSearch as = new AuctionSearch();

		String message = "Test message";
		String reply = as.echo(message);
		System.out.println("Reply: " + reply);
		System.out.println("");
		
		String query1 = "superman";
		SearchResult[] basicResults1 = as.basicSearch(query1, 0, 0);
		System.out.println("Basic Search Query: " + query1);
		System.out.println("Received " + basicResults1.length + " results");
		for(SearchResult result : basicResults1) {
			//System.out.println(result.getItemId() + ": " + result.getName());
		}
		System.out.println("");
		
		String query2 = "kitchenware";
		SearchResult[] basicResults2 = as.basicSearch(query2, 0, 0);
		System.out.println("Basic Search Query: " + query2);
		System.out.println("Received " + basicResults2.length + " results");
		for(SearchResult result : basicResults2) {
			//System.out.println(result.getItemId() + ": " + result.getName());
		}
		System.out.println("");
		
		String query3 = "star trek";
		SearchResult[] basicResults3 = as.basicSearch(query3, 0, 0);
		System.out.println("Basic Search Query: " + query3);
		System.out.println("Received " + basicResults3.length + " results");
		for(SearchResult result : basicResults3) {
			//System.out.println(result.getItemId() + ": " + result.getName());
		}
		System.out.println("");
		
		SearchConstraint constraint =
		    new SearchConstraint(FieldName.BuyPrice, "5.99"); 
		SearchConstraint[] constraints = {constraint};
		SearchResult[] advancedResults = as.advancedSearch(constraints, 0, 20);
		System.out.println("Advanced Seacrh");
		System.out.println("Received " + advancedResults.length + " results");
		for(SearchResult result : advancedResults) {
			System.out.println(result.getItemId() + ": " + result.getName());
		}
		System.out.println("");
		
		String itemId = "1497595357";
		String item = as.getXMLDataForItemId(itemId);
		System.out.println("XML data for ItemId: " + itemId);
		System.out.println(item);
		System.out.println("");

		// Add your own test here
	}
}
