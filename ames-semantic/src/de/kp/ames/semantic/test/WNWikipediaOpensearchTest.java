package de.kp.ames.semantic.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import de.kp.ames.semantic.xml.WNJsonConverter;

public class WNWikipediaOpensearchTest extends TestCase {

	public void test() throws Exception {
		StringBuilder builder = new StringBuilder();
		
		// http://en.wikipedia.org/w/api.php?action=opensearch&search=a&limit=10&namespace=0&format=jsonfm
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet("http://en.wikipedia.org/w/api.php?action=opensearch&search=house&limit=10&namespace=0&format=xml");
		
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				
				System.out.println(WNJsonConverter.conversionXmlToJson(content));
				
			} else {
				System.out.println("Failed to download file");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
