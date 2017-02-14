// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License"); 
// you may not use this file except in compliance with the License. 
// You may obtain a copy of the License at 
// 
// http://www.apache.org/licenses/LICENSE-2.0 
//  
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, 
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and 
// limitations under the License.
//
package org.pathwayloom;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import org.pathwayloom.Suggestion.SuggestionException;
import org.pathwayloom.uniprot.UniprotResultsHandler;
import org.pathwayloom.utils.InteractionBinaryResults;
import org.pathwayloom.utils.ResultHandler;
import org.pathwayloom.utils.SourceInteraction;
import org.pathwayloom.utils.SparqlQuery;
import org.pathwayloom.utils.SparqlQueryParser;
import org.pathwayloom.utils.TargetInteraction;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

/**
 * Default implementation of Suggestion interface
 */
public class SuggestionAdapter implements Suggestion 
{
	protected GdbManager gdbManager;
	protected DataSource dataSource;
	
	protected String inputID;
	protected String inputLabel;
	
	protected SparqlQuery sparqlQueryNode;	
	protected Map<String, SparqlQuery> map = SparqlQueryParser.getQueries();
	
	protected String taxon;
	
	protected String idParameter;
	protected String labelParameter;
	protected String typeInteraction;
	protected String typeDataNode;
	protected String systemCode;
	protected String menu_label;
	protected ResultHandler interactionResultsHandler;
	
	public boolean canSuggest(PathwayElement input) 
	{
		return true;
	}
	@Override 
	public PathwayBuilder doSuggestion(PathwayElement input) throws SuggestionException  {

		this.dataSource = input.getDataSource();
		this.inputID = input.getElementID();
		this.inputLabel = input.getTextLabel();
		this.interactionResultsHandler.clear();
		
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
//	public PathwayBuilder doSuggestion(PathwayElement input)
//			throws SuggestionException 
//	{
//		PathwayElement hub = input.copy();
//		
//		PathwayBuilder result = PathwayBuilder.radialLayout(hub, new ArrayList<PathwayElement>());
//		return result;
//	}
	
	public void mapping(String systemCode){
		Xref xref = new Xref(inputID, dataSource);
		try {
			if ( !dataSource.getSystemCode().equals(systemCode) ){
				Set<Xref> setRef  = gdbManager.getGeneDb().
						mapID(xref,DataSource.getExistingBySystemCode(systemCode));
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
	}
	
	public void updateTaxon(PathwayElement input){
		Organism species = Organism.fromLatinName(input.getParent().getMappInfo().getOrganism());
		
		if (species!=null){
			this.taxon = species.taxonomyID().getId();
		}
		else{
			this.taxon = null;
		}
		
	}
	
	public void getOrganismError(){
		JOptionPane.showMessageDialog(null,
				"Please define the pathway organism","Organism Error", JOptionPane.ERROR_MESSAGE);
	}
	
	public PathwayElement createPathwayElement(PathwayElement input){

		PathwayElement pelt = PathwayElement.createPathwayElement(ObjectType.DATANODE);
		pelt.setMWidth (PppPlugin.DATANODE_MWIDTH);
		pelt.setMHeight (PppPlugin.DATANODE_MHEIGHT);
		pelt.setTextLabel(input.getTextLabel());
		
		pelt.setCopyright("Copyright notice");
		pelt.setDataNodeType(input.getDataNodeType());
		pelt.setGraphId(input.getGraphId());
		pelt.addComment(pelt.getGraphId(), "ParentGraphId");
		pelt.addComment("True", "Input");		

		pelt.setDataSource(input.getDataSource());
		pelt.setElementID(input.getElementID());
		return pelt;
	}
	
	public List<PathwayElement> doQuery(String graphId){
		String endpoint = sparqlQueryNode.getEndpoint().trim();
		String sparqlQuery = sparqlQueryNode.getQuery();

		sparqlQuery = sparqlQuery.replaceAll("\\+inputID\\+", this.inputID);
		sparqlQuery = sparqlQuery.replaceAll("\\+taxon\\+", this.taxon);
		
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpoint, query);
		ResultSet resultSet = queryExecution.execSelect();
		
		while (resultSet.hasNext()) {
			QuerySolution solution = resultSet.next();

			String targetURI = solution.get(idParameter).toString();
			String targetLabel = solution.get(labelParameter).asLiteral().getLexicalForm();
			SourceInteraction sourceInteraction = new SourceInteraction(inputID,inputLabel,typeDataNode);
			TargetInteraction targetInteraction = new TargetInteraction(targetURI,targetLabel,typeDataNode);
			
			InteractionBinaryResults interactionBinaryResults = new InteractionBinaryResults(
					inputID,typeInteraction,"",inputID+targetURI,graphId);			
			
			interactionResultsHandler.add(interactionBinaryResults, sourceInteraction, targetInteraction);
		}
		List<PathwayElement> spokes = interactionResultsHandler.getBinaryResults();
		return spokes;
	}
	public String getMenu_label() {
		return menu_label;
	}
	
}
