package com.leansoft.nano.impl;

import com.leansoft.nano.annotation.schema.ElementSchema;
import com.leansoft.nano.util.FastStack;

/**
 * Helper class for XmlReader
 * 
 * @author bulldog
 *
 */
class XmlReaderHelper {

   public XmlReaderHelper(Class<?> bindClazz)
   {
      this.bindClazz = bindClazz;
   }
	
	public int depth = 0;
   
   public Object bindObject = null;
   
	public StringBuilder textBuilder = new StringBuilder();
	
	private static class ObjectHolder
	{
	   public Object obj;
	   public ElementSchema es;
	   public ObjectHolder(Object obj, ElementSchema es)
	   {
	      this.obj = obj;
	      this.es = es;
	   }
	}
	private FastStack<ObjectHolder> valueStack = new FastStack<ObjectHolder>(5);
	
	public int getValueStackSize()
	{
	   return valueStack.size();
	}
   public Object popFromValueStack()
   {
      return valueStack.pop().obj;
   }
   
   public Object peekFromValueStack()
   {
      return valueStack.peek().obj;
   }
   
   public void pushToValueStack(Object obj, ElementSchema es)
   {
      valueStack.push(new ObjectHolder(obj, es));
   }
   
   public boolean needToEncryptField(String fieldName)
   {
      
      if (valueStack.size()>0)
      {
         ElementSchema es = valueStack.peek().es;
         return es!= null && es.needToEncryptSubField(fieldName);
      }
      return false;
   }
   
   public boolean needToEncryptTopElement()
   {
      
      if (valueStack.size()>0)
      {
         ElementSchema es = valueStack.peek().es;
         if (es!=null && !es.isEncrypted() && valueStack.size()>1)
         {
            ElementSchema parentEs = valueStack.peek2nd().es;
            if ( parentEs != null && parentEs.needToEncryptSubField(es.getXmlName()))
            {
               es.setEncrypted(true);
            }
         }
         return es!= null && es.isEncrypted();
      }
      return false;
   }
   
   public Class<?> bindFaultClazz;
   public Class<?> bindClazz;
   
	public boolean isRoot() {
		return valueStack.size() == 1 && depth == 1;
	}
	
	public void clearTextBuffer() {
		int length = textBuilder.length();
		this.textBuilder.delete(0, length);
	}
}
