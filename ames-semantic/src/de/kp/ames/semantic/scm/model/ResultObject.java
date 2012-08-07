package de.kp.ames.semantic.scm.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrDocument;

import de.kp.ames.semantic.solr.SolrConstants;

public class ResultObject {
	private SolrDocument doc;
	private Map<String, Map<String, List<String>>> highlighting;

	public ResultObject(SolrDocument doc) {
		this(doc, null);
	}

	public ResultObject(SolrDocument doc, Map<String, Map<String, List<String>>> highlighting) {
		this.doc = doc;
		this.highlighting = highlighting;
	}

	public String getId() {
		return (String) doc.getFieldValue("id");
	}

	public String getName() {
		return (String) doc.getFieldValue(SolrConstants.NAME_FIELD);
	}

	/*
	 * title is a multivalue field, only one value expected
	 */
	public String getTitle() {
		return (String) ((ArrayList) doc.getFieldValue(SolrConstants.TITLE_FIELD)).get(0);
	}

	public String getDescription() {
		return (String) doc.getFieldValue("description");
	}

	public String getSource() {
		return (String) ((ArrayList) doc.getFieldValue(SolrConstants.SOURCE_FIELD)).get(0);
	}

	public String getHighlightTitle() {
		String highlighted = null;
		if (highlighting.get(getId()).containsKey(SolrConstants.TITLE_FIELD)) {
			highlighted = highlighting.get(getId()).get(SolrConstants.TITLE_FIELD).get(0);
		} else {
			highlighted = getTitle();
		}
		return highlighted;
	}

	/*
	 * Multiple teaser support with elipses
	 */
	public String getHighlightDescription() {
		String highlighted = "";
		if (highlighting.get(getId()).containsKey("description")) {
			ArrayList<String> hls = (ArrayList<String>) highlighting.get(getId()).get("description");
			for (String hl : hls) {
				highlighted += " ... " + hl;
			}
			highlighted += " ...";
		} else {
			highlighted = getDescription();
		}
		
		return highlighted;
	}

	/*
	 * construct package from FQDN title and substract name
	 */
	public String getPackage() {
		String title = getTitle();
		return title.substring(0, title.length() - getName().length() - 1);
	}

	public String getMetricBacklinks() {
		return (String) ((ArrayList) doc.getFieldValue("mback_kps")).get(0);
	}

	public String getMetricMethodCount() {
		return (String) ((ArrayList) doc.getFieldValue("mmthd_kps")).get(0);
	}

	public String getMetricLOC() {
		return (String) ((ArrayList) doc.getFieldValue("mloc_kps")).get(0);
	}

	public String getAnnotations() {
		String result = StringUtils.join((ArrayList)doc.getFieldValue("tags_kpg"), ", ");
		if (result.startsWith(", ")) result = result.substring(2);
		return result;
	}

	
}
