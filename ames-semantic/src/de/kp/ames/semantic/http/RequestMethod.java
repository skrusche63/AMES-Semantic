package de.kp.ames.semantic.http;
/**
 *	Copyright 2012 Dr. Krusche & Partner PartG
 *
 *	AMES-Web-Service is free software: you can redistribute it and/or 
 *	modify it under the terms of the GNU General Public License 
 *	as published by the Free Software Foundation, either version 3 of 
 *	the License, or (at your option) any later version.
 *
 *	AMES- Web-Service is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * 
 *  See the GNU General Public License for more details. 
 *
 *	You should have received a copy of the GNU General Public License
 *	along with this software. If not, see <http://www.gnu.org/licenses/>.
 *
 */

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Stefan Krusche (krusche@dr-kruscheundpartner.de)
 *
 */

public class RequestMethod {

	private HttpServletRequest request;
	
	private String query;

	private String name;
	private HashMap<String, String> attributes;
	
	private static String ATTRIBUTE_ERROR = "[RequestMethod] Attribute Retrieval Error";
	private static String METHOD_ERROR    = "[RequestMethod] Method Retrieval Error";

	/**
	 * @param query
	 * @throws Exception
	 */
	public RequestMethod(HttpServletRequest request) throws Exception {

		/*
		 * Register request
		 */
		this.request =request;
		
		/*
		 * Derive query
		 */
		this.query = request.getQueryString();
		
		System.out.println("-----> RequestMethod> " + this.query);
		
		String[] tokens = query.split("&");
		
		// the first token describes the method: method=name
		setName(tokens[0]);
		
		if (tokens.length > 1) {
			// retrieve attribute from query: key=value
			attributes = new HashMap<String, String>();
			for (int i=1; i < tokens.length; i++) {
				setAttribute(tokens[i]);
			}
		}

	}
	
	/**
	 * 
	 * @param str
	 * @return
	 * 
	 * A utility function to decode incoming query parameter values, 
	 * Or use an existing utility class like
	 * http://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/org/apache/http/client/utils/URLEncodedUtils.html
	 */
    public String urlDecode(String str) {
        if (str == null) {
            return null;
        }
        try {
            return URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }
	
	/**
	 * @param token
	 * @throws Exception
	 */
	private void setAttribute(String token) throws Exception {

		String[] tokens = token.split("=");

		if (tokens.length != 2) throw new Exception(ATTRIBUTE_ERROR);
		
		String key = tokens[0];
		String val = urlDecode(tokens[1]);
		
		// System.out.println("====> k<" + key + "> v<" + val + ">");
		
		attributes.put(key, val);
		
	}
	
	/**
	 * @param token
	 * @throws Exception
	 */
	private void setName(String token) throws Exception {
		
		String[] tokens = token.split("=");
		
		System.out.println("setName: token: " + token + " token 0: " + tokens[0]);

		if (tokens.length != 2) throw new Exception(METHOD_ERROR);
		if (tokens[0].equals("method") == false) throw new Exception(METHOD_ERROR);
		
		this.name = tokens[1];
		
	}
	
	/**
	 * @return
	 */
	public HttpServletRequest getRequest() {
		return this.request;
	}
	
	/**
	 * @return
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * @return
	 */
	public String getQuery() {
		return this.query;
	}
	
	/**
	 * @return
	 */
	public HashMap<String, String> getAttributes() {
		return this.attributes;
	}
	
	/**
	 * @param key
	 * @return
	 */
	public String getAttribute(String key) {
		
		if (this.attributes.containsKey(key)) {
			return this.attributes.get(key);
		}
		
		return null;
	
	}
	
	/**
	 * @return
	 */
	public String toQuery() {
		
		StringBuffer sb = new StringBuffer();
		
		/* 
		 * Add method
		 */
		sb.append("?method=" + this.name);
		
		/* 
		 * Add attributes
		 */
		Set<String> keys = this.attributes.keySet();
		for (String key:keys) {
			sb.append("&" + key + "=" + this.attributes.get(key));
		}
		
		return sb.toString();

	}

}
