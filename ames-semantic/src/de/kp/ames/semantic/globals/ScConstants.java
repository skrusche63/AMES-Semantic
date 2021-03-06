package de.kp.ames.semantic.globals;
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

/**
 * This class comprises all smart client specific
 * parameters, defined by SmartGwt 3.0, that are
 * used within HttpRequests
 * 
 * @author Stefan Krusche (krusche@dr-kruscheundpartner.de)
 *
 */
public class ScConstants {

	/*
	 * Paging request parameters
	 */
	public static String SC_LIMIT  = "_endRow";
	public static String SC_PARENT = "_parentId";
	public static String SC_START  = "_startRow";

	/*
	 * Common response parameters
	 */
	public static String SC_ICON = "icon";
	
	/*
	 * Grid response parameters
	 */
	public static String SC_DATA        = "data";
	public static String SC_STATUS 	 	= "status";
	public static String SC_STARTROW 	= "startRow";
	public static String SC_ENDROW   	= "endRow";
	public static String SC_TOTALROWS 	= "totalRows";

}
