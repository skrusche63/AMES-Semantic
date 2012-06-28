package de.kp.ames.semantic.wn;
/**
 * Copyright 2012 Dr. Krusche & Partner PartG. All rights reserved
 *
 * This file is part of the AMES-Semantic Project.
 *   
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import de.kp.ames.semantic.LexConstants;
import de.kp.ames.semantic.solr.SolrConstants;
import de.kp.ames.semantic.solr.SolrProxy;
import de.kp.ames.semantic.wn.config.ConfigLoader;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.Dictionary;

public class WNProvider {

	private static WNProvider instance = new WNProvider();

	private Dictionary wordnet = null;
	private Map<String, POS> posMap = null;

	private WNProvider() {

		try {

			JWNL.initialize(ConfigLoader.load());
			wordnet = Dictionary.getInstance();
			
			this.initialize();
			
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
	}
	
	public static WNProvider getInstance() {
		if (instance  == null) instance = new WNProvider();
		return instance;
	}

	public JSONArray getJHypernyms(String offset, String pos) throws Exception {
		
		JSONArray jHypernyms = new JSONArray();

		Synset synset = getSynset(offset, pos);
		if (synset == null) return jHypernyms;
		
		return jHypernyms;

	}

	public JSONArray getJHyponyms(String offset, String pos) throws Exception {
		
		JSONArray jHyponyms = new JSONArray();

		Synset synset = getSynset(offset, pos);
		if (synset == null) return jHyponyms;
		
		return jHyponyms;

	}

	public JSONArray getJRelated(String offset, String pos) throws Exception {
		
		JSONArray jRelated = new JSONArray();

		Synset synset = getSynset(offset, pos);
		if (synset == null) return jRelated;
		
		return jRelated;

	}

	// SYNONYMS
	
	public JSONArray getJSynonyms(String term, String pos) throws Exception {

		JSONArray jSynonyms = new JSONArray();
		ArrayList<Synset> synsets = getSynsets(term, pos);
		
		if (synsets == null) return jSynonyms;
		
		for (Synset synset:synsets) {
			
			JSONObject jSynset = getJSynset(synset);
			if (jSynset != null) jSynonyms.put(jSynset);

		}
		
		return jSynonyms;
	
	}
	
	private void initialize() {

		this.posMap = new HashMap<String, POS>();
	
		this.posMap.put(LexConstants.POS_ADJECTIVE, POS.ADJECTIVE);
		this.posMap.put(LexConstants.POS_ADVERB, 	 POS.ADVERB);
		this.posMap.put(LexConstants.POS_NOUN, 	 POS.NOUN);
		this.posMap.put(LexConstants.POS_VERB, 	 POS.VERB);

	}
	
	private POS getPOS(String pos) {
		return this.posMap.get(pos);
	}
	
	private ArrayList<Synset> getSynsets(String term, String pos) throws Exception {
	
		ArrayList<Synset> synsets = new ArrayList<Synset>();
		
		/*
		 * synsets are retrieved in a 2-step strategy: first, the
		 * lexical part of the index is requested to retrieve all
		 * identifiers (synset ids) that refer to a certain entry
		 * in the WordNet lexical Database
		 * 
		 * second, the database is requested to retrieve the respective
		 * sense from the provided identifiers
		 */
		
		ArrayList<String> offsets = SolrProxy.getInstance().getWNKeys(SolrConstants.LEX_ID_WordNet, term);

		for (String offset:offsets) {
			
			Synset synset = getSynset(offset, pos);
			if (synset != null) synsets.add(synset);
			
		}

		return synsets;
		
	}
	
	private JSONObject getJSynset(Synset synset) throws Exception {
		
		JSONObject jSynset = new JSONObject();

		/* 
		 * Identifier
		 */
		String id = getSynsetID(synset);
		jSynset.put(LexConstants.UI_ID, id);
		
		/* 
		 * Terms
		 */
		jSynset.put(LexConstants.UI_TERMS, getJWords(synset));		
		
		/* 
		 * Definition
		 */
		jSynset.put(LexConstants.UI_DESC, synset.getGloss());
		return jSynset;
		
		
	}
	
	/**
	 * @param offset
	 * @param pos
	 * @return
	 * @throws Exception
	 */
	private Synset getSynset(String offset, String pos) throws Exception {
		
		String key = offset.substring(SolrConstants.WN_PREFIX.length());
		return wordnet.getSynsetAt(this.getPOS(pos), Long.parseLong(key));
		
	}
	
	private String getSynsetID(Synset synset) {		
		return SolrConstants.WN_PREFIX + synset.getKey();		
	}
	
	private JSONArray getJWords(Synset synset) {

		JSONArray jWords = new JSONArray();
		Word[] words = synset.getWords();

		for (Word word:words) {
			jWords.put(word.getLemma());
		}
		
		return jWords;
		
	}
}
