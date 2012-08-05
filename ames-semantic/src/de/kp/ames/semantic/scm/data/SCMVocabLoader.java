package de.kp.ames.semantic.scm.data;
/**
 * Copyright 2012 Dr. Krusche & Partner PartG. All rights reserved
 *
 * This file is part of the AMES-Semantic Project.
 *   
 */

import java.io.InputStream;

public class SCMVocabLoader {

	private static String SCM_VOCAB_FILE = "ames_vocab.json";
	
	public static InputStream load() {
		
		Class<?> loader = SCMVocabLoader.class;
		InputStream is = loader.getResourceAsStream(SCM_VOCAB_FILE);
		
		return is;
		
	}
	
}
