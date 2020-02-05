package com.leansoft.nano.impl;

import java.io.Writer;

import org.xmlpull.v1.XmlSerializer;

import com.leansoft.nano.Format;
import com.leansoft.nano.annotation.Element;
import com.leansoft.nano.annotation.schema.ElementSchema;
import com.leansoft.nano.annotation.schema.RootElementSchema;
import com.leansoft.nano.exception.MappingException;
import com.leansoft.nano.exception.WriterException;
import com.leansoft.nano.util.StringUtil;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class SOAPWriter extends XmlPullWriter {
	
	static final String SOAP_PREFIX = "soapenv";
	static final String XSI_PREFIX = "xsi";
	static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
	static final String XSD_PREFIX = "xsd";
	static final String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
	static final String INNER_PREFIX = "n"; //tg fix
	
   private boolean qualifiedFromDefault = false;;
   
	public SOAPWriter() {
		super();
	}
	
	public SOAPWriter(Format format, boolean qualifiedFromDefault) {
		super(format, qualifiedFromDefault);
	}
	
	
	@Override
	public void write(Object source, Writer out) throws WriterException, MappingException {
		try {
			// entry validation
			validate(source, out);
			
			if (!(source instanceof com.leansoft.nano.soap11.Envelope) && !(source instanceof com.leansoft.nano.soap12.Envelope)) {
				throw new IllegalArgumentException("Can't write no-soap object of type : " + source.getClass().getName());
			}
	
			XmlSerializer serializer = factory.newSerializer();
			if (format.isIndent()) {
				try {
					serializer.setFeature(IDENT_PROPERTY, true);
				} catch (IllegalStateException ise) {
					serializer.setProperty(PROPERTY_SERIALIZER_INDENTATION, "    ");
				}
			}
			serializer.setOutput(out);
			serializer.startDocument(format.getEncoding(), null);
			
			MappingSchema ms = MappingSchema.fromObject(source);
			RootElementSchema res = ms.getRootElementSchema();
			String namespace = res.getNamespace();
			String xmlName = res.getXmlName();
			
			// set soap prefix
			serializer.setPrefix(SOAP_PREFIX, namespace);
			serializer.setPrefix(XSI_PREFIX, XSI_NAMESPACE);
			serializer.setPrefix(XSD_PREFIX, XSD_NAMESPACE);

			
			// set default namespace without prefix
			String innerNamespace = this.findInnerClassNamespace(source);
			if (!StringUtil.isEmpty(innerNamespace)) {
				if (serializer.getPrefix(innerNamespace, false) == null) {
					serializer.setPrefix(qualifiedFromDefault ? "" : INNER_PREFIX, innerNamespace); //tg fix
//					serializer.setPrefix("", innerNamespace); //tg fix
				}
			}
			this.addAllInnerNamespacesToPrefix(serializer, source);
			
			serializer.startTag(namespace, xmlName);
			this.writeObject(serializer, source, namespace);
			serializer.endTag(namespace, xmlName);
			
			serializer.endDocument();
		} catch (MappingException me) {
			throw me;
		} catch (IllegalArgumentException iae) {
			throw new WriterException("Entry validation failure", iae);
		} catch (Exception e) {
			throw new WriterException("Error to write/serialize object", e);
		}
	}
	
   private Object getInnerObjectFromEnvelope(Object obj) throws MappingException  {
		Object innerObject = null;
		if (obj instanceof com.leansoft.nano.soap11.Envelope) {
			com.leansoft.nano.soap11.Envelope envelope = (com.leansoft.nano.soap11.Envelope)obj;
			com.leansoft.nano.soap11.Body body = envelope.body;
			if (body != null && body.any != null && body.any.size() > 0) {
				innerObject = body.any.get(0);
			}
		} else if (obj instanceof com.leansoft.nano.soap12.Envelope) {
			com.leansoft.nano.soap12.Envelope envelope = (com.leansoft.nano.soap12.Envelope)obj;
			com.leansoft.nano.soap12.Body body = envelope.body;
			if (body != null && body.any != null && body.any.size() > 0) {
				innerObject = body.any.get(0);
			}
		}
      return innerObject;
   }
       
   
	private String findInnerClassNamespace(Object obj) throws MappingException  {
		Object innerObject = getInnerObjectFromEnvelope(obj);
		if (innerObject != null) {
			MappingSchema ms = MappingSchema.fromObject(innerObject);
			RootElementSchema res = ms.getRootElementSchema();
			
			return res.getNamespace();
		}
		
		return null;
	}

	private void gatherAllInnerNamespaces(MappingSchema ms, Set<String> foundNamespaces, Set<String> processedClasses) throws MappingException
	{
		processedClasses.add(ms.getType().getCanonicalName());//to prevent recursive call for the same class
		for (Map.Entry<String,Object> entry : ms.getField2SchemaMapping().entrySet()) {
			if (entry.getValue() instanceof ElementSchema) {
				ElementSchema elemSchema = (ElementSchema)entry.getValue();
				Element elemenAnnotation = elemSchema.getField().getAnnotation(Element.class);
				if (!StringUtil.isEmpty(elemenAnnotation.namespace())) {
					foundNamespaces.add(elemenAnnotation.namespace().intern());
				}
				if (elemSchema.getParameterizedType() != null &&
				    !processedClasses.contains(elemSchema.getParameterizedType().getCanonicalName())) {
					gatherAllInnerNamespaces(MappingSchema.fromClass(elemSchema.getParameterizedType()), foundNamespaces, processedClasses);
					processedClasses.add(elemSchema.getParameterizedType().getCanonicalName());
				} else
				if (!processedClasses.contains(elemSchema.getField().getType().getCanonicalName())) {
					gatherAllInnerNamespaces(MappingSchema.fromClass(elemSchema.getField().getType()), foundNamespaces, processedClasses);
					processedClasses.add(elemSchema.getField().getType().getCanonicalName());
				}
			}
		}
	}

	private void addAllInnerNamespacesToPrefix(XmlSerializer serializer, Object obj) throws MappingException, IOException {
		Object innerObject = getInnerObjectFromEnvelope(obj);
		if (innerObject != null) {
			LinkedHashSet<String> foundNamespaces = new LinkedHashSet<String>();
			HashSet<String> processedClasses = new HashSet<String>();

			MappingSchema ms = MappingSchema.fromObject(innerObject);
			gatherAllInnerNamespaces(ms, foundNamespaces, processedClasses);
			int nsNum=1;
			for (String ns: foundNamespaces) {
				serializer.setPrefix("n"+nsNum, ns);
				nsNum++;
			}
		}
	}

}
