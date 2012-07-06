package de.kp.ames.semantic.wn;
/**
 * Copyright 2012 Dr. Krusche & Partner PartG. All rights reserved
 *
 * This file is part of the AMES-Semantic Project.
 *   
 */

import de.kp.ames.semantic.wn.config.ConfigLoader;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.dictionary.Dictionary;

public class WNProvider {

	private static WNProvider instance = new WNProvider();
	private Dictionary wordnet = null;

	private WNProvider() {

		try {

			JWNL.initialize(ConfigLoader.load());
			wordnet = Dictionary.getInstance();
			
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
	}
	
	public static WNProvider getInstance() {
		if (instance  == null) instance = new WNProvider();
		return instance;
	}
	
	/**
	 * @param offset
	 * @param wnPos
	 * @return
	 * @throws Exception
	 */
	public Synset getSynset(String key, POS wnPos) throws Exception {	
		return wordnet.getSynsetAt(wnPos, Long.parseLong(key));
	}
}
