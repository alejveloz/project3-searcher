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
		
		SearchConstraint[] constraints1 = {new SearchConstraint(FieldName.BuyPrice, "5.99")};
		SearchResult[] advancedResults1 = as.advancedSearch(constraints1, 0, 20);
		System.out.println("Advanced Seacrh");
		System.out.println("Received " + advancedResults1.length + " results");
		for(SearchResult result : advancedResults1) {
			//System.out.println(result.getItemId() + ": " + result.getName());
		}
		System.out.println("");
		
		SearchConstraint[] constraints2 = {
				new SearchConstraint(FieldName.ItemName, "pan"),
				new SearchConstraint(FieldName.Category, "kitchenware")
				};
		SearchResult[] advancedResults2 = as.advancedSearch(constraints2, 0, 20);
		System.out.println("Advanced Seacrh");
		System.out.println("Received " + advancedResults2.length + " results");
		for(SearchResult result : advancedResults2) {
			//System.out.println(result.getItemId() + ": " + result.getName());
		}
		System.out.println("");
		
		SearchConstraint[] constraints3 = {
				new SearchConstraint(FieldName.ItemName, "Precious Moments"),
				new SearchConstraint(FieldName.SellerId, "waltera317a")
				};
		SearchResult[] advancedResults3 = as.advancedSearch(constraints3, 0, 20);
		System.out.println("Advanced Seacrh");
		System.out.println("Received " + advancedResults3.length + " results");
		for(SearchResult result : advancedResults3) {
			//System.out.println(result.getItemId() + ": " + result.getName());
		}
		System.out.println("");
		
		SearchConstraint[] constraints4 = {
				new SearchConstraint(FieldName.EndTime, "Dec-14-01 21:00:05")
				};
		SearchResult[] advancedResults4 = as.advancedSearch(constraints4, 0, 20);
		System.out.println("Advanced Seacrh");
		System.out.println("Received " + advancedResults4.length + " results");
		for(SearchResult result : advancedResults4) {
			//System.out.println(result.getItemId() + ": " + result.getName());
		}
		System.out.println("");
		
		String itemId = "1044078197";
		String item = as.getXMLDataForItemId(itemId);
		System.out.println("XML data for ItemId: " + itemId);
		System.out.println(item);
		System.out.println("");

		// Add your own test here
	}
}
