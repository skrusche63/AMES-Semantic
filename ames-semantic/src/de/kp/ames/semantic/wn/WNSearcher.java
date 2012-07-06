package de.kp.ames.semantic.wn;

import org.apache.solr.client.solrj.SolrQuery;
import de.kp.ames.semantic.solr.SolrProxy;

public class WNSearcher {

	/*
	 * Reference to SolrProxy
	 */
	private SolrProxy solrProxy;

	public WNSearcher() {
		solrProxy = SolrProxy.getInstance();
	}

	public String suggest(String prefix, String pos, String start, String limit) throws Exception {

		/*
		 * Retrieve terms
		 */
		SolrQuery query = new SolrQuery();
		return null;
		
	}	


}
