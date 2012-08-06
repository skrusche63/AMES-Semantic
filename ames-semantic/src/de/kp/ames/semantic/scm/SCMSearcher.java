package de.kp.ames.semantic.scm;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.kp.ames.semantic.Bundle;
import de.kp.ames.semantic.globals.ScConstants;
import de.kp.ames.semantic.solr.SolrConstants;
import de.kp.ames.semantic.solr.SolrProxy;

public class SCMSearcher {
	
	private static SuggestionLRUCache<String, Integer> suggestionLRUCache = new SuggestionLRUCache<String, Integer>(50, 100);
	/*
	 * Reference to SolrProxy
	 */
	private SolrProxy solrProxy;
	private Integer MAX_SIMILARITY_LEVEL = 4;
	private Integer MAX_SIMILARITY_LEAVES = 5;

	private static Bundle bundle = Bundle.getInstance();
	private static String FILE_SEPARATOR = System.getProperty("file.separator");
	
	public SCMSearcher() {
		solrProxy = SolrProxy.getInstance();
	}

	
	/**
	 * Process a Java-Module ZIP file representation of the cart
	 * 
	 * @param jCheckout
	 * @return
	 * @throws Exception 
	 * @throws Exception
	 */
	public byte[] download(JSONArray jCheckout) throws Exception {

    	System.out.println("====> SCMSearcher.download");

		// generate HTML representation as readme
		String semanticResearchReport = generateCheckoutHtml(jCheckout);
		
		List<String> ids = new ArrayList<String>();
		for (int i = 0; i < jCheckout.length(); i++) {
			JSONObject record = jCheckout.getJSONObject(i);
			ids.add(record.getString("id"));
		} 
		
		List<String> files = getAbsoluteFilenamesFormIds(ids);
		byte[] zip = zipFiles(bundle.getString("ames.scm.root"), files, semanticResearchReport);

    	System.out.println("====> SCMSearcher.download.zipFiles packed");

		return zip;
	}

	@SuppressWarnings({ "unchecked" })
	private List<String> getAbsoluteFilenamesFormIds(List<String> ids) throws Exception {
		/*
		 * Build Apache Solr query
		 */
		SolrQuery query = new SolrQuery();
		/*
		 *  concatenate quoted ids to searchTerm
		 *  
		 *  +id:("id1" "id2" "id3" "id4")
		 */
		
		String searchTerm = "+id:(\"" + StringUtils.join(ids, "\" \"")+ "\")";
		query.setQuery(searchTerm);
		query.setFields("id", "exturi_kps");
		query.setRows(ids.size());

		QueryResponse response = solrProxy.executeQuery(query);
		SolrDocumentList docs = response.getResults();
		
		Iterator<SolrDocument> iter = docs.iterator();
		List<String> files = new ArrayList<String>();
		while (iter.hasNext()) {

			SolrDocument doc = iter.next();
			files.add(((ArrayList<String>) doc.getFieldValue(SolrConstants.EXTURI_FIELD)).get(0));
		}
		
    	System.out.println("======> SCMSearcher.download.getAbsoluteFilenamesFormIds: " + files.size());

		return files;
	}


	/**
     * Compress the given directory with all its files.
     */
    private byte[] zipFiles(String scmRootFolderName, List<String> files, String semanticResearchReport) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        //File scmRootFolder = new File(scmRootFolderName);
        
        /*
         * write checkout report in root folder of zip
         * 	SemanticResearchReport.html
         */
        zos.putNextEntry(new ZipEntry("SemanticResearchReport.html"));
        zos.write(semanticResearchReport.getBytes());
        zos.closeEntry();
        
        
        byte bytes[] = new byte[2048];
        
        scmRootFolderName = makeOSDependantAbsoluteFileName(scmRootFolderName);
 
        for (String fileName : files) {
        	
        	System.out.println("========> SCMSearcher.download.zipFiles: " + fileName);
        	
        	fileName = makeOSDependantAbsoluteFileName(fileName);
        	
            FileInputStream fis = new FileInputStream(fileName);
            BufferedInputStream bis = new BufferedInputStream(fis);
 
            zos.putNextEntry(new ZipEntry(getRelativeFileName(fileName, scmRootFolderName)));
 
            int bytesRead;
            while ((bytesRead = bis.read(bytes)) != -1) {
                zos.write(bytes, 0, bytesRead);
            }
            zos.closeEntry();
            bis.close();
            fis.close();
        }
        zos.flush();
        baos.flush();
        zos.close();
        baos.close();
 
