package de.kp.ames.semantic.solr;
/**
 * Copyright 2012 Dr. Krusche & Partner PartG. All rights reserved
 *
 * This file is part of the AMES-Semantic Project.
 *   
 */

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import de.kp.ames.semantic.Bundle;

public class SolrProxy {
	
	private static Bundle bundle = Bundle.getInstance();
	private static SolrProxy instance = new SolrProxy();
	
	private SolrServer server;

	private SolrProxy() {

		try {
	
			String endpoint = bundle.getString("ames.solr.endpoint");
			server = new CommonsHttpSolrServer(endpoint);
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
			
		}

	}
	
	public static SolrProxy getInstance() {

		if (instance == null) instance = new SolrProxy();
		return instance;
	
	}

	/************************************************************************
	 * 
	 * INDEX     INDEX     INDEX     INDEX     INDEX     INDEX     INDEX
	 * 
	 ***********************************************************************/
	
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

	/*
	 * this method requests the lexical part of the solr search index and
	 * retrieves all the identifiers that refer to the respective term
	 *
	 * this method supports the 2-step semantic strategy exclusively 
	 * for WordNet-3.0; all other term concept maps assigned to the
	 * actual search index are completely extracted from the index
	 */
	public ArrayList<String> getWNKeys(String source, String term) {
		
		ArrayList<String>synsets = new ArrayList<String>();

		try {

			// the source provided is used as a filter for
			// this lexical search request
			String fp = "+" + SolrConstants.SOURCE_FIELD + ":\"" + source + "\"";
			
			SolrQuery solrQuery = new SolrQuery();
			solrQuery.addFilterQuery(fp);		

			solrQuery.setQuery(term);
			
			QueryResponse response = server.query(solrQuery);
			SolrDocumentList docs = response.getResults();
			
			Iterator<SolrDocument> iter = docs.iterator();
			while (iter.hasNext()) {
				
				SolrDocument doc = iter.next();
				String id  = (String)doc.getFieldValue("id");

				synsets.add(id);

			}
			
		} catch (SolrServerException e) {
			e.printStackTrace();
			
		
		} finally {}
		
		return synsets;
		
	}
	
}
