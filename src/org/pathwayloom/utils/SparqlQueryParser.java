package org.pathwayloom.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;










import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
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
//		String url = "/home/bigcat-jonathan/BiGCaT/pathway_interaction_models/models.xml";
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

				//				queriesList.add(new SparqlQuery(source, endpoint, query, data_types, interaction_types));

				queriesMap.put(interaction_types, 
						new SparqlQuery(source, endpoint, query, data_types, interaction_types));
//				System.out.println(interaction_types);
//				System.out.println(source);
				//				System.out.println(interaction.getChild("SPARQL").getText());
				//				System.out.println(interaction.getChild("query").getText());

			}
		}
		/*
//		System.out.println();
		for (int i=0; i < nodes.getLength(); i++ ){
			if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
				System.out.println(nodes.item(i).getNodeName());

				NodeList sourcesNodes = nodes.item(i).getChildNodes();

				for (int j=0; j < sourcesNodes.getLength() ; j++ ){
//					sourcesNodes.item(j);					
					if (sourcesNodes.item(j).getNodeType() == Node.ELEMENT_NODE) {
						System.out.println(sourcesNodes.item(j).getNodeName());
						sourcesNodes.item(j).get
					}
				}

				//			System.out.println(nodes.item(i).getLocalName());
				//			System.out.println(nodes.item(i).getNodeValue());



			}
		}*/
	}

	public static Map<String, SparqlQuery> getQueries(){
		return queriesMap;		
	}

}
