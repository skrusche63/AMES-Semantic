package de.kp.ames.semantic;
/**
 * Copyright 2012 Dr. Krusche & Partner PartG. All rights reserved
 *
 * This file is part of the AMES-Semantic Project.
 *   
 */

import java.util.Enumeration;
import java.util.ResourceBundle;

public class Bundle extends ResourceBundle {

	public static final String BASE_NAME = "de.kp.ames.semantic.Settings";
    private static Bundle instance;

	private ResourceBundle bundle;

    protected Bundle() {
        bundle = ResourceBundle.getBundle(BASE_NAME);
    }

    public synchronized static Bundle getInstance() {
        if (instance == null) instance = new Bundle();
        return instance;
    }
	
	public ResourceBundle getBundle() {
		return bundle;
	}

    protected Object handleGetObject(String key) {
        return getBundle().getObject(key);
     }

    public final Enumeration<String> getKeys() {
        return getBundle().getKeys();
     }

}