        return baos.toByteArray();
    }


	private String makeOSDependantAbsoluteFileName(String fileName) {
		return fileName.replace("file:/", "").replace("/", SCMSearcher.FILE_SEPARATOR);
	}
	
	private String getRelativeFileName(String fileName, String scmRootFolderName) {
		String relativeFileName = fileName.replace(scmRootFolderName, "");
		
		System.out.println("========> SCMSearcher.download.zipFiles:getrelative " + relativeFileName);
		if (relativeFileName.startsWith(FILE_SEPARATOR))
			relativeFileName = relativeFileName.substring(1);

		return relativeFileName;
	}


	/**
	 * Process a server-side HTML representation of the cart
	 * 
	 * @param jCheckout
	 * @return
	 * @throws Exception
	 */
	public String checkout(JSONArray jCheckout) throws Exception {
		
		System.out.println("====> SCMSearcher.checkout: count: " + jCheckout.length());

		String response = generateCheckoutHtml(jCheckout);
	
		return createCheckout(response.toString());
	}


	private String generateCheckoutHtml(JSONArray jCheckout) throws JSONException {
		StringBuilder response = new StringBuilder();
		response.append("<html><body><ul>");
		for (int i = 0; i < jCheckout.length(); i++) {
			JSONObject record = jCheckout.getJSONObject(i);
			response.append("<li>suggestion: " + record.getString("suggest") + " / module:" + record.getString("choice") + "</li>");
		} 
		response.append("</ul></body></html>");
		return response.toString();
	}

	
	/**
	 * Query is uid from focused record
	 * This query processes a recursive query till max level is reached
	 * and responds with a JSONObject compatible with thejit HyperTree  
	 * 
	 * @param uid
	 * @return
	 * @throws Exception
	 */
	public String similar(String uid, String rootName) throws Exception {

		return hypertree(uid, rootName);
	}

	public String search(String searchTerm, String start, String end) throws Exception {

		/*
		 * Build Apache Solr query
		 */
		SolrQuery query = new SolrQuery();

		/*
		 * Paging support from Apache Solr
		 */
		int s = Integer.valueOf(start);
		int e = Integer.valueOf(end);
		int r = e - s;

		query.setStart(s);
		query.setRows(r);

		// String qs = SolrConstants.WORD_FIELD + ":" + searchTerm;
		String qs = searchTerm; // default field
		// query term weights are prepared from suggest already
		query.setQuery(qs);

		// this query settings can be moved to an own RequestHandler too
		query.setParam("defType", "edismax");
		// query field weights
		query.setParam("qf", "tags_kpg^20.0 description^0.3");
//		query.setParam("qf", "title^20.0 description^0.3");
		query.setParam("q.op", "OR");

		/*
		 * set filter for a specific set of indexed documents within Solr by
		 * category "cat" field
		 */
		query.setFilterQueries(SolrConstants.CATEGORY_FIELD + ":" +
				SolrConstants.CATEGORY_RESULT_SCM_VALUE);

		query.addHighlightField(SolrConstants.TITLE_FIELD);
		query.addHighlightField("description");
		query.setHighlight(true);

		query.setHighlightSimplePre("<span class=\"sg-th\">");
		query.setHighlightSimplePost("</span>");
		query.setHighlightSnippets(3);

		QueryResponse response = solrProxy.executeQuery(query);
		SolrDocumentList docs = response.getResults();
		long total = docs.getNumFound();

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
			 * Title (multivalue field)
			 */
			String title = (String) ((ArrayList) doc.getFieldValue(SolrConstants.TITLE_FIELD)).get(0);
			if (title == null) {
				System.out.println("ID has no title: " + id);
				continue;
			}
			jDoc.put("title", title);
			jDoc.put("name", doc.getFieldValue(SolrConstants.NAME_FIELD));
			jDoc.put("source", doc.getFieldValue(SolrConstants.SOURCE_FIELD));

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
			String resultString = "" +
					"<div class=\"sg\">" + 
						"<span class=\"sg-t\">" + highlightedTitle + "</span>" +
						"<p class=\"sg-dg\">" + 
							"<span class=\"sg-dl\">Description:</span>" + 
							"<span class=\"sg-d\"> " + highlightedDescription + "</span>" + 
						"</p>" + 
					"</div>";
			jDoc.put("result", resultString);
			
			String descString = "" +
				"<div class=\"sg\">" + 
					"<p class=\"sg-dg\">" + 
						"<span class=\"sg-dl\">Id:</span>" + 
						"<span class=\"sg-d\"> " + id + "</span>" + 
					"</p>" + 
					"<p class=\"sg-dg\">" + 
						"<span class=\"sg-dl\">Metric: </span>" + 
						"<a class=\"sg-lk\" target=\"_blank\" href=\"http://en.wikipedia.org/wiki/" + 
							title.replace(" ", "_") + "\">" + title + "</a>" + 
					"</p>" + 
				"</div>";
			jDoc.put("desc", descString);

			jArray.put(jDoc);
		}

		/*
		 * Render result
		 */
		// return jArray.toString();
		return createGrid(jArray, start, end, String.valueOf(total));

	}

	public String suggest(String prefix, String start, String end) throws Exception {

		/*
		 * Build Apache Solr query
		 */
		SolrQuery query = new SolrQuery();

		/*
		 * Paging support from Apache Solr
		 */
		int s = Integer.valueOf(start);
		int e = Integer.valueOf(end);
		int r = e - s;

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
		query.setFilterQueries(SolrConstants.CATEGORY_FIELD + ":" + SolrConstants.CATEGORY_SUGGEST_SCM_VALUE);

		// query.addHighlightField(SolrConstants.WORD_FIELD);
		query.addHighlightField("textsuggest");
		query.setHighlight(true);

		query.setHighlightSimplePre("<span class=\"sg-th\">");
		query.setHighlightSimplePost("</span>");

		QueryResponse response = solrProxy.executeQuery(query);

		SolrDocumentList docs = response.getResults();
		long total = docs.getNumFound();

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
			 * highlighted term field
			 */
			String highlightedTerm = null;
			if (highlighting.get(id).containsKey("textsuggest")) {
				highlightedTerm = highlighting.get(id).get("textsuggest").get(0);
			} else {
				highlightedTerm = (String) doc.getFieldValue("textsuggest");
			}

			jDoc.put("term", highlightedTerm);

			/*
			 * HTML rendered Result field
			 */
			String synonyms = (String) doc.getFieldValue(SolrConstants.SYNONYM_FIELD);
			String value = "<div class=\"sg\">" + "<span class=\"sg-t\">" + highlightedTerm + "</span>"
					+ "<p class=\"sg-dg\">" + "<span class=\"sg-dl\">Related:</span>" + "<span class=\"sg-s\"> "
					+ synonyms + "</span>" + "</p>" + "<p class=\"sg-dg\">"
					+ "</p>" + "</div>";
			jDoc.put("result", value);

			/*
			 * Hypernym
			 */
			String hypernym = (String) doc.getFieldValue(SolrConstants.HYPERNYM_FIELD);
			jDoc.put("hypernym", hypernym);

			/*
			 * Query String for selection
			 */
			// String queryString = (String)
			// doc.getFieldValue(SolrConstants.WORD_FIELD);
			/*
			 * All terms are quoted as phrases and combined with an open OR
			 * query
			 * 
			 * Main term is mandatory and weighted 50 +queryString^20 Synonyms
			 * is optional and weighted 10 Hypernym is optional and weighted 5
			 */
			String synonymBoosts = "\"" + synonyms.replace(", ", "\"^10 OR \"") + "\"^10";
			String queryString = (String) doc.getFieldValue("textsuggest");
			jDoc.put("qs", "+\"" + queryString + "\"^100 OR " + "\"" + hypernym + "\"^5 OR " + synonymBoosts);
			// raw query string for TextWidget update
			jDoc.put("qsraw", queryString);

			jDoc.put("type", "suggest");

			jArray.put(jDoc);

		}

		/*
		 * group entries by hypernym
		 */
		ArrayList<String> groupHeaders = new ArrayList<String>();
		ArrayList<ArrayList<JSONObject>> groupedList = new ArrayList<ArrayList<JSONObject>>();
		for (int i = 0; i < jArray.length(); i++) {
			JSONObject doc = (JSONObject) jArray.get(i);
			String hypernym = doc.getString("hypernym");

			if (groupHeaders.contains(hypernym)) {
				// add doc to groupedList with index number as hypernym in
				// groupHeaders
				int index = groupHeaders.indexOf(hypernym);
				groupedList.get(index).add(doc);
			} else {
				// add new group header
				groupHeaders.add(hypernym);
				System.out.println("====> SCM.suggest: new group: <" + hypernym+ ">");

				// add new empty list
				groupedList.add(new ArrayList<JSONObject>());
				// add doc to last new empty list
				ArrayList<JSONObject> lastList = groupedList.get(groupedList.size() - 1);
				JSONObject jGroupHeaderDoc = new JSONObject();
				// generate unique key from first doc with g: prefix
				jGroupHeaderDoc.put("id", "g:" + doc.getString("id"));
				jGroupHeaderDoc.put("result", "<div class=\"sgh\">" + "<span class=\"sgh-s\"> " + hypernym + 
						"</span>" + "</div>");

				jGroupHeaderDoc.put("type", "group");
				jGroupHeaderDoc.put("enabled", false);
				jGroupHeaderDoc.put("qsraw", hypernym);
				// add pseudo group entry
				lastList.add(jGroupHeaderDoc);

				// add doc after pseudo group entry
				lastList.add(doc);
			}
		}

		/*
		 * flatten result list
		 */
		JSONArray jGroupedJArray = new JSONArray();
		// depth first
		for (ArrayList<JSONObject> groupList : groupedList) {

			JSONObject jGroupHeader = groupList.get(0);
			
			jGroupHeader.put(
					"result",
					jGroupHeader.getString("result")
					);

			
			for (JSONObject doc : groupList) {
				jGroupedJArray.put(doc);
			}
		}

		/*
		 * increase total count with additional group-headers count
		 */
		System.out.println("====> SCM.suggest: term: <" + prefix + "> s/e: " + s + "/" + e +" total: " + total + " groups: " + groupHeaders.size());
		if (suggestionLRUCache.containsKey(prefix)) {
//			if (groupHeaders.size() == 0) {
//				// no additional headers
//				total = suggestionLRUCache.get(prefix);
//			} else {
//				// paging search will add additional headers 
//				total = suggestionLRUCache.get(prefix) + groupHeaders.size();
//				suggestionLRUCache.put(prefix, (int) total);
//			}
			total = suggestionLRUCache.get(prefix);
				
		} else {
			total = total + groupHeaders.size();
			suggestionLRUCache.put(prefix, (int) total);
		}
		System.out.println("======> SCM.suggest: LRU total: " + total);

		/*
		 * Render result
		 */
		// return jGroupedJArray.toString();
		// with group headers
		return createGrid(jGroupedJArray, start, end, String.valueOf(total));

		// without group headers
		// return createGrid(jArray, start, end, String.valueOf(total));

	}

	public String createGrid(JSONArray jArray, String start, String end, String total) throws Exception {

		JSONObject jResponse = new JSONObject();

		try {

			jResponse.put(ScConstants.SC_STATUS, 0);
			jResponse.put(ScConstants.SC_TOTALROWS, total);
			// jResponse.put(ScConstants.SC_STARTROW, start);
			// jResponse.put(ScConstants.SC_ENDROW, end);

			jResponse.put(ScConstants.SC_DATA, jArray);

		} catch (Exception e) {
			e.printStackTrace();

		} finally {
		}

		return new JSONObject().put("response", jResponse).toString();

	}
	
	public String createCheckout(String checkoutPage) throws Exception {

		JSONObject jResponse = new JSONObject();

		try {
			jResponse.put(ScConstants.SC_DATA, checkoutPage);

		} catch (Exception e) {
			e.printStackTrace();

		} finally {
		}

		return jResponse.toString();

	}

	/************************************************************************
	 * 
	 * HYPERTREE HYPERTREE HYPERTREE HYPERTREE HYPERTREE
	 * @param rootName 
	 * 
	 ***********************************************************************/

	public String hypertree(String uid, String rootName) throws Exception {

		Set<String> cache = new HashSet<String>();
		Integer level = 0;

		/*
		 * define root node
		 */
		JSONObject jRootDocument = new JSONObject();
		jRootDocument.put("id", "0");
		jRootDocument.put("name", rootName);
		jRootDocument.put("data", new JSONArray());
		
		// register root uid to avoid infinite loops
		cache.add(uid);
		
		JSONArray jHypertreeChildren = getJHypertree(uid, level, cache);

		// this 'children' parameter is a MUST
		jRootDocument.put("children", jHypertreeChildren);

		return jRootDocument.toString();

	}

	// this method retrieves the respective solr document
	// as a JSON representation

	private JSONArray getJHypertree(String uid, Integer level, Set<String> cache) throws Exception {


		JSONArray jChildren = new JSONArray();
		

		
		// update level
		level = level + 1;
		String debugIdent = StringUtils.repeat("..", level); 

		/*
		 * Build Apache Solr query
		 */
		SolrQuery query = new SolrQuery();

		/*
		 * Paging support from Apache Solr
		 */
		int s = 0;
		int r = 2 * MAX_SIMILARITY_LEAVES; // ask for more to fill up the duplicates

		query.setStart(s);
		query.setRows(r);
		
		// choose suggest requesthandler
		query.setQueryType("/mlt");

		/*
		 *  quoted query due to colon conflicts on solr, it cannot separate
		 *  id: from following uid
		 *  &q=id:urn:de:kp:ames:scm:ADF:af613363 
		 */
		
		String searchTerm = "id:\"" + uid + "\"";
		query.setQuery(searchTerm);
		
		// MLT more like this parameters
		query.setParam("mlt.fl", "tags_kpg");
		query.setParam("mlt.mintf", "1");
		query.setParam("mlt.mindf", "2");
		query.setParam("mlt.match.include", "false");
		// minimize result size
		query.setFields("id", "name", "title");
		
		QueryResponse response = solrProxy.executeQuery(query);
		SolrDocumentList docs = response.getResults();
		
		if (docs == null) return jChildren;

		Iterator<SolrDocument> iter = docs.iterator();
		Map<String, JSONArray> childrenQueue = new HashMap<String, JSONArray>();
		
		int directChildrenCount = 0;
		while (iter.hasNext()) {

			if (directChildrenCount == MAX_SIMILARITY_LEAVES)
				// children count is satisfied, so we can skip the additional docs
				break;
			
			SolrDocument doc = iter.next();
			String rid = (String) doc.getFieldValue("id");
			

			if (cache.contains(rid)) {
				// System.out.println(debugIdent + "skip: " + level + " n> "  + doc.getFieldValue("name") + " / " + rid);
				// skip relations to already traversed nodes
				continue;
			} 
			
			cache.add(rid);
			//System.out.println(debugIdent + " cch: " + level + " c> "  + cache.size());
			
			JSONObject jDocument = new JSONObject();
			jDocument.put("id", rid);
			jDocument.put("name", (String) doc.getFieldValue("name"));
			jDocument.put("data", new JSONArray());
			
			// this 'children' parameter is a MUST
			JSONArray jSubChildren = new JSONArray();
			jDocument.put("children", jSubChildren);
			childrenQueue.put(rid, jSubChildren);
			
			//System.out.println(debugIdent + " add: " + level + " n> "  + doc.getFieldValue("name") + " / " + rid);

			
			// add ourselve to parent children
			jChildren.put(jDocument);
			directChildrenCount++;
		}
		
		
		if (level < MAX_SIMILARITY_LEVEL) {
			Iterator<Entry<String, JSONArray>> it = childrenQueue.entrySet().iterator();
			while (it.hasNext()) {
				 Map.Entry<String, JSONArray> pairs = (Map.Entry<String, JSONArray>) it.next();
				 String rid = pairs.getKey();
				 JSONArray jSubChildren = pairs.getValue();

				 JSONArray jRelatedChildren = getJHypertree(rid, level, cache);
				 if (jRelatedChildren.length() > 0) {
					for (int i = 0; i < jRelatedChildren.length(); i++) {
						jSubChildren.put(i, jRelatedChildren.get(i));
					} 
				}
			}
		}


		return jChildren;

	}

}
