package org.pathwayloom;

import java.awt.Color;
import java.util.List;

import org.pathvisio.core.model.DataNodeType;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;

public class PathwayBuilder 
{
    private static final double HUB_X = 270;
    private static final double HUB_Y = 270;
    private static final double RADIUS = 170;	
    
    private PathwayElement input;
    private Pathway result;
    
//    public PathwayBuilder(){
//    	
//    }
	
    /**
     * creates a network with radial layout of a hub surrounded
     * by spokes
     * 
     * The arguments should not be part of a Pathway yet.
     */
	public static PathwayBuilder radialLayout (PathwayElement hub, List<PathwayElement> spokes)
	{
	    Pathway result = new Pathway();	    
	    PathwayBuilder pb = new PathwayBuilder();
	    pb.setInput(hub);
	    
        int noRecords = spokes.size();
        double angle = 0;
		double incrementStep = (2* Math.PI)/noRecords;

		hub.setMCenterX(HUB_X);
	    hub.setMCenterY(HUB_Y);
	    if (hub.getDataNodeType().equals("Metabolite")){
	    	hub.setColor(Color.BLUE);
		}
        result.add (hub);
        hub.setGeneratedGraphId();
        
		for (PathwayElement pchildElt : spokes) 
        {
			String selectedDataNodeType = pchildElt.getDataNodeType();
			if (selectedDataNodeType.equals("Unkown")){
				pchildElt.setDataNodeType(DataNodeType.UNKOWN);
			}
			if (selectedDataNodeType.equals("Metabolite")){
				pchildElt.setDataNodeType(DataNodeType.METABOLITE);
				pchildElt.setColor(Color.BLUE);
			}
			if (selectedDataNodeType.equals("GeneProduct")){
				pchildElt.setDataNodeType(DataNodeType.GENEPRODUCT);
			}
			if (selectedDataNodeType.equals("Protein")){
				pchildElt.setDataNodeType(DataNodeType.PROTEIN);
			}
            PathwayElement connectElement = PathwayElement.createPathwayElement(ObjectType.LINE);
	    	connectElement.setMStartX(HUB_X);
	    	connectElement.setMStartY(HUB_Y);
	    	connectElement.setMEndX(HUB_X + RADIUS * Math.cos(angle));
	    	connectElement.setMEndY(HUB_Y + RADIUS * Math.sin(angle));
		    pchildElt.setMCenterX(HUB_X + RADIUS * Math.cos(angle));
		    pchildElt.setMCenterY(HUB_Y + RADIUS * Math.sin(angle));
		    result.add(pchildElt);
		    result.add(connectElement);
		    pchildElt.setGeneratedGraphId();
		    connectElement.setStartGraphRef(hub.getGraphId());
		    connectElement.setEndGraphRef(pchildElt.getGraphId());
	    	angle += incrementStep;
        }
		
		pb.setResult(result);
		return pb;		
	}

	public PathwayElement getInput() {
		return input;
	}

	public void setInput(PathwayElement input) {
		this.input = input;
	}

	public Pathway getResult() {
		return result;
	}

	public void setResult(Pathway result) {
		this.result = result;
	}
	
}
