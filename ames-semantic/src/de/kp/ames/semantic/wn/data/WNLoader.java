package de.kp.ames.semantic.wn.data;
/**
 * Copyright 2012 Dr. Krusche & Partner PartG. All rights reserved
 *
 * This file is part of the AMES-Semantic Project.
 *   
 */

import java.io.InputStream;

public class WNLoader {

	private static String WN_FILE = "wn_s.pl";
	
	public static InputStream load() {
		
		Class<?> loader = WNLoader.class;
		InputStream is = loader.getResourceAsStream(WN_FILE);
		
		return is;
		
	}
	
}
