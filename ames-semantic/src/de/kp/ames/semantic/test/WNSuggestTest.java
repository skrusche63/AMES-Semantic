package de.kp.ames.semantic.test;

import junit.framework.TestCase;
import de.kp.ames.semantic.wn.WNSearcher;

public class WNSuggestTest extends TestCase {

	public void test() throws Exception {
		
		WNSearcher wnSearcher = new WNSearcher();
		System.out.println("prefix for house: \n" + wnSearcher.suggest("house", "0", "50"));

	}
}
