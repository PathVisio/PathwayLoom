package org.pathwayloom.wikidata;

import java.util.List;

import org.pathvisio.core.data.GdbManager;
import org.pathvisio.core.model.PathwayElement;
import org.pathwayloom.PathwayBuilder;
import org.pathwayloom.SuggestionAdapter;
import org.pathwayloom.uniprot.UniprotResultsHandler;

public class WikidataEncodesSparqlPlugin extends SuggestionAdapter  {
	static final String queryNodeID = "encodes.sparql";	
	
	public WikidataEncodesSparqlPlugin(GdbManager gdbManager){
		this.gdbManager = gdbManager;
		this.sparqlQueryNode = map.get(queryNodeID);
		this.menu_label = sparqlQueryNode.getMenu_label();

		this.idParameter = "uniprotId";
		this.labelParameter = "proteinLabel";
		this.typeInteraction = "Encodes";
		this.typeDataNode = "Protein";
		this.systemCode = "L";
		this.interactionResultsHandler = new UniprotResultsHandler();
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