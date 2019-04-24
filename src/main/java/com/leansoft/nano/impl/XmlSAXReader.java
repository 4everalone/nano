package com.leansoft.nano.impl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.leansoft.nano.Format;
import com.leansoft.nano.IReader;
import com.leansoft.nano.exception.MappingException;
import com.leansoft.nano.exception.ReaderException;
import com.leansoft.nano.transform.StringTransform;
import com.leansoft.nano.transform.Transformer;
import com.leansoft.nano.util.TypeReflector;

/**
 * IReader implementation using Java SAX parser
 * 
 * @author bulldog
 *
 */
public class XmlSAXReader implements IReader {
	
    private SAXParserFactory spf;
	private Format format;
   private Class<?> bindClazz;
	private StringTransform tr=null;

	public XmlSAXReader() {
		this(new Format());
	}

	public XmlSAXReader(StringTransform tr) {
		this();
		this.tr = tr;
	}
   
	
	public XmlSAXReader(Format format) {
		this.format = format;
		spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
	}
   
   public XmlSAXReader(Format format, Class<?> bindClazz) {
		this(format);
      this.bindClazz = bindClazz;
	}

	public <T> T read(Class<? extends T> type, InputStream source)
			throws ReaderException, MappingException {
		try {
			return this.read(type, new InputStreamReader(source, format.getEncoding()));
		} catch (UnsupportedEncodingException e) {
			throw new ReaderException("Encoding is not supported", e);
		}
	}

	public <T> T read(Class<? extends T> type, String source)
			throws ReaderException, MappingException {
		return this.read(type, new StringReader(source));
	}

	public <T> T read(Class<? extends T> type, Reader source)
			throws ReaderException, MappingException {
		
		validate(type, source);
		
		try {
			
			SAXParser saxParser = spf.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			
			
			Constructor con = null;
			try {
				con = TypeReflector.getConstructor(type);
			} catch (NoSuchMethodException nsme) {
				throw new ReaderException("No-arg contructor is missing, type = " + type.getName());
			}
			Object obj = con.newInstance();
         
         XmlReaderHelper helper = new XmlReaderHelper(bindClazz);
         if (type.getName().equals(com.leansoft.nano.soap11.Envelope.class.getName()) )
         {
            helper.bindFaultClazz = com.leansoft.nano.soap11.Fault.class;
         }
         else if (type.getName().equals(com.leansoft.nano.soap12.Envelope.class.getName()))
         {
            helper.bindFaultClazz = com.leansoft.nano.soap12.Fault.class;
         }
			helper.pushToValueStack(obj, null);
			
			XmlReaderHandler saxHandler = new XmlReaderHandler(helper, tr);
			xmlReader.setContentHandler(saxHandler);
			
			xmlReader.parse(new InputSource(source));
			
         
			if (helper.getValueStackSize() == 1) { // has one and only one object left on the stack
				return (T)helper.popFromValueStack(); // read is successful, just return the object
			} else {
				throw new ReaderException("Error to read/deserialize object, no result to return");
			}
		} catch (SAXException se) {
			if (se.getException() instanceof MappingException) {
				MappingException me = (MappingException)(se.getException());
				throw me;
			}
			if (se.getException() instanceof ReaderException) {
				ReaderException re = (ReaderException)(se.getException());
				throw re;
			}
			throw new ReaderException("Error to read/deserialize object", se);
		} catch (ReaderException re) {
			throw re;
		} catch (Exception e) {
			throw new ReaderException("Error to read/deserialize object", e);
		}
		
	}
	
	private <T> void validate(Class<? extends T> type, Reader reader) throws ReaderException {
		if (type == null) {
			throw new ReaderException("Can not read, type is null!");
		}
		
		if (reader == null) {
			throw new ReaderException("Reader is null!");
		}
		
		if (Transformer.isPrimitive(type)) {
			throw new ReaderException("Can not read primitive or enum type object, " +
					"only Nano bindable complex type object can be accepted!");
		}
	}

}
