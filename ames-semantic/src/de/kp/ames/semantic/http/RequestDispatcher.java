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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.kp.ames.semantic.service.Service;
import de.kp.ames.semantic.util.BaseParam;

/**
 * @author Stefan Krusche (krusche@dr-kruscheundpartner.de)
 * 
 */

public class RequestDispatcher extends HttpServlet {

	/*
	 * generated serial number
	 */
	private static final long serialVersionUID = -7927476960641284338L;

	/*
	 * indicates whether registered services are initialized
	 */
	private boolean initialized = false;

	/*
	 * registered services
	 */
	private HashMap<String, Service> registeredServices;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		initializeServices();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		/*
		 * Retrieve service from the unique service identifier provided with the
		 * actual request url
		 */
		Service service = getService(request);

		RequestContext ctx = new RequestContext(request, response);
		ctx.setContext(getServletContext());

		/*
		 * Evaluate method
		 */

		if (service.getMethod() == null) {

			String errorMessage = "[" + service.getClass().getName() + "] Method not available.";
			int errorStatus = HttpServletResponse.SC_NOT_IMPLEMENTED;

			try {
				service.sendErrorResponse(errorMessage, errorStatus, ctx.getResponse());

			} catch (IOException e) {
				// do nothing
			}

			return;

		}

		/*
		 * Process GET request
		 */
		service.processRequest(ctx);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		/*
		 * Retrieve service from the unique service identifier provided with the
		 * actual request url
		 */
		Service service = getService(request);

		RequestContext ctx = new RequestContext(request, response);
		ctx.setContext(getServletContext());

		/*
		 * Evaluate method
		 */

		if (service.getMethod() == null) {

			String errorMessage = "[" + service.getClass().getName() + "] Method not available.";
			int errorStatus = HttpServletResponse.SC_NOT_IMPLEMENTED;

			try {
				service.sendErrorResponse(errorMessage, errorStatus, ctx.getResponse());

			} catch (IOException e) {
				// do nothing
			}

			return;

		}

		/*
		 * Process POST request
		 */
		service.processRequest(ctx);

	}

	/**
	 * The main method to register new services, i.e. additional core and
	 * business functionality
	 */
	private void initializeServices() {

		if (initialized == true)
			return;

		/*
		 * Temporary cache for all registered services
		 */
		registeredServices = new HashMap<String, Service>();

		/*
		 * Retrieve actual service configuration and initialize respective
		 * services
		 */
		ArrayList<BaseParam> serviceConfig = getServiceConfig();
		for (BaseParam param : serviceConfig) {

			String key = param.getKey();
			String val = param.getValue();

			Service service = createServiceForName(val);
			if (service == null)
				continue;

			registeredServices.put(key, service);

		}

	}

	/**
	 * Common method to retrieve a certain functional service and initiate it
	 * with the request parameters provided by the actual request
	 * 
	 * @param request
	 * @return
	 */
	private Service getService(HttpServletRequest request) {

		String path = request.getRequestURI();
		

		// determine service identifier from request URI
		int pos = path.lastIndexOf("/") + 1;
		String sid = path.substring(pos);

		//System.out.println("requestURI: " + path + " sid: " + sid);

		Service service = null;

		Set<String> keys = registeredServices.keySet();
		Iterator<String> iter = keys.iterator();

		while (iter.hasNext()) {

			String key = iter.next();
			if (sid.equals(key)) {
				service = registeredServices.get(key);
				break;
			}

		}

		if (service == null)
			return null;

		// invoke request method from request uri
		RequestMethod method;
		try {

			method = new RequestMethod(request);
			service.setMethod(method);

		} catch (Exception e) {

			e.printStackTrace();
			service = null;

		}

		return service;

	}

	/**
	 * Helper method to create a certain service from a given class name
	 * 
	 * @param serviceName
	 * @return
	 */
	private Service createServiceForName(String serviceName) {

		try {

			Class<?> clazz = Class.forName(serviceName);
			Constructor<?> constructor = clazz.getConstructor();

			Object instance = constructor.newInstance();
			return (Service) instance;

		} catch (Exception e) {
			e.printStackTrace();

		} finally {
		}

		return null;

	}

	private ArrayList<BaseParam> getServiceConfig() {

		ArrayList<BaseParam> services = new ArrayList<BaseParam>();

		/*
		 * Bulletin Service to support a posting between different communities
		 * of interest and their associated members
		 */
		services.add(new BaseParam("search", "de.kp.ames.semantic.service.SearchImpl"));


		return services;

	}
}
