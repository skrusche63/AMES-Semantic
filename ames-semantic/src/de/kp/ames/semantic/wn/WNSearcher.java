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

	/*
	 * experimental Wikipedia OPenSearch API test
	 */
	public String suggestWikipedia(String prefix, String limit) {
		StringBuilder builder = new StringBuilder();
		
		// http://en.wikipedia.org/w/api.php?action=opensearch&search=a&limit=10&namespace=0&format=jsonfm
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet("http://en.wikipedia.org/w/api.php?action=opensearch&search=" + prefix + "&limit=" + limit + "&namespace=0&format=xml");
		
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				
				
				
//				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
//				String line;
//				while ((line = reader.readLine()) != null) {
//					builder.append(line);
//				}
			} else {
				return "[\"ERROR\",[]]";
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*
		 * Example:
		 * http://en.wikipedia.org/w/api.php?action=opensearch&search=hou&limit=10&namespace=0&format=xml
		 * 
		<SearchSuggestion xmlns="http://opensearch.org/searchsuggest2" version="2.0">
		<Query xml:space="preserve">hou</Query>
		<Section>
			<Item>
			<Image source="http://upload.wikimedia.org/wikipedia/commons/thumb/4/4e/Queen_Anne_in_the_House_of_Lords.jpg/41px-Queen_Anne_in_the_House_of_Lords.jpg" width="41" height="50"/>
			<Text xml:space="preserve">House of Lords</Text>
			<Description xml:space="preserve">
			The House of Lords is the upper house of the Parliament of the United Kingdom. Like the House of Commons, it meets in the Palace of Westminster.
			</Description>
			<Url xml:space="preserve">http://en.wikipedia.org/wiki/House_of_Lords</Url>
			</Item>
			
			...
			
			<Item>
			<Image source="http://upload.wikimedia.org/wikipedia/commons/thumb/7/7b/Coat_of_Arms_of_England_%281509-1554%29.svg/50px-Coat_of_Arms_of_England_%281509-1554%29.svg.png" width="50" height="50"/>
			<Text xml:space="preserve">House of Commons of England</Text>
			<Description xml:space="preserve">
			The House of Commons of England was the lower house of the Parliament of England (which incorporated Wales) from its development in the 14th century to the union of England and Scotland in 1707, when it was replaced by the House of Commons of Great Britain.
			</Description>
			<Url xml:space="preserve">
			http://en.wikipedia.org/wiki/House_of_Commons_of_England
			</Url>
			</Item>
		</Section>
		</SearchSuggestion>
		 */
		return builder.toString();

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
		 * set filter for a specific set of indexed documents
		 * within Solr by category "cat" field  
		 */
		query.setFilterQueries("cat:" + SolrConstants.CATEGORY_VALUE);
		
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
			String id  = (String)doc.getFieldValue("id");
			
			String pos = id.substring(0,1);
			String posLabel = WNConstants.posMap.get(pos).getLabel();
			
			// change emboss to bold tag
			String highlightedTerm = highlighting.get(id).get(SolrConstants.WORD_FIELD).get(0);
						
			jDoc.put("term", highlightedTerm);

			/* 
			 * Description
			 */
			String desc  = (String)doc.getFieldValue(SolrConstants.DESC_FIELD);
			jDoc.put("desc", desc);
			
			/* 
			 * Synonyms
			 */
			String synonyms  = (String)doc.getFieldValue(SolrConstants.SYNONYM_FIELD);
//			jDoc.put("synonyms", synonyms);
			String value = "<div class=\"sg\"><span class=\"sg-t\">" 
						+ highlightedTerm 
						+ "</span><span class=\"sg-s\"> " + synonyms 
						+ "</span><p class=\"sg-dg\"><span class=\"sg-dl\">Description:</span><span class=\"sg-d\"> " 
						+ desc + "</span></p></div>";
			jDoc.put("synonyms", value);

			/* 
			 * Hypernym
			 */
			String hypernym  = (String)doc.getFieldValue(SolrConstants.HYPERNYM_FIELD);
			jDoc.put("hypernym", hypernym  + " (" + posLabel + ")");
			
			jArray.put(jDoc);

		}

		/*
		 * Render result
		 */
		return jArray.toString();
//		return createGrid(jArray);
		
	}	

	public String createGrid(JSONArray jArray) throws Exception {

		JSONObject jResponse = new JSONObject();
		int card = jArray.length();

		try {
		
			jResponse.put(ScConstants.SC_STATUS, 0);	
			jResponse.put(ScConstants.SC_TOTALROWS, card);
//			jResponse.put(ScConstants.SC_STARTROW, 0);
//			jResponse.put(ScConstants.SC_ENDROW, card);

			jResponse.put(ScConstants.SC_DATA, jArray);
			
		} catch(Exception e) {
			e.printStackTrace();
			
		} finally {}

		
		return jResponse.toString(4);
		

	}
	
	
}
