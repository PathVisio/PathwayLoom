package org.pathwayloom.chembl;

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

public class ChEMBLProteinSparqlPlugin extends SuggestionAdapter {
	private GdbManager gdbManager;
	protected DataSource dataSource;
	protected String inputID;
	protected String inputLabel;
	
	public ChEMBLProteinSparqlPlugin(GdbManager gdbManager)
	{
		this.gdbManager = gdbManager;
	}
	
	@Override public PathwayBuilder doSuggestion(PathwayElement input) throws SuggestionException  {

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
		
		String endpoint = "https://www.ebi.ac.uk/rdf/services/chembl/sparql";		
		String sparqlQuery = 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
			"PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"+
			"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"+
			"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"+
			"PREFIX dcterms: <http://purl.org/dc/terms/>\n"+
			"PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"+
			"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n"+
			"PREFIX cco: <http://rdf.ebi.ac.uk/terms/chembl#>\n"+
	
			"PREFIX chembl_molecule: <http://rdf.ebi.ac.uk/resource/chembl/molecule/>\n"+
			"PREFIX uniprot: <http://purl.uniprot.org/uniprot/>\n"+
			
			"SELECT DISTINCT ?compound ?actType ?actValue ?actUnits ?label "+
			"WHERE {\n"+
			"  BIND (uniprot:"+inputID+" AS ?uniprot)\n"+
			"  { ?activity a cco:Activity ;\n"+
			"            cco:hasMolecule ?compound ;\n"+
			"            cco:hasAssay ?assay ;\n"+
			"            cco:standardType ?actType ;\n"+
			"            cco:standardValue ?actValue ;\n"+
			"            cco:standardUnits ?actUnits . "+
			"    ?compound rdfs:label ?label. \n "+ 
			"}"+

			"  MINUS {\n"+
			"    ?activity cco:standardUnits '%' .\n"+
			"  }\n"+
			"  ?assay cco:hasTarget ?target ;\n"+
			"         cco:assayType 'Binding' .\n"+
			"  ?target cco:hasTargetComponent ?targetcmpt .\n"+
			"  ?targetcmpt cco:targetCmptXref ?uniprot .\n"+
			"  ?uniprot a cco:UniprotRef\n"+
			"} ORDER BY ASC(?actValue)\n"+
			"  LIMIT 30\n";

		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpoint, query);
		ResultSet resultSet = queryExecution.execSelect();
		
		ChEMBLProteinResultsHandler interactionResultsHandler = new ChEMBLProteinResultsHandler();
		
		String type = "Metabolite";
		while (resultSet.hasNext()) {
			QuerySolution solution = resultSet.next();

			String targetURI = solution.get("compound").toString();
			String targetLabel = solution.get("label").asLiteral().getLexicalForm();
			
			String actType = solution.get("actType").asLiteral().getLexicalForm();
			String actValue = solution.get("actValue").asLiteral().getLexicalForm();

			SourceInteraction sourceInteraction = new SourceInteraction(inputID,inputLabel,type);
			TargetInteraction targetInteraction = new TargetInteraction(targetURI,targetLabel,type);
			
			InteractionBinaryResults interactionBinaryResults = new InteractionBinaryResults(
					inputID,"protein -> compounds",actType+"\t"+actValue,inputID+targetURI,pelt.getGraphId());			
			
			interactionResultsHandler.add(interactionBinaryResults, sourceInteraction, targetInteraction);
		}
		spokes = interactionResultsHandler.getBinaryResults();
		PathwayBuilder result = PathwayBuilder.radialLayout(pelt, spokes);
		return result;
	}
}
