package de.kp.ames.semantic.scm.renderer;

import java.io.StringWriter;

import org.json.JSONException;
import org.json.JSONObject;

import com.googlecode.jatl.Html;

import de.kp.ames.semantic.scm.model.ResultObject;
import de.kp.ames.semantic.scm.model.SuggestObject;

public class SCMHtmlRenderer {

	/*
	 * Suggest render methods
	 */
	
	public static  String getSuggestHtmlResult(SuggestObject scm) {
		StringWriter htmlWriter = new StringWriter();
		Html html = new Html(htmlWriter);
		html
			.div().classAttr("sg")
				// raw highlight injection, because it contains tags from Solr
				.span().classAttr("sg-t").raw(scm.getHighlightTextSuggest()).end()
				.p().classAttr("sg-dg")
					.span().classAttr("sg-dl").text("Related:").end()
					.span().classAttr("sg-s").text(scm.getSynonyms()).end()
				.end()
				.p().classAttr("sg-dg")
			.endAll();
		String result = htmlWriter.getBuffer().toString();
		return result;
	}
	
	public static  String getSuggestHtmlGroupHeader(JSONObject jDoc) throws JSONException {
		StringWriter htmlWriter = new StringWriter();
		Html html = new Html(htmlWriter);
		html
			.div().classAttr("sgh")
				.span().classAttr("sgh-s")
					.text(jDoc.getString("hypernym"))
			.endAll();
		
		String htmlOut = htmlWriter.getBuffer().toString();
		return htmlOut;
	}


	/*
	 * Search result render methods
	 */
	public static  String getResultHtml(ResultObject scm) {
		StringWriter htmlWriter = new StringWriter();
		Html html = new Html(htmlWriter);
		html
			.div().classAttr("sg")
				// raw highlight injection, because it contains tags from Solr
				.span().classAttr("sgh-t").raw(scm.getHighlightTitle()).end()
				.p().classAttr("sg-dg")
					.span().classAttr("sg-dl").text("Description:").end()
					.span().classAttr("sg-d").text(scm.getHighlightDescription()).end()
			.endAll();
		return htmlWriter.getBuffer().toString();
	}


	private static String getFlagForMetric(String metric) {
		String flag = null;
		if (metric.equals("low")) flag = "images/silk/flag_green.png";
		else if (metric.equals("medium")) flag = "images/silk/flag_yellow.png";
		else if (metric.equals("high")) flag = "images/silk/flag_red.png";
		
		return flag;
	}
	public static String getResultHtmlDescription(ResultObject scm) {
		StringWriter htmlWriter;
		Html html;
		htmlWriter = new StringWriter();
		html = new Html(htmlWriter);
		html
			.div().classAttr("sg")
				.p().classAttr("sg-dg")
					.span().classAttr("sg-dl").text("SCM  Project:").end()
					.span().classAttr("sg-d").text(scm.getSource()).end()
				.end()
				.p().classAttr("sg-dg")
					.span().classAttr("sg-dl").text("Annotations:").end()
					.span().classAttr("sg-d").text(scm.getAnnotations()).end()
				.end()
				.p().classAttr("sg-dg")
					.span().classAttr("sg-dl").text("Metrics: Used by other(").end()
					.span().classAttr("sg-d").text(scm.getMetricBacklinks().toUpperCase())
						.img().src(getFlagForMetric(scm.getMetricBacklinks())).height("16")
					.end()
					.span().classAttr("sg-dl").text(") / Method count(").end()
					.span().classAttr("sg-d").text(scm.getMetricMethodCount().toUpperCase())
						.img().src(getFlagForMetric(scm.getMetricMethodCount())).height("16")
					.end()	
					.span().classAttr("sg-dl").text(") / LOC(").end()
					.span().classAttr("sg-d").text(scm.getMetricLOC().toUpperCase())
						.img().src(getFlagForMetric(scm.getMetricLOC())).height("16")					
					.end()
					.span().classAttr("sg-dl").text(")").end()
			.endAll();
		return htmlWriter.getBuffer().toString();
	}
	
}
