package com.leansoft.nano;


import com.leansoft.nano.impl.UtCacheHelper;
import com.leansoft.nano.transform.Transformer;
import com.leansoft.nano.util.TypeReflector;

import junit.framework.TestCase;

public abstract class NanoBaseUnitTest extends TestCase
{
   @Override
   protected void setUp() throws Exception {
      UtCacheHelper.clearCaches();
      Transformer.clearCaches();
      TypeReflector.clearCaches();
   }

   @Override
   protected void tearDown() throws Exception {
      UtCacheHelper.clearCaches();
      Transformer.clearCaches();
      TypeReflector.clearCaches();
   }

}
