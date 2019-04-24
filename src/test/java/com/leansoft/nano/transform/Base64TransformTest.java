package com.leansoft.nano.transform;

import java.util.Arrays;

import com.leansoft.nano.NanoBaseUnitTest;
import com.leansoft.nano.transform.Base64Transform;

public class Base64TransformTest extends NanoBaseUnitTest {
	
	public void testBase64() throws Exception {
		byte[] data = "hello world!".getBytes();
		Base64Transform transform = new Base64Transform();
		
		String value = transform.write(data);
		byte[] copy = transform.read(value);
		System.out.println(new String(copy));

		assertTrue(Arrays.equals(data, copy));
		
		
	}

}
