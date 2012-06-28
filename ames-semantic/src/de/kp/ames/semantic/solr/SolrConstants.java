package de.kp.ames.semantic.solr;
/**
 * Copyright 2012 Dr. Krusche & Partner PartG. All rights reserved
 *
 * This file is part of the AMES-Semantic Project.
 *   
 */

public class SolrConstants {

	public static String LEX_ID 		= "urn:oasis:names:tc:ebxml-regrep:LEX";
	public static String LEX_ID_WordNet = LEX_ID + ":WordNet";

	public static String WN_PREFIX = "WN-";

	/* 
	 * A predfined index field that holds all the terms
	 * that have been assigned to a certain synset key 
	 */
	public static String WORD_FIELD = "word_kpg";
	
	/* 
	 * A predefined index field to hold the different
	 * lexical sources that are assigned to the search
	 * index
	 */
	public static String SOURCE_FIELD = "source_kps";

}
