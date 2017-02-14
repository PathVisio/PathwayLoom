package org.pathwayloom.uniprot;

import org.pathvisio.core.data.GdbManager;
import org.pathwayloom.SuggestionAdapter;

public class UniprotProteinSparqlPlugin extends SuggestionAdapter {
	static final String queryNodeID = "Protein_protein_interactions.sparql";	
	
	public UniprotProteinSparqlPlugin(GdbManager gdbManager)
	{
		this.gdbManager = gdbManager;
		this.sparqlQueryNode = map.get(queryNodeID);
		this.menu_label = sparqlQueryNode.getMenu_label();

		this.idParameter = "targetprotein";
		this.labelParameter = "label";
		this.typeInteraction = "Protein-Protein Interaction";
		this.typeDataNode = "Protein";
		this.systemCode = "S";
		this.interactionResultsHandler = new UniprotResultsHandler();
	}
}
