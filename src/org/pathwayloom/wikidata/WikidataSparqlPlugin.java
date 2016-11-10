package org.pathwayloom.wikidata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

public class WikidataSparqlPlugin  extends SuggestionAdapter {
	private GdbManager gdbManager;
	protected DataSource dataSource;
	protected String inputID;
	protected String inputLabel;
	
	public WikidataSparqlPlugin(GdbManager gdbManager)
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
					mapID(xref,DataSource.getExistingBySystemCode("L"));
			if (!setRef.isEmpty())
				inputID = setRef.iterator().next().getId();
		} catch (IDMapperException e) {
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
		
		String endpoint = "https://query.wikidata.org/sparql?";		
		String sparqlQuery = 
			"PREFIX wd: <http://www.wikidata.org/entity/> \n"+
			"PREFIX wdt: <http://www.wikidata.org/prop/direct/> \n"+
			"PREFIX wikibase: <http://wikiba.se/ontology#> \n"+
			"PREFIX p: <http://www.wikidata.org/prop/> \n"+
			"PREFIX ps: <http://www.wikidata.org/prop/statement/> \n"+
			"PREFIX pq: <http://www.wikidata.org/prop/qualifier/> \n"+
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
			"PREFIX bd: <http://www.bigdata.com/rdf#> \n"+
			
			" SELECT  ?suggested_entrezid ?suggested_geneLabel WHERE {\n "+
			" ?gene wdt:P2293 ?disease .\n"+
			" ?gene wdt:P351 \""  + inputID  + "\" .\n"+
			" ?suggested_gene wdt:P2293 ?disease ;\n"+
			" wdt:P351 ?suggested_entrezid .\n"+
            " SERVICE wikibase:label { bd:serviceParam wikibase:language 'en' }\n"+
			"}";

		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpoint, query);
		ResultSet resultSet = queryExecution.execSelect();
		
		WikidataResultsHandler interactionResultsHandler = new WikidataResultsHandler();
		
		String type = "Association";
		while (resultSet.hasNext()) {
			QuerySolution solution = resultSet.next();

			String targetURI = solution.get("suggested_entrezid").toString();

			String targetLabel = solution.get("suggested_geneLabel").asLiteral().getLexicalForm();
			SourceInteraction sourceInteraction = new SourceInteraction(inputID,inputLabel,type);
			TargetInteraction targetInteraction = new TargetInteraction(targetURI,targetLabel,type);
			
			InteractionBinaryResults interactionBinaryResults = new InteractionBinaryResults(
					inputID,"Association","NA",inputID+targetURI,pelt.getGraphId());			
			
			interactionResultsHandler.add(interactionBinaryResults, sourceInteraction, targetInteraction);
		}
		spokes = interactionResultsHandler.getBinaryResults();
		Pathway result = PathwayBuilder.radialLayout(pelt, spokes);
		return result;
	}
}
