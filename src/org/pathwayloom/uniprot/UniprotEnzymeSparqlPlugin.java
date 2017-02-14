package org.pathwayloom.uniprot;

import org.pathvisio.core.data.GdbManager;
import org.pathwayloom.SuggestionAdapter;

public class UniprotEnzymeSparqlPlugin extends SuggestionAdapter {
	static final String queryNodeID = "Protein_enzyme_interactions.sparql";	
	
	public UniprotEnzymeSparqlPlugin(GdbManager gdbManager)
	{
		this.gdbManager = gdbManager;
		this.sparqlQueryNode = map.get(queryNodeID);
		this.menu_label = sparqlQueryNode.getMenu_label();
		
		this.idParameter = "enzyme";
		this.labelParameter = "label";
		this.typeInteraction = "Protein-Enzyme Interaction";
		this.typeDataNode = "Protein";
		this.systemCode = "S";
		this.interactionResultsHandler = new UniprotEnzymeResultsHandler();
	}
//	@Override 
//	public PathwayBuilder doSuggestion(PathwayElement input) throws SuggestionException  {
//
//		this.dataSource = input.getDataSource();
//		this.inputID = input.getElementID();
//		this.inputLabel = input.getTextLabel();
//		this.interactionResultsHandler = new UniprotEnzymeResultsHandler();
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