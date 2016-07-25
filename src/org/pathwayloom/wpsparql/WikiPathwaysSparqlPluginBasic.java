package org.pathwayloom.wpsparql;

import java.util.ArrayList;
import java.util.List;

import org.pathvisio.core.data.GdbManager;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathwayloom.PathwayBuilder;
import org.pathwayloom.PppPlugin;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;



public class WikiPathwaysSparqlPluginBasic extends WikiPathwaysSparqlPlugin {
	
	
	public WikiPathwaysSparqlPluginBasic(GdbManager gdbManager)
	{
		super(gdbManager);
	}
	@Override public Pathway doSuggestion(PathwayElement input) throws SuggestionException  {

		dataSource = input.getDataSource();
		inputID = input.getElementID();
		
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
		
		String sparqlInput = "";
		
		sparqlInput = Predicate_BY_DataSourceSymtemCode.get(dataSource.getSystemCode());
		
		if (sparqlInput==null){
			sparqlInput = convertPredicate(input.getDataNodeType());
		}
		pelt.setDataSource(dataSource);
		pelt.setElementID(inputID);
		
		sparqlInput = sparqlInput +" <"+ dataSource.getIdentifiersOrgUri(inputID) +">";	
		
		String endpoint = "http://sparql.wikipathways.org/";		
		String sparqlQuery = 
			"prefix gpml:    <http://vocabularies.wikipathways.org/gpml#>\n"+
			"prefix dcterms: <http://purl.org/dc/terms/>\n"+
			"prefix dc:      <http://purl.org/dc/elements/1.1/>\n"+
			"prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"+
			"prefix wp:      <http://vocabularies.wikipathways.org/wp#> \n"+
			"prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> \n"+
			
			" SELECT DISTINCT ?pathway ?interaction ?typeInt  ?participants ?source ?target "
			+ "?typeS ?typeT (str(?SourceLabel)as?labelS) (str(?TargetLabel)as?labelT)  ?DataNodeLabel  WHERE {\n"+
			" ?pathway a wp:Pathway .\n"+
			" ?interaction dcterms:isPartOf ?pathway . \n"+
			" ?interaction a wp:Interaction .\n"+
			" ?interaction a ?typeInt .\n"+
			" FILTER (!regex(str(?typeInt), 'http://vocabularies.wikipathways.org/wp#Interaction','i')) .\n"+
			" ?interaction wp:participants ?participants .\n"+

			" ?participants "+sparqlInput+" .\n"+

			" ?interaction wp:source ?source.\n"+
			" ?interaction wp:target ?target.\n"+
			" ?source rdfs:label ?SourceLabel . \n"+
			" ?source a ?typeS . \n"+
			" FILTER (!regex(str(?typeS), 'http://vocabularies.wikipathways.org/wp#DataNode','i')) .\n"+
			" ?target rdfs:label ?TargetLabel .  \n"+ 
			" ?target a ?typeT . \n"+
			" FILTER (!regex(str(?typeT), 'http://vocabularies.wikipathways.org/wp#DataNode','i')) .\n"+
			"}";

		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpoint, query);
		ResultSet resultSet = queryExecution.execSelect();
		
		InteractionResultsHandler interactionResultsHandler = new InteractionResultsHandler();

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
		spokes = interactionResultsHandler.getBinaryResults();
		Pathway result = PathwayBuilder.radialLayout(pelt, spokes);
		return result;
	}
}
