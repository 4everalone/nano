package com.leansoft.nano.util;

import com.leansoft.nano.NanoBaseUnitTest;
import com.leansoft.nano.util.LRUCache;

public class LRUCacheTest extends NanoBaseUnitTest {
	
	public void testLRUCache() {
		LRUCache<Integer, Integer> cache = new LRUCache<Integer, Integer>(10);
		assertTrue(cache.isEmpty());
		
		for(int i = 1; i <= 5; i++) {
			cache.put(i, i);
		}
		assertTrue(cache.size() == 5);
		
		for(int i = 6; i <= 15; i++) {
			cache.put(i, i);
		}
		assertTrue(cache.size() == 10);
		
		assertNull(cache.get(1));
		assertNull(cache.get(3));
		assertNull(cache.get(5));
	}

}
