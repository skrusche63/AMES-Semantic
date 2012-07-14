package de.kp.ames.semantic.wn;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.json.JSONArray;
import org.json.JSONObject;

import de.kp.ames.semantic.solr.SolrConstants;
import de.kp.ames.semantic.solr.SolrProxy;
import de.kp.ames.semantic.wn.config.WNConstants;

public class WNSearcher {

	/*
	 * Reference to SolrProxy
	 */
	private SolrProxy solrProxy;

	public WNSearcher() {
		solrProxy = SolrProxy.getInstance();
	}


	public String search(String searchTerm, String start, String limit) throws Exception {

		/*
		 * Build Apache Solr query
		 */
		SolrQuery query = new SolrQuery();

		/*
		 * Paging support from Apache Solr
		 */
		int s = Integer.valueOf(start);
		int r = Integer.valueOf(limit);

		query.setStart(s);
		query.setRows(r);

		// String qs = SolrConstants.WORD_FIELD + ":" + searchTerm;
		String qs = searchTerm; // default field
		query.setQuery(qs);

		/*
		 * set filter for a specific set of indexed documents within Solr by
		 * category "cat" field
		 */
		// TODO: reindex wikipedia content with category first
		// query.setFilterQueries(SolrConstants.CATEGORY_FIELD + ":" +
		// SolrConstants.CATEGORY_RESULT_VALUE);

		query.addHighlightField(SolrConstants.TITLE_FIELD);
		query.setHighlight(true);

		query.setHighlightSimplePre("<span class=\"sg-th\">");
		query.setHighlightSimplePost("</span>");

		QueryResponse response = solrProxy.executeQuery(query);
		SolrDocumentList docs = response.getResults();

		Map<String, Map<String, List<String>>> highlighting = response.getHighlighting();

		/*
		 * Sort search result
		 */
		JSONArray jArray = new JSONArray();

		Iterator<SolrDocument> iter = docs.iterator();
		while (iter.hasNext()) {

			SolrDocument doc = iter.next();

			JSONObject jDoc = new JSONObject();


			/*
			 * Identifier
			 */
			String id = (String) doc.getFieldValue("id");
			jDoc.put("id", id);

			/*
			 * Title
			 */
			String title = (String) doc.getFieldValue(SolrConstants.TITLE_FIELD);
			if (title == null) {
				System.out.println("ID has no title: " + id);
				continue;
			}
			jDoc.put("title", title);

			
			/*
			 * highlighted field
			 */
			String highlightedTerm = null;
			if (highlighting.get(id).containsKey(SolrConstants.TITLE_FIELD)) {
				highlightedTerm = highlighting.get(id).get(SolrConstants.TITLE_FIELD).get(0);
			} else {
				highlightedTerm = title;
			}
	
			/*
			 * result field
			 */
			String value = "<div class=\"sg\"><span class=\"sg-t\">" + highlightedTerm
					+ "</span><p class=\"sg-dg\"><span class=\"sg-dl\">Id:</span><span class=\"sg-d\"> " + id
					+ "</span></p></div>";
			jDoc.put("result", value);

			jArray.put(jDoc);
		}

		/*
		 * Render result
		 */
		return jArray.toString();
	}

	public String suggest(String prefix, String start, String limit) throws Exception {

		/*
		 * Build Apache Solr query
		 */
		SolrQuery query = new SolrQuery();

		/*
		 * Paging support from Apache Solr
		 */
		int s = Integer.valueOf(start);
		int r = Integer.valueOf(limit);

		query.setStart(s);
		query.setRows(r);

		String qs = SolrConstants.WORD_FIELD + ":" + prefix;
		query.setQuery(qs);

		/*
		 * set filter for a specific set of indexed documents within Solr by
		 * category "cat" field
		 */
		query.setFilterQueries(SolrConstants.CATEGORY_FIELD + ":" + SolrConstants.CATEGORY_SUGGEST_VALUE);

		query.addHighlightField(SolrConstants.WORD_FIELD);
		query.setHighlight(true);

		query.setHighlightSimplePre("<span class=\"sg-th\">");
		query.setHighlightSimplePost("</span>");

		QueryResponse response = solrProxy.executeQuery(query);
		SolrDocumentList docs = response.getResults();

		Map<String, Map<String, List<String>>> highlighting = response.getHighlighting();

		/*
		 * Sort search result
		 */
		JSONArray jArray = new JSONArray();

		Iterator<SolrDocument> iter = docs.iterator();
		while (iter.hasNext()) {

			SolrDocument doc = iter.next();

			JSONObject jDoc = new JSONObject();

			/*
			 * Identifier
			 */
			String id = (String) doc.getFieldValue("id");

			String pos = id.substring(0, 1);
			String posLabel = WNConstants.posMap.get(pos).getLabel();

			// change emboss to bold tag
			String highlightedTerm = highlighting.get(id).get(SolrConstants.WORD_FIELD).get(0);

			jDoc.put("term", highlightedTerm);

			/*
			 * Description
			 */
			String desc = (String) doc.getFieldValue(SolrConstants.DESC_FIELD);
			jDoc.put("desc", desc);

			/*
			 * result field
			 */
			String synonyms = (String) doc.getFieldValue(SolrConstants.SYNONYM_FIELD);
			String value = "<div class=\"sg\">" + 
								"<span class=\"sg-t\">" + highlightedTerm + "</span>" + 
								"<p class=\"sg-dg\">" + 
									"<span class=\"sg-dl\">Synonyms:</span>" + 
									"<span class=\"sg-s\"> " + synonyms + "</span>" +
								"</p>" +	
								"<p class=\"sg-dg\">" + 
									"<span class=\"sg-dl\">Description:</span>" + 
									"<span class=\"sg-d\"> " + desc + "</span>" + 
								"</p>" + 
							"</div>";
			jDoc.put("result", value);

			/*
			 * Hypernym
			 */
			String hypernym = (String) doc.getFieldValue(SolrConstants.HYPERNYM_FIELD);
			jDoc.put("hypernym", hypernym + " (" + posLabel + ")");

			/*
			 * Description
			 */
			String queryString = (String) doc.getFieldValue(SolrConstants.WORD_FIELD);
			jDoc.put("qs", queryString);

			jArray.put(jDoc);

		}

		/*
		 * Render result
		 */
		return jArray.toString();
		// return createGrid(jArray);

	}

	public String createGrid(JSONArray jArray) throws Exception {

		JSONObject jResponse = new JSONObject();
		int card = jArray.length();

		try {

			jResponse.put(ScConstants.SC_STATUS, 0);
			jResponse.put(ScConstants.SC_TOTALROWS, card);
			// jResponse.put(ScConstants.SC_STARTROW, 0);
			// jResponse.put(ScConstants.SC_ENDROW, card);

			jResponse.put(ScConstants.SC_DATA, jArray);

		} catch (Exception e) {
			e.printStackTrace();

		} finally {
		}

		return jResponse.toString(4);

	}

}
