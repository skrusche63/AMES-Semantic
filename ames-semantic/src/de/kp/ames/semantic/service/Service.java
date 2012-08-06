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

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import de.kp.ames.semantic.http.RequestContext;
import de.kp.ames.semantic.http.RequestMethod;

/**
 * @author Stefan Krusche (krusche@dr-kruscheundpartner.de)
 *
 */

public interface Service {

	/**
	 * @param method
	 */
	public void setMethod(RequestMethod method);
	
	/**
	 * @return
	 */
	public RequestMethod getMethod();

	/**
	 * @param requestContext
	 */
	public void processRequest(RequestContext requestContext);
	
	/**
	 * @param content
	 * @param status
	 * @param response
	 * @throws IOException
	 */
	public void sendErrorResponse(String content, int errorStatus, HttpServletResponse response) throws IOException;


	/**
	 * @param content
	 * @param response
	 * @throws IOException
	 */
	public void sendJSONResponse(String content, HttpServletResponse response) throws IOException;

	/**
	 * 
	 * @param bytes
	 * @param response
	 * @throws IOException
	 */
	public void sendZIPResponse(byte[] bytes, HttpServletResponse response) throws IOException;


	/**
	 * @param content
	 * @param mimetype
	 * @param response
	 * @throws IOException
	 */
	public void sendResponse(String content, String mimetype, HttpServletResponse response) throws IOException;

	/**
	 * 
	 * @param bytes
	 * @param mimetype
	 * @param response
	 * @throws IOException
	 */
	public void sendResponse(byte[] bytes, String mimetype, HttpServletResponse response) throws IOException;

}
