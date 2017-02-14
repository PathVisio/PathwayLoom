package org.pathwayloom.wikidata;

import java.util.List;

import org.pathvisio.core.data.GdbManager;
import org.pathvisio.core.model.PathwayElement;
import org.pathwayloom.PathwayBuilder;
import org.pathwayloom.SuggestionAdapter;
import org.pathwayloom.chembl.ChEMBLProteinResultsHandler;

public class WikidataCompoundSparqlPlugin extends SuggestionAdapter  {
	static final String queryNodeID = "physically_interacts_with.sparql";	
	
	public WikidataCompoundSparqlPlugin(GdbManager gdbManager){
		this.gdbManager = gdbManager;
		this.sparqlQueryNode = map.get(queryNodeID);
		this.menu_label = sparqlQueryNode.getMenu_label();

		this.idParameter = "pubchemCid";
		this.labelParameter = "compoundLabel";
		this.typeInteraction = "Physically interacts with";
		this.typeDataNode = "Protein";
		this.systemCode = "S";
		this.interactionResultsHandler = new ChEMBLProteinResultsHandler();
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