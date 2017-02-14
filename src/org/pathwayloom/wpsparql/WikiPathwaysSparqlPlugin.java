package org.pathwayloom.wpsparql;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bridgedb.DataSource;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.pathvisio.core.data.GdbManager;
import org.pathwayloom.SuggestionAdapter;

public abstract class WikiPathwaysSparqlPlugin extends SuggestionAdapter {
	
//	protected GdbManager gdbManager;
//	protected DataSource dataSource;
//	protected String inputID;
	public WikiPathwaysSparqlPlugin(GdbManager gdbManager)
	{
		this.gdbManager = gdbManager;
	}
	public static final Map<String, String> Predicate_BY_DataSourceSymtemCode = new HashMap<String, String>();
	static
	{
		Predicate_BY_DataSourceSymtemCode.put("En","wp:bdbEnsembl");
		Predicate_BY_DataSourceSymtemCode.put("L","wp:bdbEntrezGene");
		Predicate_BY_DataSourceSymtemCode.put("H","wp:bdbHgncSymbol");
		
		Predicate_BY_DataSourceSymtemCode.put("Ce","wp:bdbChEBI");
		Predicate_BY_DataSourceSymtemCode.put("Cs","wp:bdbChemspider");
		Predicate_BY_DataSourceSymtemCode.put("Ch","wp:bdbHmdb");
		Predicate_BY_DataSourceSymtemCode.put("Cpc","wp:bdbPubChem");
		
		Predicate_BY_DataSourceSymtemCode.put("S","wp:bdbUniprot");
	}
	
	public String convertPredicate( String dataNodeType){
		Xref xref = new Xref(inputID, dataSource);
		Xref ref = null;
		String sparqlInput = null;
		if (dataNodeType.equals("Metabolite")){
			try {
				Set<Xref> setRef = gdbManager.getMetaboliteDb().mapID(xref,
					DataSource.getExistingBySystemCode("Ch"));
				if (!setRef.isEmpty())
					ref = setRef.iterator().next();
				sparqlInput = "wp:bdbHmdb";
			} catch (IDMapperException e) {
				e.printStackTrace();
			}				
		}
		if (dataNodeType.equals("GeneProduct")){
			try {
				Set<Xref> setRef = gdbManager.getMetaboliteDb().mapID(xref,
					DataSource.getExistingBySystemCode("En"));
				if (!setRef.isEmpty())
					ref = setRef.iterator().next();
				sparqlInput = "wp:bdbEnsembl";
			} catch (IDMapperException e) {
				e.printStackTrace();
			}
		}
		inputID = ref.getId();
		inputID = inputID.substring(inputID.lastIndexOf(":")+1);
		dataSource = ref.getDataSource();
		return sparqlInput;		
	}	
}
