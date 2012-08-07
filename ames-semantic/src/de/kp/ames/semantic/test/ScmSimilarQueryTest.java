package de.kp.ames.semantic.test;

import junit.framework.TestCase;
import de.kp.ames.semantic.scm.SCMSearcher;

public class ScmSimilarQueryTest extends TestCase {

	public void test() throws Exception {
		
//		String result = new SCMSearcher().similar("urn:de:kp:ames:scm:ADF:742b1e1a-34c5-4de7-a02a-9c5b27718f2e");
		String result = new SCMSearcher().hypertree("urn:de:kp:ames:scm:ADF:820cb193-0165-4924-bb1a-a5c536f763c8", "RssCache");
		
		System.out.println(result);
		

	}
}
