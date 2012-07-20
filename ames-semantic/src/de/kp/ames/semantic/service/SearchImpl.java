package de.kp.ames.semantic.service;

import de.kp.ames.semantic.http.RequestContext;
import de.kp.ames.semantic.wn.WNSearcher;

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

public class SearchImpl extends ServiceImpl {

	/**
	 * Constructor
	 */
	public SearchImpl() {
		super();
	}

	/* (non-Javadoc)
	 * @see de.kp.ames.web.core.service.ServiceImpl#processRequest(de.kp.ames.web.http.RequestContext)
	 */
	public void processRequest(RequestContext ctx) {		

		String methodName = this.method.getName();
		if (methodName.equals("suggest")) {			
			/*
			 * Call suggest method
			 */
			String query = this.method.getAttribute("query");
			String start = this.method.getAttribute("_startRow");
			String end = this.method.getAttribute("_endRow");
			
			
			
			if ((query == null) || (start == null) || (end == null)) {
				this.sendNotImplemented(ctx);
				
			} else {
				
				try {
					/*
					 * JSON response
					 */
					String content = suggest(query, start, end);
					this.sendJSONResponse(content, ctx.getResponse());
					
				} catch (Exception e) {
					this.sendBadRequest(ctx, e);

				}
			}
		} else if (methodName.equals("search")) {
			/*
			 * Call searchmethod
			 */
			String query = this.method.getAttribute("query");
			String start = this.method.getAttribute("_startRow");
			String end = this.method.getAttribute("_endRow");
			
			if ((query == null) || (start == null) || (end == null)) {
				this.sendNotImplemented(ctx);
				
			} else {
				
				try {
					/*
					 * JSON response
					 */
					String content = search(query, start, end);
					this.sendJSONResponse(content, ctx.getResponse());
					
				} catch (Exception e) {
					this.sendBadRequest(ctx, e);

				}
			}			
		}

	}


	/**
	 * Term suggestion returns a JSON object as response
	 * 
	 * @param query
	 * @param start
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	private String suggest(String query, String start, String end) throws Exception {
		return new WNSearcher().suggest(query, start, end);
	}

	/**
	 * Term suggestion returns a JSON object as response
	 * 
	 * @param query
	 * @param start
	 * @param end
	 * @return
	 * @throws Exception
	 */
	private String search(String query, String start, String end) throws Exception {
		return new WNSearcher().search(query, start, end);
	}
	
}
