package com.leansoft.nano.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.leansoft.nano.annotation.schema.AttributeSchema;
import com.leansoft.nano.annotation.schema.ElementSchema;
import com.leansoft.nano.annotation.schema.AnyElementSchema;
import com.leansoft.nano.annotation.schema.RootElementSchema;
import com.leansoft.nano.annotation.schema.ValueSchema;
import com.leansoft.nano.exception.ReaderException;
import com.leansoft.nano.transform.StringTransform;
import com.leansoft.nano.transform.Transformer;
import com.leansoft.nano.util.StringUtil;
import com.leansoft.nano.util.TypeReflector;
import com.leansoft.nano.custom.types.AnyObject;

/**
 * SAX handler implementation for XmlSaxReader
 * 
 * @author bulldog
 * 
 */
class XmlReaderHandler extends DefaultHandler {
	
	private XmlReaderHelper helper;
	private StringTransform tr;
	
	public XmlReaderHandler(XmlReaderHelper helper) {
		this.helper = helper;
	}

	public XmlReaderHandler(XmlReaderHelper helper, StringTransform tr) {
		this(helper);
		this.tr = tr; 
	}

	private void populateAttributes(Object obj, Attributes attrs, MappingSchema ms) throws Exception {
		Map<String, AttributeSchema> xml2AttributeSchemaMapping = ms.getXml2AttributeSchemaMapping();
		for(int index = 0; index < attrs.getLength(); index++) {
			String attrName = attrs.getLocalName(index);
			
			AttributeSchema as = xml2AttributeSchemaMapping.get(attrName);
			if (as == null) continue;
			
			String attrValue = attrs.getValue(index);
			Field field = as.getField();
			Object value = Transformer.read(attrValue, field.getType());
			field.set(obj, value);
		}
	}
	
