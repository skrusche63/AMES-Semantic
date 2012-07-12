package de.kp.ames.semantic.service;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import de.kp.ames.semantic.http.RequestContext;
import de.kp.ames.semantic.http.RequestMethod;

/**
 * @author Stefan Krusche (krusche@dr-kruscheundpartner.de)
 *
 */

public class ServiceImpl implements Service {

	/*
	 * RequestMethod describes the method to be
	 * invoked by the actual request
	 */
	protected RequestMethod method;
	
	/**
	 * Constructor
	 */
	public ServiceImpl() {
	}

	
	/* (non-Javadoc)
	 * @see de.kp.ames.web.core.service.Service#setMethod(de.kp.ames.web.core.method.RequestMethod)
	 */
	public void setMethod(RequestMethod method) {
		this.method = method;		
	}
	
	/* (non-Javadoc)
	 * @see de.kp.ames.web.core.service.Service#getMethod()
	 */
	public RequestMethod getMethod() {
		return this.method;
	}

	/* (non-Javadoc)
	 * @see de.kp.ames.web.core.service.Service#processRequest(de.kp.ames.web.core.RequestContext)
	 */
	public void processRequest(RequestContext requestContext) {		
	}
	
	/* (non-Javadoc)
	 * @see de.kp.ames.web.core.service.Service#sendJSONResponse(java.lang.String, javax.servlet.http.HttpServletResponse)
	 */
	public void sendJSONResponse(String content, HttpServletResponse response) throws IOException {
		if (content == null) return;
		sendResponse(content, "application/json", response);		
	}

	
	/* (non-Javadoc)
	 * @see de.kp.ames.web.core.service.Service#sendResponse(java.lang.String, java.lang.String, javax.servlet.http.HttpServletResponse)
	 */
	public void sendResponse(String content, String mimetype, HttpServletResponse response) throws IOException {

		response.setStatus( HttpServletResponse.SC_OK );
		response.setCharacterEncoding("UTF-8");
		
		response.setContentType(mimetype);
		
		byte[] bytes = content.getBytes("UTF-8");
		response.setContentLength(bytes.length);

		OutputStream os = response.getOutputStream();

		os.write(bytes);
		os.close();

	}

	/**
	 * A helper method to retrieve the request data (POST) 
	 * in terms of a String representation
	 * 
	 * @param ctx
	 * @return
	 */
	protected String getRequestData(RequestContext ctx) {
		
		StringBuffer buffer = null;;

		try {
			BufferedReader reader = ctx.getRequest().getReader();
			buffer = new StringBuffer();
			
			String line;
			while ( (line = reader.readLine()) != null) {
				buffer.append(line);
			}

		} catch (IOException e) {
			// do nothing
		}

		return (buffer == null) ? null : buffer.toString();
		
	}
	
	
	/* (non-Javadoc)
	 * @see de.kp.ames.web.core.service.Service#sendBadResponse(java.lang.String, int, javax.servlet.http.HttpServletResponse)
	 */
	public void sendErrorResponse(String content, int errorStatus, HttpServletResponse response) throws IOException {

		response.setStatus(errorStatus);
		response.setCharacterEncoding("UTF-8");
		
		response.setContentType("text/plain");
		
		byte[] bytes = content.getBytes("UTF-8");
		response.setContentLength(bytes.length);

		OutputStream os = response.getOutputStream();

		os.write(bytes);
		os.close();
		
	}

	/**
	 * Send Bad Request response
	 * 
	 * @param ctx
	 * @param e
	 */
	protected void sendBadRequest(RequestContext ctx, Throwable e) {

		String errorMessage = "[" + this.getClass().getName() + "] " + e.getMessage();
		int errorStatus = HttpServletResponse.SC_BAD_REQUEST;
		
		try {
			sendErrorResponse(errorMessage, errorStatus, ctx.getResponse());

		} catch (IOException e1) {
			// do nothing
		}

	}

	/**
	 * Send Not Implemented response
	 * 
	 * @param ctx
	 */
	protected void sendNotImplemented(RequestContext ctx) {

		String errorMessage = "[" + this.getClass().getName() + "] Required parameters not provided.";
		int errorStatus = HttpServletResponse.SC_NOT_IMPLEMENTED;
		
		try {
			sendErrorResponse(errorMessage, errorStatus, ctx.getResponse());

		} catch (IOException e1) {
			// do nothing
		}

	}


	
}
