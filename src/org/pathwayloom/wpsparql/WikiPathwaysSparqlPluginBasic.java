package org.pathwayloom.wpsparql;

import java.util.List;

import org.pathvisio.core.data.GdbManager;
import org.pathvisio.core.model.PathwayElement;
import org.pathwayloom.PathwayBuilder;
import org.pathwayloom.utils.InteractionBinaryResults;
import org.pathwayloom.utils.SourceInteraction;
import org.pathwayloom.utils.TargetInteraction;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;



public class WikiPathwaysSparqlPluginBasic extends WikiPathwaysSparqlPlugin {
	static final String queryNodeID = "get_all_known_interactions.sparql";
	
	public WikiPathwaysSparqlPluginBasic(GdbManager gdbManager){
		super(gdbManager);
		this.sparqlQueryNode = map.get(queryNodeID);
	}
	
	@Override 
	public PathwayBuilder doSuggestion(PathwayElement input) throws SuggestionException  {
		this.dataSource = input.getDataSource();
		this.inputID = input.getElementID();
		this.inputLabel = input.getTextLabel();
		
		PathwayElement pelt = createPathwayElement(input);
		String sparqlInput = "";
		
		sparqlInput = Predicate_BY_DataSourceSymtemCode.get(dataSource.getSystemCode());		
		if (sparqlInput==null){
			sparqlInput = convertPredicate(input.getDataNodeType());
		}
		pelt.setDataSource(dataSource);
		pelt.setElementID(inputID);
		
		sparqlInput = sparqlInput +" <"+ dataSource.getIdentifiersOrgUri(inputID) +">";

		String endpoint = sparqlQueryNode.getEndpoint().trim();
		String sparqlQuery = sparqlQueryNode.getQuery();	
		sparqlQuery = sparqlQuery.replaceAll("\\+sparqlInput\\+", sparqlInput);
		
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpoint, query);
		ResultSet resultSet = queryExecution.execSelect();
		
		WikiPathwaysResultHandler interactionResultsHandler = new WikiPathwaysResultHandler();

		while (resultSet.hasNext()) {
			QuerySolution solution = resultSet.next();
			String pathway = solution.get("pathway").toString();
			pathway = pathway.substring(pathway.lastIndexOf("/")+1);

			String interactionID = solution.get("interaction").toString();
			String inputURI = solution.get("participants").toString();
			String type = solution.get("typeInt").toString();
			type = type.substring(type.lastIndexOf("#")+1);

			String sourceURI = solution.get("source").toString();
			String targetURI = solution.get("target").toString();
			String sourceLabel = solution.get("labelS").asLiteral().getLexicalForm();
			String targetLabel = solution.get("labelT").asLiteral().getLexicalForm();
			String targetType = solution.get("typeT").toString();				
			String sourceType = solution.get("typeS").toString();
			SourceInteraction sourceInteraction = new SourceInteraction(sourceURI,sourceLabel,sourceType);
			TargetInteraction targetInteraction = new TargetInteraction(targetURI,targetLabel,targetType);
			
			InteractionBinaryResults interactionBinaryResults = new InteractionBinaryResults(
					inputURI,type,pathway,interactionID,pelt.getGraphId());			
			
			interactionResultsHandler.add(interactionBinaryResults, sourceInteraction, targetInteraction);
		}
		List<PathwayElement> spokes = interactionResultsHandler.getBinaryResults();
		PathwayBuilder result = PathwayBuilder.radialLayout(pelt, spokes);
		return result;
		
	}
}
