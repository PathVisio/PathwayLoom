package org.pathwayloom.wpsparql;

public class TargetInteraction {
	String targetURI;	
	String targetLabel;
	String targetType;
	
	public TargetInteraction(String targetURI, String targetLabel,
			String targetType) {
		super();
		this.targetURI = targetURI;
		this.targetLabel = targetLabel;
		this.targetType = targetType;
	}
}
