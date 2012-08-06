package de.kp.ames.semantic.service;

import org.json.JSONArray;
import org.json.JSONException;

import de.kp.ames.semantic.http.RequestContext;
import de.kp.ames.semantic.scm.SCMSearcher;
import de.kp.ames.semantic.wn.WNSearcher;

/**
 * Copyright 2012 Dr. Krusche & Partner PartG
 * 
 * AMES-Web-Service is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * AMES- Web-Service is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this software. If not, see <http://www.gnu.org/licenses/>.
 * 
 */

public class SearchImpl extends ServiceImpl {
	
	static int count = 0; 

	/**
	 * Constructor
	 */
	public SearchImpl() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.kp.ames.web.core.service.ServiceImpl#processRequest(de.kp.ames.web
	 * .http.RequestContext)
	 */
	public void processRequest(RequestContext ctx) {

		String methodName = this.method.getName();
		if (!(methodName.equals("get") || methodName.equals("apply"))) {
			this.sendBadRequest(ctx, new Throwable("[SearchImpl] only method=get & apply supported"));
		}

		/*
		 * set search use case by source: wn or scm 
		 */
		String source = this.method.getAttribute("source");

		if (source == null || !(source.equals("wn") || source.equals("scm"))) {
			System.out.println("====> processRequest: source not set or not scm | wn: " + source);
			this.sendNotImplemented(ctx);
		}

		String type = this.method.getAttribute("type");
		System.out.println("====> processRequest: " + type);
		
		if (type.equals("suggest")) {

			/*
			 * Call suggest method
			 */
			String query = this.method.getAttribute("query");
			String start = this.method.getAttribute("_startRow");
			String end = this.method.getAttribute("_endRow");

			if ((!methodName.equals("get")) || (query == null) || (start == null) || (end == null)) {
				this.sendNotImplemented(ctx);

			} else {

				try {
					/*
					 * JSON response
					 */
					String content = suggest(source, query, start, end);
					this.sendJSONResponse(content, ctx.getResponse());

				} catch (Exception e) {
					this.sendBadRequest(ctx, e);

				}
			}
		} else if (type.equals("search")) {
			/*
			 * Call searchmethod
			 */
			String query = this.method.getAttribute("query");
			String start = this.method.getAttribute("_startRow");
			String end = this.method.getAttribute("_endRow");

			if ((!methodName.equals("get")) || (query == null) || (start == null) || (end == null)) {
				this.sendNotImplemented(ctx);

			} else {

				try {
					/*
					 * JSON response
					 */
					String content = search(source, query, start, end);
					this.sendJSONResponse(content, ctx.getResponse());

				} catch (Exception e) {
					this.sendBadRequest(ctx, e);

				}
			}
		} else if (type.equals("similar")) {
			String query = this.method.getAttribute("query");
			String name = this.method.getAttribute("name");
			if ((!methodName.equals("get")) || (query == null) || (name == null)) {
				this.sendNotImplemented(ctx);

			} else {
				try {
					/*
					 * JSON response
					 */

					String content = similar(source, query, name);
					this.sendJSONResponse(content, ctx.getResponse());

				} catch (Exception e) {
					this.sendBadRequest(ctx, e);

				}
			}
		} else if (type.equals("checkout")) {

			// access post data
			String data = this.getRequestData(ctx);
			
			System.out.println("====> SearchImpl.checkout> data.len: " + data.length());
				
			if ((!methodName.equals("apply")) || (data == null)) {
				this.sendNotImplemented(ctx);

			} else {
				try {
					/*
					 * JSON response
					 */

					String content = checkout(source, data);
					this.sendJSONResponse(content, ctx.getResponse());

				} catch (Exception e) {
					this.sendBadRequest(ctx, e);

				}
			}
		} else if (type.equals("download")) {

			// access post data when send with doApply
			//	String data = this.getRequestData(ctx);
			
			// access post data, from named FORM field
			String data = ctx.getRequest().getParameter("hiddenField");
			
			System.out.println("====> SearchImpl.download> data.len: " + data.length());
				
			
			if ((!methodName.equals("apply")) || (data == null)) {
				this.sendNotImplemented(ctx);

			} else {
				try {
					/*
					 * JSON response
					 */

					byte[] bytes = download(source, data);
					this.sendZIPResponse(bytes, ctx.getResponse());

				} catch (Exception e) {
					this.sendBadRequest(ctx, e);

				}
			}
		}
	}

	/**
	 * Term suggestion returns a JSON object as response
	 * 
	 * @param source 
	 * @param query
	 * @param start
	 * @param end
	 * @return
	 * @throws Exception
	 */
	private String suggest(String source, String query, String start, String end) throws Exception {
		String result = null;
		if (source.equals("wn"))
			result = new WNSearcher().suggest(query, start, end);
		else if (source.equals("scm"))
			result = new SCMSearcher().suggest(query, start, end);
		
		return result;
	}

	/**
	 * Documents search based on suggestion returns a JSON object as response
	 * 
	 * @param source
	 * @param query
	 * @param start
	 * @param end
	 * @return
	 * @throws Exception
	 */
	private String search(String source, String query, String start, String end) throws Exception {
		String result = null;
		if (source.equals("wn"))
			result = new WNSearcher().search(query, start, end);
		else if (source.equals("scm"))
			result = new SCMSearcher().search(query, start, end);
		
		return result;

	}

	/**
	 * Similar documents returns a JSON object as response
	 * 
	 * @param source
	 * @param query
	 * @param name
	 * @return
	 * @throws Exception
	 */
	private String similar(String source, String query, String name) throws Exception {
		String result = null;
		if (source.equals("wn"))
			result = new WNSearcher().similar(query, name);
		else if (source.equals("scm"))
			result = new SCMSearcher().similar(query, name);
		
		return result;

	}
	
	/**
	 * Computes a checkout HTML-form and embeds it in a JSON object as response
	 * 
	 * @param source
	 * @param data
	 * @return
	 * @throws Exception 
	 */
	private String checkout(String source, String data) throws Exception {
		String result = null;
		JSONArray jCheckout = new JSONArray(data);
		if (source.equals("wn"))
			result = new WNSearcher().checkout(jCheckout);
		else if (source.equals("scm"))
			result = new SCMSearcher().checkout(jCheckout);
		
		return result;
	}

	/**
	 * Download all Java Module from a checkout as a ZIP response
	 * 
	 * @param source
	 * @param data
	 * @return
	 * @throws Exception 
	 */
	private byte[] download(String source, String data) throws Exception {
		byte[] bytes = null;
		JSONArray jCheckout = new JSONArray(data);
		if (source.equals("wn"))
			// not implemented
			return null;
		else if (source.equals("scm"))
			bytes = new SCMSearcher().download(jCheckout);
		
		return bytes;
	}

	
}
