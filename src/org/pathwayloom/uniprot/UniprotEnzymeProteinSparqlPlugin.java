package org.pathwayloom.uniprot;

import org.pathvisio.core.data.GdbManager;
import org.pathwayloom.SuggestionAdapter;

public class UniprotEnzymeProteinSparqlPlugin extends SuggestionAdapter {

	static final String queryNodeID = "Enzyme_protein_interactions.sparql";	

	public UniprotEnzymeProteinSparqlPlugin(GdbManager gdbManager){
		this.gdbManager = gdbManager;
		this.sparqlQueryNode = map.get(queryNodeID);
		this.menu_label = sparqlQueryNode.getMenu_label();
		
		this.idParameter = "protein";
		this.labelParameter = "label";
		this.typeInteraction = "Enzyme-Protein Interaction";
		this.typeDataNode = "Protein";
		this.systemCode = "E";
		this.interactionResultsHandler = new UniprotResultsHandler();
	}
//	@Override 
//	public PathwayBuilder doSuggestion(PathwayElement input) throws SuggestionException {
//
//		this.dataSource = input.getDataSource();
//		this.inputID = input.getElementID();
//		this.inputLabel = input.getTextLabel();
//		this.interactionResultsHandler = new UniprotResultsHandler();
//		
//		mapping(systemCode);
//		updateTaxon(input);
//		if (this.taxon==null){
//			getOrganismError();
//			return new PathwayBuilder();
//		}		
//		PathwayElement pelt = createPathwayElement(input);
//		List<PathwayElement> spokes = doQuery(pelt.getGraphId());	
//
//		PathwayBuilder result = PathwayBuilder.radialLayout(pelt, spokes);
//		return result;
//	}
}