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
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.solr.common.SolrInputDocument;

import de.kp.ames.semantic.solr.SolrConstants;
import de.kp.ames.semantic.solr.SolrProxy;
import de.kp.ames.semantic.wn.data.WNLoader;

public class WNConsumer {
	
	/**
	 * @throws Exception
	 */
	public static void buildWNIndex() throws Exception {

	    BufferedReader reader = new BufferedReader(new InputStreamReader(WNLoader.load()));
	    String line;

        final Map<String, ArrayList<String>> imap = new TreeMap<String, ArrayList<String>>();

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
	    	 * Determine synset identifier
	    	 */
	    	int comma = line.indexOf(',');
            String key = line.substring(0, comma);

            /* 
             * Determine quoted word
             */
            int quote1 = line.indexOf('\'');
            line = line.substring(quote1 + 1);
 
            int q2 = line.lastIndexOf('\'');
            String word = line.substring(0, q2).toLowerCase().replace("''", "'");

            if (! isWord(word)) continue;

            ArrayList<String> words = imap.get(key);
            if (words == null) {
                
            	words = new ArrayList<String>();
                words.add(word);
                
                imap.put(key, words);
            
            } else {
                words.add(word);

	   		}
            
	    }
	    
	    /* 
	     * Finally build wordnet index in solr
	     */
	    buildWNIndex(imap);
	    
	}

	/**
	 * Check whether a word contains only alphabetic 
	 * characters by checking it one character at a time.
	 * 
	 * @param s
	 * @return
	 */
	private static boolean isWord(String s) {
   
		int len = s.length();
		for (int i = 0; i < len; i++) {
			if (!Character.isLetter(s.charAt(i))) return false;
  
		}
    
		return true;
	
	}
	
	/**
	 * This method creates a wordnet based search index entry, that is
	 * based on the WordNet-3.0 synonyms provided as a prolog file
	 * 
	 * @param synonyms
	 */
	private static void buildWNIndex(Map<String, ArrayList<String>> synonyms) {

		Collection<SolrInputDocument> documents = new ArrayList<SolrInputDocument>();
		
		Set<String> keys = synonyms.keySet();
		for (String key:keys) {
			
			SolrInputDocument document = new SolrInputDocument();
			
			/* 
			 * Identifier
			 */
			document.addField("id", SolrConstants.WN_PREFIX + key);

			/* 
			 * Source
			 */
			ArrayList<String> sources = new ArrayList<String>();
			sources.add(SolrConstants.LEX_ID_WordNet);
			
			document.addField(SolrConstants.SOURCE_FIELD, sources);
			
			/* 
			 * Words
			 */
			document.addField(SolrConstants.WORD_FIELD, synonyms.get(key));
			documents.add(document);
			
		}
		
		SolrProxy.getInstance().createEntries(documents);
		
	}

}
