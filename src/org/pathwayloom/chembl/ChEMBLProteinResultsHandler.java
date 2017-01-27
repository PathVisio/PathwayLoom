package org.pathwayloom.chembl;

import java.util.ArrayList;
import java.util.List;

import org.bridgedb.DataSource;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.PathwayElement;
import org.pathwayloom.PppPlugin;
import org.pathwayloom.utils.AbstractResultHandler;
import org.pathwayloom.utils.InteractionBinaryResults;
import org.pathwayloom.utils.SourceInteraction;
import org.pathwayloom.utils.TargetInteraction;

public class ChEMBLProteinResultsHandler extends AbstractResultHandler {

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
				String targetURI = targetInteraction.targetURI.
						substring(targetInteraction.targetURI.lastIndexOf("/")+1);
				String sourceLabel = sourceInteraction.sourceLabel;
				String targetLabel = targetInteraction.targetLabel;

				pchildElt.addComment(sourceLabel, "Source");
				pchildElt.addComment(targetLabel, "Target");
				
				if ( !sourceURI.equals(interaction.inputURI.toString())){
					pchildElt.setTextLabel(sourceLabel);
					pchildElt.setDataSource(DataSource.getExistingBySystemCode("Cl"));
					pchildElt.setElementID(sourceURI);
					pchildElt.setDataNodeType(sourceInteraction.sourceType);
					pchildElt.addComment("Source", "Output");		
				}
				else if ( !targetURI.equals(interaction.inputURI.toString())){
					pchildElt.setTextLabel(targetLabel);
					pchildElt.setDataSource(DataSource.getExistingBySystemCode("Cl"));
					pchildElt.setElementID(targetURI);
					pchildElt.setDataNodeType(targetInteraction.targetType);	
					pchildElt.addComment("Target", "Output");		
				}
				else {//TODO
					System.out.println("DO IT");
				}

				String pathway = interaction.pathwayID;
				String[] pathwaySplit = pathway.split("\t");
				pchildElt.addComment("actType: "+pathwaySplit[0]+"; actValue: "+pathwaySplit[1], "WikiPathway");
				pchildElt.setMWidth (targetLabel.length()*6);
				pchildElt.setMHeight (PppPlugin.DATANODE_MHEIGHT);
				pchildElt.addComment(interaction.getParentGraphId(), "ParentGraphId");
				pchildElt.addComment("False", "Input");			
				spokes.add (pchildElt);
			}
		}
		return spokes;
	}
}