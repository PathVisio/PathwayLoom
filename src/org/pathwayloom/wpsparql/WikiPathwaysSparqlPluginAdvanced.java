package org.pathwayloom.wpsparql;

import java.util.ArrayList;
import java.util.List;

import org.bridgedb.DataSource;
import org.pathvisio.core.data.GdbManager;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.PathwayElement;
import org.pathwayloom.PathwayBuilder;
import org.pathwayloom.PppPlugin;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;



public class WikiPathwaysSparqlPluginAdvanced extends WikiPathwaysSparqlPlugin {
	static final String queryNodeID = "get_all_known_interactions.sparql";

	public WikiPathwaysSparqlPluginAdvanced(GdbManager gdbManager){
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
		
		List<PathwayElement> spokes = new ArrayList<PathwayElement>();
		
		while (resultSet.hasNext()) {
			QuerySolution solution = resultSet.next();
			PathwayElement pchildElt = PathwayElement.createPathwayElement(ObjectType.DATANODE);
	
			String type = solution.get("typeInt").toString();
			type = type.substring(type.lastIndexOf("#")+1);
			pchildElt.addComment(type, "Interaction Type");
			
			String sourceURI = solution.get("source").toString();
			String targetURI = solution.get("target").toString();
			String sourceLabel = solution.get("labelS").asLiteral().getLexicalForm();
			String targetLabel = solution.get("labelT").asLiteral().getLexicalForm();
			
			pchildElt.addComment(sourceLabel, "Source");
			pchildElt.addComment(targetLabel, "Target");

			
			if ( !sourceURI.equals(solution.get("participants").toString())){
				pchildElt.setTextLabel(sourceLabel);
				String xref = sourceURI;
				String dsResult = xref.substring(0, xref.lastIndexOf("/"));

				String id = xref.substring(xref.lastIndexOf("/")+1);
				pchildElt.setDataSource(DataSource.getByIdentiferOrgBase(dsResult));
				pchildElt.setElementID(id);
				String dnType = solution.get("typeS").toString();
				dnType = dnType.substring(dnType.lastIndexOf("#")+1);
				pchildElt.setDataNodeType(dnType); 
			}
			else{
				pchildElt.setTextLabel(targetLabel);
				String xref = targetURI;
				String dsResult = xref.substring(0, xref.lastIndexOf("/"));

				String id = xref.substring(xref.lastIndexOf("/")+1);
				pchildElt.setDataSource(DataSource.getByIdentiferOrgBase(dsResult));
				pchildElt.setElementID(id);
				String dnType = solution.get("typeT").toString();
				dnType = dnType.substring(dnType.lastIndexOf("#")+1);
				pchildElt.setDataNodeType(dnType);				
			}

			String pathway = solution.get("pathway").toString();
			pathway = pathway.substring(pathway.lastIndexOf("/")+1);
			pchildElt.addComment(pathway, "WikiPathway");

			pchildElt.setMWidth (PppPlugin.DATANODE_MWIDTH);
			pchildElt.setMHeight (PppPlugin.DATANODE_MHEIGHT);
			pchildElt.addComment(pelt.getGraphId(), "ParentGraphId");
			pchildElt.addComment("False", "Input");
			spokes.add (pchildElt);
		}
		PathwayBuilder result = PathwayBuilder.radialLayout(pelt, spokes);
		return result;
	}
}
