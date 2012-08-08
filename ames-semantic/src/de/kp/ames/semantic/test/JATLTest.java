package de.kp.ames.semantic.test;

import java.io.StringWriter;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import junit.framework.TestCase;

import com.googlecode.jatl.Html;

import de.kp.ames.semantic.scm.SCMSearcher;

public class JATLTest extends TestCase {

	public void test() throws Exception {
		
//		
		StringWriter sw = new StringWriter();
		Html html = new Html(sw);
		html
			.div().classAttr("sgh")
				.span().classAttr("sgh-s")
					.text("zickzack")
			.endAll();
		
		String htmlOut = sw.getBuffer().toString();
		System.out.println("1: " + htmlOut);
		
		sw = new StringWriter();
		html = new Html(sw);
		html
			.div().classAttr("sgh")
				.span().classAttr("sgh-s")
					.text("zickzack")
			.endAll();
		htmlOut = sw.getBuffer().toString();
		System.out.println("2: " + htmlOut);

		String mod = "de.kp.web.AmesTestClass";
		System.out.println("3: " + mod.substring(0, mod.length() - "AmesTestClass".length() - 1));

		final JSONArray jCheckout = new JSONArray();
		jCheckout.put(
			new JSONObject(new HashMap<String, String>() {{
				put("suggest", "get (action)");
				put("id", "");
			}}));
		jCheckout.put(
				new JSONObject(new HashMap<String, String>() {{
					put("suggest", "get (action)");
					put("id", "");
				}}));
		jCheckout.put(
				new JSONObject(new HashMap<String, String>() {{
					put("suggest", "dms (core)");
					put("id", "");
				}}));
		
		System.out.println(new SCMSearcher().generateCheckoutHtml(jCheckout, true));

	}
	
}
