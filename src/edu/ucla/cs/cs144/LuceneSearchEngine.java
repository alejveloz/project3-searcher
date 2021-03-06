package edu.ucla.cs.cs144;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;

public class LuceneSearchEngine {
  private IndexSearcher searcher = null;
  private QueryParser parser = null;

  /** Creates a new instance of SearchEngine */

  public LuceneSearchEngine() throws IOException {
    searcher = new IndexSearcher(System.getenv("LUCENE_INDEX") + "/index1");
    parser = new QueryParser("content", new StandardAnalyzer()); // "content" is default search field
  }

  public Hits performBasicSearch(String queryString) throws IOException, ParseException {

	// Sanitize the query string because this is a basic search
	String santizedQuery = QueryParser.escape(queryString);
	  
	// Create the query and execute the search
    Query query = parser.parse(santizedQuery);
    Hits hits = searcher.search(query);
    return hits;
  }
  
  public Hits performAdvancedSearch(String queryString) throws IOException, ParseException {

		// No need to sanitize query string because we assume it is correctly formed
		  
		// Create the query and execute the search
	    Query query = parser.parse(queryString);
	    Hits hits = searcher.search(query);
	    return hits;
	  }
}
