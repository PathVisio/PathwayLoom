package org.pathwayloom.chembl;

import org.pathvisio.core.data.GdbManager;
import org.pathwayloom.SuggestionAdapter;

public class ChEMBLProteinSparqlPlugin extends SuggestionAdapter {
	final String queryNodeID = "Protein_compound_interactions.sparql";
	
	public ChEMBLProteinSparqlPlugin(GdbManager gdbManager){
		this.gdbManager = gdbManager;
		this.sparqlQueryNode = map.get(queryNodeID);
		this.menu_label = sparqlQueryNode.getMenu_label();
		
		this.idParameter = "compound";
		this.labelParameter = "label";
		this.typeInteraction = "protein -> compounds";
		this.typeDataNode = "Metabolite";
		this.systemCode = "S";
		this.interactionResultsHandler = new ChEMBLProteinResultsHandler();
	}
}
