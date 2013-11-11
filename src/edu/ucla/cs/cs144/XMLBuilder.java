package edu.ucla.cs.cs144;

import java.io.File;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
 
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLBuilder {


	SimpleDateFormat inputFormat;
	
	public XMLBuilder()
	{
		inputFormat = new SimpleDateFormat("MMM-dd-yy HH:mm:ss");
	}
	
	public String itemXMLString(Item item, User seller, List<String> categories, List<Bid> bids, List<User> bidders)
	{

		/*
		 * <!ELEMENT Items            (Item*)>
		   <!ELEMENT Item             (Name, Category+, Currently, Buy_Price?,
                            First_Bid, Number_of_Bids,
			    Bids, Location, Country, Started, Ends,
			    Seller, Description)>
		   <!ATTLIST Item             ItemID CDATA #REQUIRED>
		     <!ELEMENT Name	   (#PCDATA)>
		     <!ELEMENT Category	   (#PCDATA)> 
		     <!ELEMENT Currently	   (#PCDATA)> 
		     <!ELEMENT Buy_Price      (#PCDATA)>
		     <!ELEMENT First_Bid	   (#PCDATA)>
		     <!ELEMENT Number_of_Bids (#PCDATA)>
		     <!ELEMENT Bids           (Bid*)>
		       <!ELEMENT Bid          (Bidder, Time, Amount)>
		         <!ATTLIST Bidder     UserID CDATA #REQUIRED
		   			   Rating CDATA #REQUIRED>    
		         <!ELEMENT Bidder     (Location?, Country?)>
		         <!ELEMENT Time	   (#PCDATA)>
		         <!ELEMENT Amount	   (#PCDATA)>
		     <!ELEMENT Location	   (#PCDATA)>
		     <!ELEMENT Country	   (#PCDATA)>
		     <!ELEMENT Started	   (#PCDATA)>
		     <!ELEMENT Ends	   (#PCDATA)>
		     <!ELEMENT Seller	   EMPTY>
		     <!ATTLIST Seller         UserID CDATA #REQUIRED
		   			   Rating CDATA #REQUIRED>
		     <!ELEMENT Description	   (#PCDATA)>
		 */

		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// Item root element
			Document doc = docBuilder.newDocument();
			Element itemElement = doc.createElement("Item");
			doc.appendChild(itemElement);
			
			// ItemID attribute
			Attr attr = doc.createAttribute("ItemID");
			attr.setValue(Integer.toString(item.itemID));
			itemElement.setAttributeNode(attr);

			// Name element
			Element nameElement = doc.createElement("Name");
			itemElement.appendChild(nameElement);
			nameElement.appendChild(doc.createTextNode(item.name));
			
			// Category elements
			for(int i = 0; i < categories.size(); i++)
			{
				Element categoryElement = doc.createElement("Category");
				itemElement.appendChild(categoryElement);
				categoryElement.appendChild(doc.createTextNode(categories.get(i)));
			}
			
			// Current element
			Element currentlyElement = doc.createElement("Currently");
			itemElement.appendChild(currentlyElement);
			currentlyElement.appendChild(doc.createTextNode(item.currentBidAmount));

			// Buy_Price element
			if(item.buyPrice != null)
			{
				Element buyPriceElement = doc.createElement("Buy_Price");
				itemElement.appendChild(buyPriceElement);
				buyPriceElement.appendChild(doc.createTextNode(item.buyPrice));
			}
			
			// First_Bid element
			Element firstBidElement = doc.createElement("First_Bid");
			itemElement.appendChild(firstBidElement);
			firstBidElement.appendChild(doc.createTextNode(item.firstMinimumBid));
			
			// Number_of_Bids element
			Element numberOfBidsElement = doc.createElement("Number_of_Bids");
			itemElement.appendChild(numberOfBidsElement);
			numberOfBidsElement.appendChild(doc.createTextNode(Integer.toString(item.numBids)));
			
			// Bids
			Element bidsElement = doc.createElement("Bids");
			itemElement.appendChild(bidsElement);
			
			// Bids children
			for(int i = 0; i < bids.size(); i++)
			{
				// Bid element
				Element bidElement = doc.createElement("Bid");
				bidsElement.appendChild(bidElement);
				
				// Bidder element
				Element bidderElement = doc.createElement("Bidder");
				bidElement.appendChild(bidderElement);
				
				// Bidder attributes
				bidderElement.setAttribute("UserID", bids.get(i).userID);
				bidderElement.setAttribute("Rating", Integer.toString(bidders.get(i).rating));
				
				// Location element
				if(bidders.get(i).location != null)
				{
					Element locationElement = doc.createElement("Location");
					bidderElement.appendChild(locationElement);
					locationElement.appendChild(doc.createTextNode(bidders.get(i).location));
				}
				
				// Country element
				if(bidders.get(i).country != null)
				{
					Element countryElement = doc.createElement("Country");
					bidderElement.appendChild(countryElement);
					countryElement.appendChild(doc.createTextNode(bidders.get(i).country));
				}
				
				// Time element
				Element timeElement = doc.createElement("Time");
				bidElement.appendChild(timeElement);
				timeElement.appendChild(doc.createTextNode(inputFormat.format(bids.get(i).time)));

				// Amount element
				Element amountElement = doc.createElement("Amount");
				bidElement.appendChild(amountElement);
				amountElement.appendChild(doc.createTextNode(bids.get(i).amount));
				
			}

			// Location element
			Element locationElement = doc.createElement("Location");
			itemElement.appendChild(locationElement);
			locationElement.appendChild(doc.createTextNode(seller.location));

			// Country element
			Element countryElement = doc.createElement("Country");
			itemElement.appendChild(countryElement);
			countryElement.appendChild(doc.createTextNode(seller.country));
			
			// Started element
			Element startedElement = doc.createElement("Started");
			itemElement.appendChild(startedElement);
			startedElement.appendChild(doc.createTextNode(inputFormat.format(item.started)));

			// Ends element
			Element endsElement = doc.createElement("Ends");
			itemElement.appendChild(endsElement);
			endsElement.appendChild(doc.createTextNode(inputFormat.format(item.ends)));
			
			// Seller element
			Element sellerElement = doc.createElement("Seller");
			itemElement.appendChild(sellerElement);

			// Seller attributes
			sellerElement.setAttribute("UserID", seller.userID);
			sellerElement.setAttribute("Rating", Integer.toString(seller.rating));

			// Description element
			Element descriptionElement = doc.createElement("Description");
			itemElement.appendChild(descriptionElement);
			descriptionElement.appendChild(doc.createTextNode(item.description));

			
			// Write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(doc), new StreamResult(writer));
			return writer.getBuffer().toString();

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}

		return "";
	}
}
