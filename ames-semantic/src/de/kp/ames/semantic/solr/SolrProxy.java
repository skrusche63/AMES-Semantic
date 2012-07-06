package de.kp.ames.semantic.solr;
/**
 * Copyright 2012 Dr. Krusche & Partner PartG. All rights reserved
 *
 * This file is part of the AMES-Semantic Project.
 *   
 */

import java.net.MalformedURLException;
import java.util.Collection;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import de.kp.ames.semantic.Bundle;

public class SolrProxy {
	
	private static Bundle bundle = Bundle.getInstance();
	private static SolrProxy instance = new SolrProxy();
	
	private SolrServer server;

	/**
	 * Constructor
	 */
	private SolrProxy() {

		try {
	
			String endpoint = bundle.getString("ames.solr.endpoint");
			server = new CommonsHttpSolrServer(endpoint);
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
			
		}

	}
	
	/**
	 * @return
	 */
	public static SolrProxy getInstance() {

		if (instance == null) instance = new SolrProxy();
		return instance;
	
	}
	
	/**
	 * @param documents
	 * @return
	 */
	public boolean createEntries(Collection<SolrInputDocument> documents) {

		boolean indexed = false;

		try {

			server.add(documents);
			server.commit();
			
			indexed = true;
			
		} catch (Exception e) {
			e.printStackTrace();
			
		} finally {}
		
		return indexed;

	}
	
	/**
	 * @return
	 */
	public boolean delete() {

		boolean deleted = false;

		try {

			server.deleteByQuery( "*:*" );
			deleted = true;
		
		} catch (Exception e) {
			e.printStackTrace();
		
		} finally {}
		
		return deleted;
		
	}

	/**
	 * Retrieve query response from Apache Solr
	 * 
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public QueryResponse executeQuery(SolrQuery query) throws Exception {
		return server.query(query);
	}

}
