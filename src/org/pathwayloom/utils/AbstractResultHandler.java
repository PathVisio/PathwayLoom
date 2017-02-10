package org.pathwayloom.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pathvisio.core.model.PathwayElement;

public class AbstractResultHandler implements ResultHandler {
	protected Map<InteractionBinaryResults,InteractionBinaryResults> setResults;

	public AbstractResultHandler(){
		this.setResults = new HashMap<InteractionBinaryResults,InteractionBinaryResults>();
	}

	public void add(InteractionBinaryResults i, SourceInteraction s, TargetInteraction t){		
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

	@Override
	public List<PathwayElement> getBinaryResults() {
		return null;
	}
}
