package de.kp.ames.semantic.wn.config;
/**
 * Copyright 2012 Dr. Krusche & Partner PartG. All rights reserved
 *
 * This file is part of the AMES-Semantic Project.
 *   
 */

import java.io.InputStream;

public class ConfigLoader {

	private static String CONFIG_FILE = "wn.file.properties.xml";
	
	public static InputStream load() {
		
		Class<?> loader = ConfigLoader.class;
		InputStream is = loader.getResourceAsStream(CONFIG_FILE);
		
		return is;
		
	}
	
}
