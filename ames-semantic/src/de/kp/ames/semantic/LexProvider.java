package de.kp.ames.semantic;
/**
* Copyright 2012 Dr. Krusche & Partner PartG. All rights reserved
*
* This file is part of the AMES-Semantic Project.
*   
*/

import org.json.JSONArray;

import de.kp.ames.semantic.solr.SolrConstants;
import de.kp.ames.semantic.wn.WNProvider;

public class LexProvider {

	public LexProvider() {		
	}

	public JSONArray getHypernyms(String source, String synset, String pos) throws Exception {
     	return getJHypernyms(source, synset, pos);
	}

	private JSONArray getJHypernyms(String source, String synset, String pos) throws Exception {
		
		// WordNet is provided by a proprietary source
		// interface; we therefore must distinguish the
		// different semantic source here

		if (source.equals(SolrConstants.LEX_ID_WordNet)) {			
			return WNProvider.getInstance().getJHypernyms(synset, pos);
			
		} else {
			
			throw new Exception("The lexical source <" + source + "> is actually not supported.");
			
		}
		
	}

	public JSONArray getHyponyms(String source, String synset, String pos) throws Exception {
		return getJHyponyms(source, synset, pos);
	}

	private JSONArray getJHyponyms(String source, String synset, String pos) throws Exception {
		
		// WordNet is provided by a proprietary source
		// interface; we therefore must distinguish the
		// different semantic source here

		if (source.equals(SolrConstants.LEX_ID_WordNet)) {			
			return WNProvider.getInstance().getJHyponyms(synset, pos);
			
		} else {
			
			throw new Exception("The lexical source <" + source + "> is actually not supported.");
			
		}
		
	}

	public JSONArray getRelated(String source, String synset, String pos) throws Exception {
		return getJRelated(source, synset, pos);
	}

	private JSONArray getJRelated(String source, String synset, String pos) throws Exception {
		
		// WordNet is provided by a proprietary source
		// interface; we therefore must distinguish the
		// different semantic source here

		if (source.equals(SolrConstants.LEX_ID_WordNet)) {			
			return WNProvider.getInstance().getJRelated(synset, pos);
			
		} else {
			
			throw new Exception("The lexical source <" + source + "> is actually not supported.");
			
		}

	}

	public JSONArray getSynonyms(String source, String term, String pos) throws Exception {
		return getJSynonyms(source, term, pos);
	}

	private JSONArray getJSynonyms(String source, String term, String pos) throws Exception {
		
		// WordNet is provided by a proprietary source
		// interface; we therefore must distinguish the
		// different semantic source here

		if (source.equals(SolrConstants.LEX_ID_WordNet)) {			
			return WNProvider.getInstance().getJSynonyms(term, pos);
			
		} else {
			
			throw new Exception("The lexical source <" + source + "> is actually not supported.");
			
		}
	
	}

}
