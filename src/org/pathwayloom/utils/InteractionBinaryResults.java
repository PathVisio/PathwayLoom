package org.pathwayloom.utils;

import java.util.HashSet;
import java.util.Set;

public class InteractionBinaryResults  {

	public String inputURI;	
	public String typeInt;	
	public String pathwayID;
	public String interactionID;	
	public String parentGraphId;
	
	Set<SourceInteraction> setSource = new HashSet<SourceInteraction>();
	Set<TargetInteraction> setTarget = new HashSet<TargetInteraction>();

	public InteractionBinaryResults(String inputURI, String typeInt,
			String pathwayID, String interactionID, String parentGraphId) {
		this.inputURI = inputURI;
		this.typeInt = typeInt;
		this.pathwayID = pathwayID;
		this.interactionID = interactionID;
		this.parentGraphId = parentGraphId;
	}


	@Override public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof InteractionBinaryResults)) return false;

		InteractionBinaryResults other = (InteractionBinaryResults)obj;

		return this.interactionID.equals(other.interactionID) ;
	}

	@Override public int hashCode() {
		return interactionID.hashCode();
	}

	public String getInputURI() {
		return inputURI;
	}

	public void setInputURI(String inputURI) {
		this.inputURI = inputURI;
	}

	public String getTypeInt() {
		return typeInt;
	}

	public String getPathwayID() {
		return pathwayID;
	}

	public String getInteractionID() {
		return interactionID;
	}

	public String getParentGraphId() {
		return parentGraphId;
	}

	public Set<SourceInteraction> getSetSource() {
		return setSource;
	}

	public Set<TargetInteraction> getSetTarget() {
		return setTarget;
	}
}
