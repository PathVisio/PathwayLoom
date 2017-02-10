package org.pathwayloom.uniprot;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.bridgedb.DataSource;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.pathvisio.core.data.GdbManager;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathwayloom.PathwayBuilder;
import org.pathwayloom.PppPlugin;
import org.pathwayloom.SuggestionAdapter;
import org.pathwayloom.Suggestion.SuggestionException;
import org.pathwayloom.utils.InteractionBinaryResults;
import org.pathwayloom.utils.SourceInteraction;
import org.pathwayloom.utils.TargetInteraction;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class UniprotProteinSparqlPlugin extends SuggestionAdapter {
	static final String queryNodeID = "Protein_protein_interactions.sparql";	
	
//	public UniprotProteinSparqlPlugin(GdbManager gdbManager)
//	{
//		this.gdbManager = gdbManager;
//	}
	public UniprotProteinSparqlPlugin(GdbManager gdbManager)
	{
		this.gdbManager = gdbManager;
		this.sparqlQueryNode = map.get(queryNodeID);

		this.idParameter = "targetprotein";
		this.labelParameter = "label";
		this.typeInteraction = "Protein-Protein Interaction";
		this.typeDataNode = "Protein";
		this.systemCode = "S";
		this.interactionResultsHandler = new UniprotResultsHandler();
	}
	@Override 
	public PathwayBuilder doSuggestion(PathwayElement input) throws SuggestionException  {

		this.dataSource = input.getDataSource();
		this.inputID = input.getElementID();
		this.inputLabel = input.getTextLabel();
		this.interactionResultsHandler = new UniprotResultsHandler();
		
		mapping(systemCode);
		updateTaxon(input);
		if (this.taxon==null){
			getOrganismError();
			return new PathwayBuilder();
		}		
		PathwayElement pelt = createPathwayElement(input);
		List<PathwayElement> spokes = doQuery(pelt.getGraphId());	

		PathwayBuilder result = PathwayBuilder.radialLayout(pelt, spokes);
		return result;
	}
}
