package com.leansoft.nano.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * This annotation maps a POJO field to an XML element
 * 
 * @author bulldog
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Element {

	
	/**
	 * The name of the XML element
	 * 
	 * @return name
	 */
	public String name() default "";
	
	/**
	 * Indicates if the string content of the field should
	 * be put in a CDATA container or not.
	 * 
	 * @return true or false
	 */
	public boolean data() default false;

	/**
	 * Indicates if the string content of the field should
	 * be encrypted
	 * 
	 * @return true or false
	 */
	public boolean encrypted() default false;
	
	/**
	 * Comma separated list of fields.
	 * Indicates what fields inside the structure we need to encrypt.
	 * Typically used when you have structure generated out of wsdl
	 * and can not add annotation to the needed fields inside the structure
	 * @return list of field names
	 */
	public String sub_fields_to_encrypt() default "";
}
