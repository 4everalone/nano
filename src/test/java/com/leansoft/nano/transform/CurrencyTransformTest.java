package com.leansoft.nano.transform;

import java.util.Currency;
import java.util.Locale;

import com.leansoft.nano.NanoBaseUnitTest;
import com.leansoft.nano.transform.CurrencyTransform;

public class CurrencyTransformTest extends NanoBaseUnitTest {
	public void testCurrency() throws Exception {
	      Currency currency = Currency.getInstance(Locale.UK);
	      CurrencyTransform format = new CurrencyTransform();
	      String value = format.write(currency);
	      Currency copy = format.read(value);
	      
	      assertEquals(currency, copy);
	}
}
