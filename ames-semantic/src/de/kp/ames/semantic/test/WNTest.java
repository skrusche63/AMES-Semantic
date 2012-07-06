package de.kp.ames.semantic.test;

import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Pointer;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.Synset;
import de.kp.ames.semantic.wn.WNProvider;
import junit.framework.TestCase;

public class WNTest extends TestCase {

	public void test() throws Exception {
		
		WNProvider provider = WNProvider.getInstance();
		
		String key = "00058608";
		POS wnPos = POS.NOUN;
		
		Synset synset = provider.getSynset(key, wnPos);
		System.out.println("---> " + synset.getGloss());

		Pointer[] pointers = synset.getPointers(PointerType.HYPERNYM);
		for (Pointer pointer:pointers) {
			
			Synset hypernym = pointer.getTargetSynset();
			System.out.println("---> " + hypernym.getGloss());
			
		}
	}
}
