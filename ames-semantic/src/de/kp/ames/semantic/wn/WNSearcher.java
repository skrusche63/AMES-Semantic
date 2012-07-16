package de.kp.ames.semantic.wn;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
		// query term weights are prepared from suggest already
		query.setQuery(qs);
		
		// this query settings can be moved to an own RequestHandler too
		query.setParam("defType", "edismax");
		// query field weights
		query.setParam("qf", "title^20.0 description^0.3");
		query.setParam("q.op", "OR");
		
		/*
		 * set filter for a specific set of indexed documents within Solr by
		 * category "cat" field
		 */
		// TODO: reindex wikipedia content with category first
		// in the mean time, exclude (-cat:sgwn) all documents from WordNet suggest feed
		query.setFilterQueries("-"+SolrConstants.CATEGORY_FIELD + ":" + SolrConstants.CATEGORY_SUGGEST_VALUE);
//		query.setFilterQueries(SolrConstants.CATEGORY_FIELD + ":" + SolrConstants.CATEGORY_RESULT_VALUE);

		query.addHighlightField(SolrConstants.TITLE_FIELD);
		query.addHighlightField("description");
		query.setHighlight(true);

		query.setHighlightSimplePre("<span class=\"sg-th\">");
		query.setHighlightSimplePost("</span>");
		query.setHighlightSnippets(3);

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
			String title = (String) ((ArrayList)doc.getFieldValue(SolrConstants.TITLE_FIELD)).get(0);
			if (title == null) {
				System.out.println("ID has no title: " + id);
				continue;
			}
			jDoc.put("title", title);

			/*
			 * Description
			 */
			String description = (String) doc.getFieldValue("description");

			
			/*
			 * highlighted field
			 */
			String highlightedTitle = null;
			if (highlighting.get(id).containsKey(SolrConstants.TITLE_FIELD)) {
				highlightedTitle = highlighting.get(id).get(SolrConstants.TITLE_FIELD).get(0);
			} else {
				highlightedTitle = title;
			}
			String highlightedDescription = "";
			if (highlighting.get(id).containsKey("description")) {
				ArrayList<String> hls = (ArrayList<String>) highlighting.get(id).get("description");
				for (String hl : hls) {
					highlightedDescription += " ... " + hl;
				}
				highlightedDescription += " ...";
			} else {
				highlightedDescription = description;
			}
	
			/*
			 * result field
			 */
			String value = "<div class=\"sg\">" + 
								"<span class=\"sg-t\">" + highlightedTitle + "</span>" +
								"<p class=\"sg-dg\">" + 
									"<span class=\"sg-dl\">Description:</span>" + 
									"<span class=\"sg-d\"> " + highlightedDescription + "</span>" + 
								"</p>" + 
								"<p class=\"sg-dg\">" + 
									"<span class=\"sg-dl\">Id:</span>" +
									"<span class=\"sg-d\"> " + id + "</span>" +
								"</p>" +
								"<p class=\"sg-dg\">" + 
									"<span class=\"sg-dl\">Link: </span>" +
									"<a class=\"sg-lk\" target=\"_blank\" href=\"http://en.wikipedia.org/wiki/" + title.replace(" ", "_") + "\">" + title + "</a>" +
								"</p>" +
							"</div>";
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

		String qs = prefix;
		query.setQuery(qs);
		
		// choose suggest requesthandler
		query.setQueryType("/suggest");

		/*
		 * set filter for a specific set of indexed documents within Solr by
		 * category "cat" field
		 */
		query.setFilterQueries(SolrConstants.CATEGORY_FIELD + ":" + SolrConstants.CATEGORY_SUGGEST_VALUE);

//		query.addHighlightField(SolrConstants.WORD_FIELD);
		query.addHighlightField("textsuggest");
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

			String pos = id.substring(0, 1);
			String posLabel = WNConstants.posMap.get(pos).getLabel();

			// change emboss to bold tag
//			String highlightedTerm = highlighting.get(id).get(SolrConstants.WORD_FIELD).get(0);
			/*
			 * highlighted field
			 */
			String highlightedTerm = null;
			if (highlighting.get(id).containsKey("textsuggest")) {
				highlightedTerm = highlighting.get(id).get("textsuggest").get(0);
			} else {
				highlightedTerm = (String) doc.getFieldValue("textsuggest");
			}

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
			 * Query String for selection
			 */
//			String queryString = (String) doc.getFieldValue(SolrConstants.WORD_FIELD);
			/*
			 * All terms are quoted as phrases and combined with an open OR query
			 * 
			 * Main term is mandatory and weighted 50 +queryString^20
			 * Synonyms is optional and weighted 10
			 * Hypernym is optional and weighted 5
			 */
			String synonymBoosts = "\"" + synonyms.replace(", ", "\"^10 OR \"") + "\"^10"; 
			String queryString = (String) doc.getFieldValue("textsuggest");
			jDoc.put("qs", "+\"" + queryString + "\"^100 OR " +
					"\"" + hypernym + "\"^5 OR " +
					synonymBoosts					
					);
			// raw query string for TextWidget update
			jDoc.put("qsraw", queryString);


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
