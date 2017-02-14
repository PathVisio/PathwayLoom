package org.pathwayloom.wikidata;

import java.util.List;

import org.pathvisio.core.data.GdbManager;
import org.pathvisio.core.model.PathwayElement;
import org.pathwayloom.PathwayBuilder;
import org.pathwayloom.SuggestionAdapter;

public class WikidataEncodedBySparqlPlugin extends SuggestionAdapter  {
	static final String queryNodeID = "encoded_by.sparql";	
	
	public WikidataEncodedBySparqlPlugin(GdbManager gdbManager){
		this.gdbManager = gdbManager;
		this.sparqlQueryNode = map.get(queryNodeID);
		this.menu_label = sparqlQueryNode.getMenu_label();

		this.idParameter = "geneId";
		this.labelParameter = "geneLabel";
		this.typeInteraction = "Encoded_by";
		this.typeDataNode = "GeneProduct";
		this.systemCode = "S";
		this.interactionResultsHandler = new WikidataResultsHandler();
	}
	
	@Override 
	public PathwayBuilder doSuggestion(PathwayElement input) throws SuggestionException  {

		this.dataSource = input.getDataSource();
		this.inputID = input.getElementID();
		this.inputLabel = input.getTextLabel();
		this.interactionResultsHandler.clear();
		
		mapping(systemCode);
	
		PathwayElement pelt = createPathwayElement(input);
		List<PathwayElement> spokes = doQuery(pelt.getGraphId());	

		PathwayBuilder result = PathwayBuilder.radialLayout(pelt, spokes);
		return result;
	}
}
