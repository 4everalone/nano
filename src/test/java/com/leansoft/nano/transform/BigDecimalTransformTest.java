package com.leansoft.nano.transform;

import java.math.BigDecimal;

import com.leansoft.nano.NanoBaseUnitTest;
import com.leansoft.nano.transform.BigDecimalTransform;

public class BigDecimalTransformTest extends NanoBaseUnitTest {
    public void testBigDecimal() throws Exception {
	      BigDecimal decimal = new BigDecimal("1.1");
	      BigDecimalTransform format = new BigDecimalTransform();
	      String value = format.write(decimal);
	      BigDecimal copy = format.read(value);
	      
	      assertEquals(decimal, copy);     
    }
}
