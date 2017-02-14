package org.pathwayloom.chembl;

import org.pathvisio.core.data.GdbManager;
import org.pathwayloom.SuggestionAdapter;

public class ChEMBLCompoundSpaqrlPlugin extends SuggestionAdapter {
	final String queryNodeID = "Compound_targets_interactions.sparql";
	
	public ChEMBLCompoundSpaqrlPlugin(GdbManager gdbManager){
		this.gdbManager = gdbManager;
		this.sparqlQueryNode = map.get(queryNodeID);
		this.menu_label = sparqlQueryNode.getMenu_label();

		this.idParameter = "uniprot";
		this.labelParameter = "label";
		this.typeInteraction = "compound -> targets";
		this.typeDataNode = "Protein";
		this.systemCode = "Cl";
		this.interactionResultsHandler = new ChEMBLProteinResultsHandler();
	}
}