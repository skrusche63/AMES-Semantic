package de.kp.ames.semantic.wn.config;

import java.util.HashMap;

import net.didion.jwnl.data.POS;

public class WNConstants {

	public static HashMap<String, POS> posMap;
	
	static {
	
		posMap = new HashMap<String, POS>();
	
		posMap.put("3", POS.ADJECTIVE);
		posMap.put("4", POS.ADVERB);
		posMap.put("1", POS.NOUN);
		posMap.put("2", POS.VERB);
	
	}

	public static HashMap<POS, String> posReverseMap;
	
	static {
	
		posReverseMap = new HashMap<POS, String>();
	
		posReverseMap.put(POS.ADJECTIVE, "3");
		posReverseMap.put(POS.ADVERB, "4");
		posReverseMap.put(POS.NOUN, "1");
		posReverseMap.put(POS.VERB, "2");
	
	}

	
}
