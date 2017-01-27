package org.pathwayloom.wpsparql;


import java.util.ArrayList;
import java.util.List;

import org.bridgedb.DataSource;
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



public class WikiPathwaysSparqlPluginAdvanced extends WikiPathwaysSparqlPlugin {

	public WikiPathwaysSparqlPluginAdvanced(GdbManager gdbManager){
		super(gdbManager);
	}
	
	@Override public PathwayBuilder doSuggestion(PathwayElement input) throws SuggestionException{		
		dataSource = input.getDataSource();
		inputID = input.getElementID();
		
		PathwayElement pelt = PathwayElement.createPathwayElement(ObjectType.DATANODE);
		pelt.setMWidth (PppPlugin.DATANODE_MWIDTH);
		pelt.setMHeight (PppPlugin.DATANODE_MHEIGHT);
		pelt.setTextLabel(input.getTextLabel());
		pelt.setDataSource(dataSource);
		pelt.setElementID(inputID);
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

		sparqlInput = sparqlInput+" <"+dataSource.getIdentifiersOrgUri(inputID)+">";
		
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
