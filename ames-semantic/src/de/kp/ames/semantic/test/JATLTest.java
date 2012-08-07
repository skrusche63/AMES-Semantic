package de.kp.ames.semantic.test;

import java.io.StringWriter;

import junit.framework.TestCase;

import com.googlecode.jatl.Html;

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
		

	}
}
