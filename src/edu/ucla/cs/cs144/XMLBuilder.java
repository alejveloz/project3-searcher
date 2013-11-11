package edu.ucla.cs.cs144;

import java.io.File;
import java.io.StringWriter;
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

	public XMLBuilder()
	{
		
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
			
			// staff elements
			/*Element staff = doc.createElement("Staff");
			rootElement.appendChild(staff);

			// set attribute to staff element
			Attr attr = doc.createAttribute("id");
			attr.setValue("1");
			staff.setAttributeNode(attr);

			// shorten way
			// staff.setAttribute("id", "1");

			// firstname elements
			Element firstname = doc.createElement("firstname");
			firstname.appendChild(doc.createTextNode("yong"));
			staff.appendChild(firstname);

			// lastname elements
			Element lastname = doc.createElement("lastname");
			lastname.appendChild(doc.createTextNode("mook kim"));
			staff.appendChild(lastname);

			// nickname elements
			Element nickname = doc.createElement("nickname");
			nickname.appendChild(doc.createTextNode("mkyong"));
			staff.appendChild(nickname);

			// salary elements
			Element salary = doc.createElement("salary");
			salary.appendChild(doc.createTextNode("100000"));
			staff.appendChild(salary);
			*/
			
			
			// write the content into xml file
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
