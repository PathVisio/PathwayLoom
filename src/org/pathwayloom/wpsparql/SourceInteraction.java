package org.pathwayloom.wpsparql;

public class SourceInteraction {
	String sourceURI;	
	String sourceLabel;
	String sourceType;
	
	public SourceInteraction(String sourceURI, String sourceLabel,
			String sourceType) {
		super();
		this.sourceURI = sourceURI;
		this.sourceLabel = sourceLabel;
		this.sourceType = sourceType;
	}
}
