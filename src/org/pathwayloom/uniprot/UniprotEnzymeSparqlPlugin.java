package org.pathwayloom.uniprot;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.bridgedb.DataSource;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.bridgedb.bio.Organism;
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

public class UniprotEnzymeSparqlPlugin extends SuggestionAdapter {
	private GdbManager gdbManager;
	protected DataSource dataSource;
	protected String inputID;
	protected String inputLabel;
	
	public UniprotEnzymeSparqlPlugin(GdbManager gdbManager)
	{
		this.gdbManager = gdbManager;
	}
	
	@Override public Pathway doSuggestion(PathwayElement input) throws SuggestionException  {

		dataSource = input.getDataSource();
		inputID = input.getElementID();
		inputLabel = input.getTextLabel();
		Xref xref = new Xref(inputID, dataSource);
		try {
			if ( !dataSource.getSystemCode().equals("S") ){
				Set<Xref> setRef  = gdbManager.getGeneDb().
						mapID(xref,DataSource.getExistingBySystemCode("S"));
				if (!setRef.isEmpty())
					inputID = setRef.iterator().next().getId();
			}	
		}catch (NullPointerException e){
			JOptionPane.showMessageDialog(null,
					"Import a gene mapping database may improve your result");
		}catch (IDMapperException e){
			JOptionPane.showMessageDialog(null,
					"Import a gene mapping database may improve your result");
			e.printStackTrace();
		}

		Organism species = Organism.fromLatinName(input.getParent().getMappInfo().getOrganism());
		String taxon = "";
		if (species!=null){
			taxon = species.taxonomyID().getId();
		}
		else{
			JOptionPane.showMessageDialog(null,
					"Please define the pathway organism","Organism Error", JOptionPane.ERROR_MESSAGE);
			return new Pathway();
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
				"PREFIX taxon:<http://purl.uniprot.org/taxonomy/>  \n"+
				"PREFIX uniprotkb:<http://purl.uniprot.org/uniprot/> \n"+
				"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> \n"+
				"PREFIX up:<http://purl.uniprot.org/core/> \n"+
				"SELECT DISTINCT ?enzyme ?label WHERE { \n"+

				"VALUES ?protein {uniprotkb:"+inputID+"}\n"+
				"?protein up:enzyme ?enzyme .\n"+
				"?protein rdfs:label ?label .\n"+
				"?protein up:organism taxon:"+taxon+" .\n"+
				"}";

		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpoint, query);
		ResultSet resultSet = queryExecution.execSelect();
		
		UniprotEnzymeResultsHandler interactionResultsHandler = new UniprotEnzymeResultsHandler();
		
		String type = "Protein";
		while (resultSet.hasNext()) {
			QuerySolution solution = resultSet.next();

			String targetURI = solution.get("enzyme").toString();
			String targetLabel = solution.get("label").asLiteral().getLexicalForm();
			SourceInteraction sourceInteraction = new SourceInteraction(inputID,inputLabel,type);
			TargetInteraction targetInteraction = new TargetInteraction(targetURI,targetLabel,type);
			
			InteractionBinaryResults interactionBinaryResults = new InteractionBinaryResults(
					inputID,"Protein-Enzyme Interaction","",inputID+targetURI,pelt.getGraphId());			
			
			interactionResultsHandler.add(interactionBinaryResults, sourceInteraction, targetInteraction);
		}
		spokes = interactionResultsHandler.getBinaryResults();
		Pathway result = PathwayBuilder.radialLayout(pelt, spokes);
		return result;
	}
}