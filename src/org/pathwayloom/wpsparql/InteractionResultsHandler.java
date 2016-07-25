package org.pathwayloom.wpsparql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bridgedb.DataSource;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.PathwayElement;
import org.pathwayloom.PppPlugin;

public class InteractionResultsHandler {

	Map<InteractionBinaryResults,InteractionBinaryResults> setResults;

	public InteractionResultsHandler(){
		this.setResults = new HashMap<InteractionBinaryResults,InteractionBinaryResults>();
	}

	public void add(InteractionBinaryResults i, SourceInteraction s, TargetInteraction t ){		
		if (setResults.get(i)==null){
			setResults.put(i,i);
			setResults.get(i).getSetSource().add(s);
			setResults.get(i).getSetTarget().add(t);
		}
		else{
			setResults.get(i).getSetSource().add(s);
			setResults.get(i).getSetTarget().add(t);		
		}
	}

	public List<PathwayElement> getBinaryResults(){
		List<PathwayElement> spokes = new ArrayList<PathwayElement>();

		for ( InteractionBinaryResults interaction : setResults.keySet()){
			if ( interaction.getSetSource().size()==1 && interaction.getSetTarget().size()==1){
				PathwayElement pchildElt = PathwayElement.createPathwayElement(ObjectType.DATANODE);

				String type = interaction.getTypeInt();
				type = type.substring(type.lastIndexOf("#")+1);
				pchildElt.addComment(type, "Interaction Type");

				SourceInteraction sourceInteraction = interaction.getSetSource().iterator().next();
				TargetInteraction targetInteraction = interaction.getSetTarget().iterator().next();

				String sourceURI = sourceInteraction.sourceURI;
				String targetURI = targetInteraction.targetURI;
				String sourceLabel = sourceInteraction.sourceLabel;
				String targetLabel = targetInteraction.targetLabel;

				pchildElt.addComment(sourceLabel, "Source");
				pchildElt.addComment(targetLabel, "Target");

				if ( !sourceURI.equals(interaction.inputURI.toString())){
					pchildElt.setTextLabel(sourceLabel);
					String xref = sourceURI;
					String dsResult = xref.substring(0, xref.lastIndexOf("/"));

					String id = xref.substring(xref.lastIndexOf("/")+1);
					pchildElt.setDataSource(DataSource.getByIdentiferOrgBase(dsResult));
					pchildElt.setElementID(id);
					String dnType = sourceInteraction.sourceType;
					dnType = dnType.substring(dnType.lastIndexOf("#")+1);
					pchildElt.setDataNodeType(dnType);
					pchildElt.addComment("Source", "Output");		
				}
				else if ( !targetURI.equals(interaction.inputURI.toString())){
					pchildElt.setTextLabel(targetLabel);
					String xref = targetURI;
					String dsResult = xref.substring(0, xref.lastIndexOf("/"));

					String id = xref.substring(xref.lastIndexOf("/")+1);
					pchildElt.setDataSource(DataSource.getByIdentiferOrgBase(dsResult));
					pchildElt.setElementID(id);
					String dnType = targetInteraction.targetType;
					dnType = dnType.substring(dnType.lastIndexOf("#")+1);
					pchildElt.setDataNodeType(dnType);	
					pchildElt.addComment("Target", "Output");		
				}
				else {//TODO
					System.out.println("DO IT");
				}

				String pathway = interaction.pathwayID;
				pathway = pathway.substring(pathway.lastIndexOf("/")+1);
				pchildElt.addComment(pathway, "WikiPathway");

				pchildElt.setMWidth (PppPlugin.DATANODE_MWIDTH);
				pchildElt.setMHeight (PppPlugin.DATANODE_MHEIGHT);
				pchildElt.addComment(interaction.getParentGraphId(), "ParentGraphId");
				pchildElt.addComment("False", "Input");			
				spokes.add (pchildElt);
			}
		}
		return spokes;
	}
}
