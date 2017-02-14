package org.pathwayloom.utils;

import java.util.List;

import org.pathvisio.core.model.PathwayElement;

public interface ResultHandler {
	
	public void add(InteractionBinaryResults i, SourceInteraction s, TargetInteraction t);
	
	public List<PathwayElement> getBinaryResults();
	
	public void clear();
	
}
