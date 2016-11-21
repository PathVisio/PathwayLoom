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
	private GdbManager gdbManager;
	protected DataSource dataSource;
	protected String inputID;
	protected String inputLabel;
	
	public UniprotProteinSparqlPlugin(GdbManager gdbManager)
	{
		this.gdbManager = gdbManager;
	}
	
	@Override public Pathway doSuggestion(PathwayElement input) throws SuggestionException  {

		dataSource = input.getDataSource();
		inputID = input.getElementID();
		inputLabel = input.getTextLabel();
		Xref xref = new Xref(inputID, dataSource);
		try {
			Set<Xref> setRef  = gdbManager.getGeneDb().
					mapID(xref,DataSource.getExistingBySystemCode("S"));
			if (!setRef.isEmpty())
				inputID = setRef.iterator().next().getId();
		}catch (NullPointerException e){
			JOptionPane.showMessageDialog(null,
					"Import a gene mapping database may improve your result");
		}catch (IDMapperException e){
			JOptionPane.showMessageDialog(null,
					"Import a gene mapping database may improve your result");
			e.printStackTrace();
		}
		
		
		PathwayElement pelt = PathwayElement.createPathwayElement(ObjectType.DATANODE);
		pelt.setMWidth (PppPlugin.DATANODE_MWIDTH);
		pelt.setMHeight (PppPlugin.DATANODE_MHEIGHT);
		pelt.setTextLabel(input.getTextLabel());
		
		pelt.setCopyright("Copyright notice");
		pelt.setDataNodeType(input.getDataNodeType());
		pelt.setGraphId(input.getGraphId());
		pelt.addComment(pelt.getGraphId(), "ParentGraphId");
		pelt.addComment("True", "Input");
		
		List<PathwayElement> spokes = new ArrayList<PathwayElement>();
		
		pelt.setDataSource(dataSource);
		pelt.setElementID(inputID);
		
		String endpoint = "http://sparql.uniprot.org/sparql/";		
		String sparqlQuery = 
				"PREFIX keywords:<http://purl.uniprot.org/keywords/> \n"+
				"PREFIX uniprotkb:<http://purl.uniprot.org/uniprot/> \n"+
				"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> \n"+
				"PREFIX up:<http://purl.uniprot.org/core/> \n"+
				"SELECT DISTINCT ?targetprotein ?label WHERE { \n"+

				"VALUES ?sourceprotein {uniprotkb:"+inputID+"}\n"+
				"?sourceprotein up:interaction ?source .\n"+
				"?targetprotein up:interaction ?target .\n"+
				"?targetprotein	rdfs:label ?label .\n"+
				"?source up:participant ?t .\n"+
				"?target up:participant  ?t .\n"+
		        "?targetprotein ?p ?a.\n"+
		   		"FILTER (?sourceprotein != ?targetprotein)\n"+
				"}";

		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpoint, query);
		ResultSet resultSet = queryExecution.execSelect();
		
		UniprotResultsHandler interactionResultsHandler = new UniprotResultsHandler();
		
		String type = "Protein";
		while (resultSet.hasNext()) {
			QuerySolution solution = resultSet.next();

			String targetURI = solution.get("targetprotein").toString();
			String targetLabel = solution.get("label").asLiteral().getLexicalForm();
//			System.out.println(targetURI + "\t" + targetLabel );
			SourceInteraction sourceInteraction = new SourceInteraction(inputID,inputLabel,type);
			TargetInteraction targetInteraction = new TargetInteraction(targetURI,targetLabel,type);
			
			InteractionBinaryResults interactionBinaryResults = new InteractionBinaryResults(
					inputID,"Protein-Protein Interaction","NA",inputID+targetURI,pelt.getGraphId());			
			
			interactionResultsHandler.add(interactionBinaryResults, sourceInteraction, targetInteraction);
		}
		spokes = interactionResultsHandler.getBinaryResults();
		Pathway result = PathwayBuilder.radialLayout(pelt, spokes);
		return result;
	}
}
