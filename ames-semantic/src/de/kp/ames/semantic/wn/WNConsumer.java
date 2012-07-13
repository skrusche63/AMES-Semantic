package de.kp.ames.semantic.wn;
/**
 * Copyright 2012 Dr. Krusche & Partner PartG. All rights reserved
 *
 * This file is part of the AMES-Semantic Project.
 *   
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Pointer;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;

import org.apache.solr.common.SolrInputDocument;

import de.kp.ames.semantic.solr.SolrConstants;
import de.kp.ames.semantic.solr.SolrProxy;
import de.kp.ames.semantic.wn.config.WNConstants;
import de.kp.ames.semantic.wn.data.WNLoader;

public class WNConsumer {
	
			
	/**
	 * @throws Exception
	 */
	public static void buildWNIndex() throws Exception {

	    BufferedReader reader = new BufferedReader(new InputStreamReader(WNLoader.load()));
	    String line;

        final HashSet<String> keys = new HashSet<String>();

        /*
         * http://wordnet.princeton.edu/man/prologdb.5WN.html
         * 
         * s(synset_id,w_num,'word',ss_type,sense_number,tag_count)
         * s(100058608,1,'elopement',n,1,0).
         */
	    while ((line = reader.readLine()) != null) {
	    	
	    	/* 
	    	 * A line starts with 's'
	    	 */
	    	if (!line.startsWith("s(")) throw new Exception("No synonyms provided.");
            
	    	/* 
	    	 * Parse line
	    	 */
	    	line = line.substring(2);
 
	    	/*
	    	 * Tokenize by ','
	    	 */
	    	String[] tokens = line.split(",");	    	
	    	keys.add(tokens[0]);
            
	    }
	    
	    /* 
	     * Finally build wordnet index in solr
	     */
	    buildWNIndex(keys);
	    
	}
	
	private static String beautify(String word) {
		return word.replace("_", " ");
	}
	
	/**
	 * This method creates a wordnet based search index entry, that is
	 * based on the WordNet-3.0 synonyms provided as a prolog file
	 * 
	 * @param synonyms
	 */
	private static void buildWNIndex(HashSet<String> keys) throws Exception {

		WNProvider provider = WNProvider.getInstance();
		
		Collection<SolrInputDocument> documents = new ArrayList<SolrInputDocument>();
		
		System.out.println("key count: " + keys.size());
		
		for (String key:keys) {
			
			String off = key.substring(1);
			String pos = key.substring(0,1);
			
			/*
			 * Retrieve synset from wordnet-3.0
			 */
			POS wnPos = WNConstants.posMap.containsKey(pos) ? WNConstants.posMap.get(pos) : null;
			
			if (wnPos == null) continue;
			
			Synset synset = provider.getSynset(off, wnPos);

			/*
			 * Hypernyms
			 */
			Pointer[] pointers = synset.getPointers(PointerType.HYPERNYM);
			if (pointers.length == 0) continue;
			
			String hypernym = pointers[0].getTargetSynset().getWord(0).getLemma();

			/*
			 * Description
			 */
			String gloss = synset.getGloss();
			
			/*
			 * Words
			 */
			Word[] words = synset.getWords();
			String synonyms = getSynonyms(words);
			
			for (Word word:words) {

				SolrInputDocument document = new SolrInputDocument();
				
				/* 
				 * category
				 */
				document.addField(SolrConstants.CATEGORY_FIELD, "sgwn"); // SUGGEST WORDNET

				/* 
				 * Identifier
				 * 		PartOfSpeach [1-4] : Synset-id [0-9]{8}: Number of word [0-9]+
				 */
				document.addField("id", WNConstants.posReverseMap.get(word.getPOS()) + ":" + synset.getKey() + ":" + word.getIndex());
				
				/* 
				 * Word
				 */
				document.addField(SolrConstants.WORD_FIELD, beautify(word.getLemma()));


				/*
				 * Description
				 */
				document.addField(SolrConstants.DESC_FIELD, gloss);
				
				/*
				 * Synonyms
				 */
				document.addField(SolrConstants.SYNONYM_FIELD, beautify(synonyms));
				
				/*
				 * Hypernym
				 */
				document.addField(SolrConstants.HYPERNYM_FIELD, beautify(hypernym));

				documents.add(document);
				
			}
			
		}
		
		System.out.println("doc count: " + documents.size());
		
		SolrProxy.getInstance().createEntries(documents);
		
	}

	private static String getSynonyms(Word[] words) {
		
		if (words.length == 1) {
			return "(Synonyms: " + words[0].getLemma() + ")";
		}
		
		String synonyms = "(Synonyms: " + words[0].getLemma();
		for (int i=1; i < words.length; i++) {
			synonyms += ", " + words[i].getLemma();
		}

		return synonyms + ")";

	}

}
