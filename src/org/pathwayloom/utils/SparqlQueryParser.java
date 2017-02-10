package org.pathwayloom.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SparqlQueryParser {
	private static Map<String, SparqlQuery> queriesMap = new HashMap<String, SparqlQuery>(); 
	private static Document doc;

	public static void init() 
			throws ParserConfigurationException, MalformedURLException,
			SAXException, IOException, JDOMException{

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		String url = "https://raw.githubusercontent.com/andrawaag/pathway_interaction_models/master/models.xml";
		SAXBuilder saxBuilder = new SAXBuilder();

		doc= saxBuilder.build(url);
		parse();
	}

	public static void parse(){
		Element root = doc.getRootElement();
		List<Element> nodes = root.getChildren();

		for (Element dataSources : nodes){
			List<Element> dataSourcesNodes = dataSources.getChildren();
			for(Element interaction : dataSourcesNodes){

				String endpoint = interaction.getChild("SPARQL").getText();
				System.out.println(endpoint);
				String query = interaction.getChild("query").getText();
				String interaction_types  = interaction.getName();
				String source = dataSources.getName();
				String data_types = interaction.getChild("DATATYPES").getText();
				queriesMap.put(interaction_types, 
						new SparqlQuery(source, endpoint, query, data_types, interaction_types));
			}
		}
	}

	public static Map<String, SparqlQuery> getQueries(){
		return queriesMap;		
	}

}