	public void startElement(String uri, String localName, String name, Attributes attrs) throws SAXException {
		try {
			helper.depth++;
			// clear the textBuilder
			helper.clearTextBuffer();
			
			if (helper.depth > helper.getValueStackSize() + 1) {
				// unexpected xml element, just ignore
				return;
			}
			
			Object obj = helper.peekFromValueStack();
			MappingSchema ms = MappingSchema.fromObject(obj);
			if(helper.isRoot()) { // first time root element mapping
				RootElementSchema res = ms.getRootElementSchema();
				String xmlName = res.getXmlName();
//				String namespace = res.getNamespace();
				// validation only for root element
//				String srcXmlFullname = StringUtil.isEmpty(uri)?localName:"{" + uri + "}#" + localName;
//				String targetXmlFullname = StringUtil.isEmpty(namespace)?xmlName:"{" + namespace + "}#" + xmlName;
//				if (!srcXmlFullname.equals(targetXmlFullname)) {
//					throw new ReaderException("Root element name mismatch, " + targetXmlFullname + " != " + srcXmlFullname);
//				}
				
				// simple validation only for root element
				if (!xmlName.equalsIgnoreCase(localName)) {
					throw new ReaderException("Root element name mismatch, " + localName + " != " + xmlName);
				}
				
				if (attrs != null && attrs.getLength() > 0) {
					this.populateAttributes(obj, attrs, ms);
				}
			} else { // sub element mapping
				Map<String, Object> xml2SchemaMapping = ms.getXml2SchemaMapping();
				Object schema = xml2SchemaMapping.get(localName);
				if (schema != null && schema instanceof ElementSchema) {
					ElementSchema es = (ElementSchema)schema;
					
					Field field = es.getField();
					Class<?> type = field.getType();
					if (es.isList()) {
						type = es.getParameterizedType();
					}
					if (!Transformer.isPrimitive(type)) {
						MappingSchema newMs = MappingSchema.fromClass(type);
						Constructor con = null;
						try {
							con = TypeReflector.getConstructor(type);
						} catch (NoSuchMethodException nsme) {
							throw new ReaderException("No-arg constructor is missing, type = " + type.getName());
						}
						Object newObj = con.newInstance();
						if (attrs != null && attrs.getLength() > 0) {
							this.populateAttributes(newObj, attrs, newMs);
						}
						
						helper.pushToValueStack(newObj, es);
					}
					
				}
            else if (schema == null && ms.getAnyElementSchema() != null)
            {
               Class<?> bindClazz = helper.bindClazz;
               MappingSchema newMs = null;
               boolean soap11 = obj instanceof com.leansoft.nano.soap11.Body;
               boolean soap12 = obj instanceof com.leansoft.nano.soap12.Body;
               if (soap11 || soap12)
               {
                  newMs = MappingSchema.fromClass(bindClazz);
                  String xmlName = newMs.getRootElementSchema().getXmlName();
                  if (!xmlName.equalsIgnoreCase(localName))
                  {
                     newMs = MappingSchema.fromClass(helper.bindFaultClazz);
                     xmlName = newMs.getRootElementSchema().getXmlName();
                     if (!xmlName.equalsIgnoreCase(localName))
                     {
                        throw new ReaderException("Root response element name mismatch, " + localName + " != " + xmlName);
                     }
                     bindClazz = helper.bindFaultClazz;
                  }
               }
               else
               {
                  bindClazz = AnyObject.class;
                  newMs = MappingSchema.fromClass(bindClazz);
               }
               
               Constructor con = null;
               try {
                  con = TypeReflector.getConstructor(bindClazz);
               } catch (NoSuchMethodException nsme) {
                  throw new ReaderException("No-arg constructor is missing, type = " + bindClazz.getName());
               }
               Object newObj = con.newInstance();
               if (attrs != null && attrs.getLength() > 0)
               {
                  this.populateAttributes(newObj, attrs, newMs);
               }
               
               helper.bindObject = newObj;
               Field anyField = ms.getAnyElementSchema().getField();
               if (!anyField.isAccessible())
               {
                  anyField.setAccessible(true);
               }
               
               List list = (List)anyField.get(obj);
               if (list == null)
               {
                  list = new ArrayList();
                  anyField.set(obj, list);
               }
               list.add(newObj);
               helper.pushToValueStack(newObj, null);
            }
			}
			
	    } catch (Exception ex) {
			throw new SAXException("Reading exception in startElement, " + ex.getMessage(), ex);
		}
	}
	
	 
	public void endElement(String uri, String localName, String name) throws SAXException {
		try {
			if (helper.depth > helper.getValueStackSize() + 1) {
				// unexpected xml element, just ignore
				helper.depth--;
				return;
			} else if (helper.depth == helper.getValueStackSize() + 1) { // handle primitive field
				Object obj = helper.peekFromValueStack();
				MappingSchema ms = MappingSchema.fromObject(obj);
				Map<String, Object> xml2SchemaMapping = ms.getXml2SchemaMapping();
				Object schema = xml2SchemaMapping.get(localName);
				if (schema != null && schema instanceof ElementSchema) {
					ElementSchema es = (ElementSchema)schema;
					Field field = es.getField();
					String xmlData = helper.textBuilder.toString();
					if (!StringUtil.isEmpty(xmlData)) {
						if (es.isList()) {
							Class<?> paramizedType = es.getParameterizedType();
							Object value = Transformer.read(xmlData, paramizedType);
							List list = (List)field.get(obj);
							if (list == null) {
								list = new ArrayList();
								field.set(obj, list);
							}
							list.add(value);
						} else {
						   if (tr != null && 
                           (  es.isEncrypted()
                            ||helper.needToEncryptField(localName)
                           ) 
                         )
                     {
						      xmlData =  tr.read(xmlData);
		               }						   
							Object value = Transformer.read(xmlData, field.getType());
							field.set(obj, value);
						}
					}
				}
			} else if (helper.depth == helper.getValueStackSize()) { // handle object field
			   boolean currentSubFieldEncrypted = helper.needToEncryptField(localName);
			   boolean currentElementEncrypted = helper.needToEncryptTopElement();
				Object obj = helper.popFromValueStack();
				MappingSchema ms = MappingSchema.fromObject(obj);
				
				if (helper.getValueStackSize() == 0) {  // the end
					helper.pushToValueStack(obj, null);
					helper.depth --;
					return;
				}
				
				ValueSchema vs = ms.getValueSchema();
				if (vs != null) {
					Field field = vs.getField();
					String xmlData = helper.textBuilder.toString();
               if (tr != null && !StringUtil.isEmpty(xmlData) &&
                     (
                        vs.isEncrypted()
                      ||currentElementEncrypted
                      ||currentSubFieldEncrypted
                      ||helper.needToEncryptField(localName)
                     )
                   )
                {
                  xmlData = tr.read(xmlData);//decrypt annotated field
                }
					if (!StringUtil.isEmpty(xmlData)) {
						Object value = Transformer.read(xmlData, field.getType());
						field.set(obj, value);
					}
				}
            else if (obj instanceof AnyObject)
            {
               String content = helper.textBuilder.toString();
               //here apply trick for special case where "content" field was listed to decrypt in enclosing object
               if (tr != null && (helper.needToEncryptField("content")||helper.needToEncryptTopElement()) )
               {
                  content = tr.read(content); 
               }
               
               ((AnyObject)obj).content = content;
            }
				
				Object parentObj = helper.peekFromValueStack();
				MappingSchema parentMs = MappingSchema.fromObject(parentObj);
				Map<String, Object> parentXml2SchemaMapping = parentMs.getXml2SchemaMapping();
				
				Object schema = parentXml2SchemaMapping.get(localName);
				if(schema != null && schema instanceof ElementSchema) {
					ElementSchema es = (ElementSchema)schema;
					Field field = es.getField();
					if (es.isList()) {
						List list = (List)field.get(parentObj);
						if (list == null) {
							list = new ArrayList();
							field.set(parentObj, list);
						}
						list.add(obj);
					} else {
						field.set(parentObj, obj);
					}
				}
				
			}
			
			helper.depth--;
    	} catch (Exception ex) {
    		throw new SAXException("Reading Exception in endElement, " + ex.getMessage(), ex);
    	}
	}
	
	public void characters(char[] ch, int start, int length) throws SAXException {
		String text = new String(ch, start, length);
		helper.textBuilder.append(text);
	}
}
