package org.pathwayloom.utils;

public class SparqlQuery {
	private String source;
	private String endpoint;
	private String query;
	private String data_types;
	private String interaction_types;
	
	public SparqlQuery(String source, String endpoint, String query,
			String data_types, String interaction_types) {
		super();
		this.source = source;
		this.endpoint = endpoint;
		this.query = query;
		this.data_types = data_types;
		this.interaction_types = interaction_types;
	}
	
	public String getSource() {
		return source;
	}
	public String getEndpoint() {
		return endpoint;
	}
	public String getQuery() {
		return query;
	}
	public String getData_types() {
		return data_types;
	}
	public String getInteraction_types() {
		return interaction_types;
	}

}
