package com.leansoft.nano.impl;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import com.leansoft.nano.NanoBaseUnitTest;
import com.leansoft.nano.annotation.AnyElement;
import com.leansoft.nano.annotation.Attribute;
import com.leansoft.nano.annotation.Element;
import com.leansoft.nano.annotation.Order;
import com.leansoft.nano.annotation.Value;
import com.leansoft.nano.custom.types.AnyObject;
import com.leansoft.nano.exception.MappingException;
import com.leansoft.nano.exception.ReaderException;
import com.leansoft.nano.exception.WriterException;
import com.leansoft.nano.transform.StringTransform;
import com.leansoft.nano.transform.Transformable;
import com.leansoft.nano.transform.Transformer;
import com.leansoft.nano.util.Base64;

public class EncryptedFieldsTest extends NanoBaseUnitTest 
{

   /* this struct is generated from  xsd structure
   <xsd:complexType name="DateTimeMixed" mixed="true">
      <xsd:sequence>
         <xsd:element name="date" type="tns:date" minOccurs="0" maxOccurs="1"/>
         <xsd:element name="time" type="tns:time" minOccurs="0" maxOccurs="1"/>
      </xsd:sequence>
      <xsd:attribute name="nil" type="xsd:boolean" use="optional" default="false"/>
   </xsd:complexType>
   if we have mixed = true it means, that we can have something like that
   <DateOfBirth nil="false"> 
      <date nil="false">1955-08-08</date> 
   </DateOfBirth> 
   or
   <DateOfBirth nil="false"> 
      <date nil="false">1955-08-08</date> 
      <time nil="false">16:01</date> 
   </DateOfBirth> 
   or
   <DateOfBirth nil="false">
      1955-08-08 
      <time nil="false">16:01</date> 
   </DateOfBirth> 
   or just 
   <DateOfBirth nil="false">
      1955-08-08 
   </DateOfBirth> 
    * */
  private static class DateTimeMixed implements Serializable 
  {
     private static final long serialVersionUID = -1L;

     @AnyElement
     @Order(value=0)
     public List<Serializable> content;  
     
     @Attribute  
     @Order(value=1)
     public Boolean nil;
     
     @Element 
     @Order(value=2)
     public String justField;
     
     @Element(name = "SecretField") 
     @Order(value=3)
     public String secretField;
     
      
  }
   
   public static class PatientInfoTST implements Serializable 
   {
      private static final long serialVersionUID = -1L;
      
      @Element(name = "SecretField")
      @Order(value=1)
      public String secretField;
      
      @Element 
      @Order(value=2)
      public String regularField;
      
      @Element(sub_fields_to_encrypt = "PatComment,DateOfBirth,LastNameXml")
      @Order(value=3)
      public PersonInfoTST personInfo;

      @Element(encrypted = true)
      private Date dob;
      
      @Element(encrypted = true)
      private SpecValueStruct spcval;

      @Element(encrypted = true)
      private BooleanValue bb;
      
      @Element 
      @Order(value=2)
      public String verySecretField;

      @Element(encrypted = true)
      @Order(value=4)
      public CommentItemType comment1;
   }
   
   public static class PersonInfoTST implements Serializable 
   {
      private static final long serialVersionUID = -1L;

      @Element(name = "SecretField")
      @Order(value=1)
      public String secretField;
      
      @Element 
      @Order(value=2)
      public String regularField;

      @Element(name = "LastNameXml") 
      @Order(value=3)
      public String lastName;
      
      @Element(name = "PatComment")
      @Order(value=4)
      public CommentItemType comment;
      
      @Element(name = "DateOfBirth")
      @Order(value=5)
      public DateTimeMixed dob;
   }
   
   @com.leansoft.nano.annotation.RootElement(name="UploadTubeInfo")
   public static class UploadTubeInfoTST implements Serializable 
   {
      private static final long serialVersionUID = -1L;
      @Element(name = "DateOfBirth", sub_fields_to_encrypt = "SecretField", encrypted = true) 
      public DateTimeMixed dateOfBirth;

      @Element(name = "patientInfo", sub_fields_to_encrypt = "SecretField") 
      public PatientInfoTST patientInfo;
   }

  private static class CommentItemType implements Serializable 
  {
     
     private static final long serialVersionUID = -1L;
     @Value
     @Order(value=0)
     public String somevalue; 
     
     @Attribute(name = "OverrideMode")
     @Order(value=1)
     public boolean overrideMode; 
     
     @Attribute  
     @Order(value=2)
     public String date;  
     
     @Attribute  
     @Order(value=3)
     public String time;  
     
     @Attribute  
     @Order(value=4)
     public String tech;  
     
     @Attribute  
     @Order(value=5)
     public Boolean hidden;  
   }
  
   private static class BooleanValue  implements Serializable 
   {
      private static final long serialVersionUID = -1L;
      @Value
      @Order(value=0)
      public Boolean somevalue;
      
   }
  
   
  
   //@RootElement(name="pat")
   private static class Patient 
   {
      // no annotation
      public String id;

      @com.leansoft.nano.annotation.Element(encrypted = true)
      public CommentItemType tubeComment;
   
      @Element(encrypted = true)
      public String name;
      
      @Element(name="description")
      public String desc;
   }

   // secret AES 128 bit key
   private static final byte[] raw = {17, -21, -7, 82, 4, -59, -19, 99, -3, -81, 1, -78, 120, -14, -64, 7};


  private final class FieldEncryptor extends StringTransform
  {
     private Cipher _do;
     private Cipher _undo;
     public FieldEncryptor()
     {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        try
        {
           _do = Cipher.getInstance("AES");
           _do.init(Cipher.ENCRYPT_MODE, skeySpec);
           _undo = Cipher.getInstance("AES");
           _undo.init(Cipher.DECRYPT_MODE, skeySpec);
        }
        catch (InvalidKeyException ex) {
           //LOG.error("Invalid key", ex);
        } catch (NoSuchAlgorithmException ex) {
           //LOG.error("No such algorithm", ex);
        } catch (NoSuchPaddingException ex) {
           //LOG.error("No such padding exception", ex);
        }
     }
     @Override
     public String read(String value) throws Exception 
     {
        byte[] decodedValue = Base64.decode(value);
        byte[] decValue = _undo.doFinal(decodedValue);
        return new String(decValue);
     }
     @Override 
     public String write(String value) throws Exception 
     {
        return Base64.encode(_do.doFinal(value.getBytes()));
     }

  };

  public static class PersonInfo implements Serializable 
  {
     private static final long serialVersionUID = -1L;
     @com.leansoft.nano.annotation.Element(name = "DateOfBirth", sub_fields_to_encrypt="content" )
     public DateTimeMixed dateOfBirth;
  }

  public void atestParseMixedStructure() throws ReaderException, MappingException
  {
     String inputXml = "<PersonInfo>\n" + 
                      "  <DateOfBirth nil=\"false\">\n" + 
                      "    <date nil=\"false\">1955-08-08</date>\n" + 
                      "  </DateOfBirth>\n"+
                      "</PersonInfo>";
     
     XmlSAXReader reader = new XmlSAXReader(new FieldEncryptor());
     PersonInfo pat = (PersonInfo)reader.read(PersonInfo.class, inputXml);
     assertEquals(1, pat.dateOfBirth.content.size());


  }
  
  DateTimeMixed createMixedDate(String date, String time)
  {
     DateTimeMixed ret = new DateTimeMixed();
     ret.nil = false;
     ret.justField = "justFieldValue";
     ret.secretField = "secretFieldValue";
     ret.content = new ArrayList<Serializable>();
     AnyObject obj = new AnyObject();
     obj.content = date; //"2015-09-03";
     ret.content.add(obj);
     AnyObject objtime = new AnyObject();
     objtime.content = time; //"15:00";
     ret.content.add(objtime);
     return ret;
  }
  
  
  public static class CustomValue{
     private String data;
     
     public CustomValue(String data) {
        this.data = data;
     }
     
     public String toString() {
        return data;
     }
  }
  
  public static class CustomValueTransform implements Transformable<CustomValue> {
     final static String PREFIX="cust:";
     final static String SUFFIX=":cust";
     public CustomValue read(String value) throws Exception 
     {
        if (value.startsWith(PREFIX) && value.endsWith(SUFFIX))
        {
           return new CustomValue(value.substring(PREFIX.length(), value.length() - SUFFIX.length()));
        }
        throw new Exception("error pasring ");
     }

     public String write(CustomValue value) throws Exception {
        return PREFIX+value.toString()+SUFFIX;
     }
     
  }

  
  private static class SpecValueStruct
  {
     @Value
     public CustomValue val;
  }

  
  public void testCustomTransform() {
     assertFalse(Transformer.isTransformable(CustomValue.class));
     Transformer.register(CustomValue.class, new CustomValueTransform());
     assertTrue(Transformer.isTransformable(CustomValue.class));
  }

  public void testEncrypt() throws WriterException, MappingException
  {
     Transformer.register(CustomValue.class, new CustomValueTransform());

     UploadTubeInfoTST dtstruct = new UploadTubeInfoTST();
     dtstruct.dateOfBirth = createMixedDate("2015-09-03", "15:00");
     
     dtstruct.patientInfo = new PatientInfoTST();
     dtstruct.patientInfo.dob = new GregorianCalendar(2010, Calendar.SEPTEMBER, 16, 9, 42,22).getTime();
     dtstruct.patientInfo.spcval = new SpecValueStruct();
     dtstruct.patientInfo.spcval.val = new CustomValue("ff");
     dtstruct.patientInfo.regularField = "regularFieldInNested1_value";
     dtstruct.patientInfo.secretField = "secretFieldInNested1_value";
     dtstruct.patientInfo.verySecretField = "notQuiteVerySecretInNested1_value";
     dtstruct.patientInfo.comment1 = new CommentItemType();
     dtstruct.patientInfo.comment1.somevalue = "nested 1 comment";
     dtstruct.patientInfo.personInfo = new PersonInfoTST();
     dtstruct.patientInfo.personInfo.comment = new CommentItemType();
     dtstruct.patientInfo.personInfo.comment.date = "2015-09-03";
     dtstruct.patientInfo.personInfo.comment.somevalue = "a comment";
     dtstruct.patientInfo.personInfo.regularField = "regularFieldInNested2_value"; 
     dtstruct.patientInfo.personInfo.secretField = "notQuiteSecretFieldInNested2_value";
     dtstruct.patientInfo.personInfo.lastName = "verySecretInNested2_value";
     dtstruct.patientInfo.personInfo.dob = createMixedDate("2015-09-04", "12:58");
     
           
     XmlPullWriter writer = new XmlPullWriter(new FieldEncryptor());
     String str  = writer.write(dtstruct);
     String expected =
              "<?xml version=\"1.0\" encoding=\"utf-8\"?><UploadTubeInfo>\n" + 
              "    <DateOfBirth nil=\"false\">\n" + 
              "        <justField>justFieldValue</justField>\n" + 
              "        <SecretField>9XaxHER+xKhLPfIQUwTW27x/rQW8J47iCy1QjbAO/jw=</SecretField>\n" + 
              "        <anyObject>9dqvc4wEq/zHUn+IOxrfGw==</anyObject>\n" + 
              "        <anyObject>cYoSm5dkoRI7PKpIrd24yQ==</anyObject>\n" + 
              "    </DateOfBirth>\n" +
              "    <patientInfo>\n" + 
              "        <SecretField>uoPbayjAfWinCG+VhfM6/1U5oqE5qo7vcUSLa2wWOF8=</SecretField>\n" + 
              "        <regularField>regularFieldInNested1_value</regularField>\n" + 
              "        <verySecretField>notQuiteVerySecretInNested1_value</verySecretField>\n" + 
              "        <personInfo>\n" + 
              "            <SecretField>notQuiteSecretFieldInNested2_value</SecretField>\n" + 
              "            <regularField>regularFieldInNested2_value</regularField>\n" +
              "            <LastNameXml>N4Qs618/y+1QiDxChYuFAsCHZ2nmL3W/fjbAP+tSE0U=</LastNameXml>\n" + 
              "            <PatComment OverrideMode=\"false\" date=\"2015-09-03\">tQmfjMbZrs1Mv0WwTOPcHQ==</PatComment>\n" + 
              "            <DateOfBirth nil=\"false\">\n" + 
              "                <justField>justFieldValue</justField>\n" + 
              "                <SecretField>secretFieldValue</SecretField>\n" + 
              "                <anyObject>S4ugQ09c85GNXxtacLywXA==</anyObject>\n" + 
              "                <anyObject>tIlpMvQCipkuDA3Viu91Wg==</anyObject>\n" + 
              "            </DateOfBirth>\n" + 
              "        </personInfo>\n" + 
              "        <comment1 OverrideMode=\"false\">asAbd5Hfa7fuOHm9XYcHgrx/rQW8J47iCy1QjbAO/jw=</comment1>\n" + 
              "        <dob>Xyq2H7VONGpuCw3doUQ0MtAqab7VDNrBOqql+u4BhZ4=</dob>\n" +
              "        <spcval>PpPCAurA899R/PH36+/fOQ==</spcval>\n" +
              "    </patientInfo>\n" + 
              "</UploadTubeInfo>"; 
     assertEquals(expected, str);
        
  }
  
  
  public void testDecrypt() throws WriterException, MappingException, ReaderException
  {
     Transformer.register(CustomValue.class, new CustomValueTransform());
     String inputXml =
           "<?xml version=\"1.0\" encoding=\"utf-8\"?><UploadTubeInfo>\n" + 
           "    <DateOfBirth nil=\"false\">\n" + 
           "        <justField>justFieldValue</justField>\n" + 
           "        <SecretField>9XaxHER+xKhLPfIQUwTW27x/rQW8J47iCy1QjbAO/jw=</SecretField>\n" + 
           "        <anyObject>9dqvc4wEq/zHUn+IOxrfGw==</anyObject>\n" + 
           "        <anyObject>cYoSm5dkoRI7PKpIrd24yQ==</anyObject>\n" + 
           "    </DateOfBirth>\n" +
           "    <patientInfo>\n" + 
           "        <SecretField>uoPbayjAfWinCG+VhfM6/1U5oqE5qo7vcUSLa2wWOF8=</SecretField>\n" + 
           "        <regularField>regularFieldInNested1_value</regularField>\n" + 
           "        <verySecretField>notQuiteVerySecretInNested1_value</verySecretField>\n" + 
           "        <personInfo>\n" + 
           "            <SecretField>notQuiteSecretFieldInNested2_value</SecretField>\n" + 
           "            <regularField>regularFieldInNested2_value</regularField>\n" +
           "            <LastNameXml>N4Qs618/y+1QiDxChYuFAsCHZ2nmL3W/fjbAP+tSE0U=</LastNameXml>\n" + 
           "            <PatComment OverrideMode=\"false\" date=\"2015-09-03\">tQmfjMbZrs1Mv0WwTOPcHQ==</PatComment>\n" + 
           "            <DateOfBirth nil=\"false\">\n" + 
           "                <justField>justFieldValue</justField>\n" + 
           "                <SecretField>secretFieldValue</SecretField>\n" + 
           "                <anyObject>S4ugQ09c85GNXxtacLywXA==</anyObject>\n" + 
           "                <anyObject>tIlpMvQCipkuDA3Viu91Wg==</anyObject>\n" + 
           "            </DateOfBirth>\n" + 
           "        </personInfo>\n" + 
           "        <comment1 OverrideMode=\"false\">asAbd5Hfa7fuOHm9XYcHgrx/rQW8J47iCy1QjbAO/jw=</comment1>\n" + 
           "        <dob>Xyq2H7VONGpuCw3doUQ0MtAqab7VDNrBOqql+u4BhZ4=</dob>\n" +
           "        <spcval>PpPCAurA899R/PH36+/fOQ==</spcval>\n" +
           "    </patientInfo>\n" + 
           "</UploadTubeInfo>"; 
        
        
     XmlSAXReader reader = new XmlSAXReader(new FieldEncryptor());
     UploadTubeInfoTST fromXmlDbStruct = (UploadTubeInfoTST)reader.read(UploadTubeInfoTST.class, inputXml);
     assertEquals(2, fromXmlDbStruct.dateOfBirth.content.size());
     assertEquals("2015-09-03",((AnyObject)fromXmlDbStruct.dateOfBirth.content.get(0)).content);
     assertEquals("15:00", ((AnyObject)fromXmlDbStruct.dateOfBirth.content.get(1)).content);

     assertEquals(false, fromXmlDbStruct.dateOfBirth.nil.booleanValue());
     assertEquals("justFieldValue", fromXmlDbStruct.dateOfBirth.justField);
     assertEquals("secretFieldValue", fromXmlDbStruct.dateOfBirth.secretField);
     
     assertEquals("regularFieldInNested1_value", fromXmlDbStruct.patientInfo.regularField);
     assertEquals("secretFieldInNested1_value", fromXmlDbStruct.patientInfo.secretField);
     assertEquals("notQuiteVerySecretInNested1_value", fromXmlDbStruct.patientInfo.verySecretField);
     assertEquals("nested 1 comment", fromXmlDbStruct.patientInfo.comment1.somevalue) ;
     assertEquals("2015-09-03", fromXmlDbStruct.patientInfo.personInfo.comment.date);
     assertEquals("a comment", fromXmlDbStruct.patientInfo.personInfo.comment.somevalue);
     assertEquals("regularFieldInNested2_value", fromXmlDbStruct.patientInfo.personInfo.regularField) ; 
     assertEquals("notQuiteSecretFieldInNested2_value", fromXmlDbStruct.patientInfo.personInfo.secretField) ;
     assertEquals("verySecretInNested2_value", fromXmlDbStruct.patientInfo.personInfo.lastName);
     assertEquals(2, fromXmlDbStruct.patientInfo.personInfo.dob.content.size());
     assertEquals("2015-09-04",((AnyObject)fromXmlDbStruct.patientInfo.personInfo.dob.content.get(0)).content);
     assertEquals("12:58", ((AnyObject)fromXmlDbStruct.patientInfo.personInfo.dob.content.get(1)).content);
     assertEquals("ff", fromXmlDbStruct.patientInfo.spcval.val.data );
     assertEquals(new GregorianCalendar(2010, Calendar.SEPTEMBER, 16, 9, 42,22).getTime(), fromXmlDbStruct.patientInfo.dob);
   }
   
   public void testEncryptEmptyComment() throws WriterException, MappingException
   {
      Patient pat = new  Patient();
      pat.tubeComment = new CommentItemType();
      pat.tubeComment.somevalue = null;
      pat.tubeComment.overrideMode = false;
      pat.tubeComment.hidden = false;      
      XmlPullWriter writer = new XmlPullWriter();
      String str = writer.write(pat);
      String expected = 
               "<?xml version=\"1.0\" encoding=\"utf-8\"?><patient>\n" + 
               "    <tubeComment OverrideMode=\"false\" hidden=\"false\" />\n" + 
               "</patient>";
      assertEquals(expected, str);
   }

   public void testDecryptEmptyComment() throws WriterException, MappingException, ReaderException
   {
      String inputXml = 
            "<?xml version=\"1.0\" encoding=\"utf-8\"?><patient>\n" + 
            "    <tubeComment OverrideMode=\"false\" hidden=\"false\" />\n" + 
            "</patient>";
      XmlSAXReader reader = new XmlSAXReader(new FieldEncryptor());
      Patient pat = (Patient)reader.read(Patient.class, inputXml);
      assertNull(pat.tubeComment.somevalue);
   }
   
   
   public void testNoEncryption() throws WriterException, MappingException
   {
      Patient pat = new  Patient();
      pat.id = "id";
      pat.name = "John Doe";
      pat.desc = "description";
      pat.tubeComment = new CommentItemType();
      pat.tubeComment.somevalue = "some comment";
      pat.tubeComment.tech = "SCC";
      pat.tubeComment.time = "2015-11-22T12:33";
      XmlPullWriter writer = new XmlPullWriter();
      String str = writer.write(pat);
      String expected = 
               "<?xml version=\"1.0\" encoding=\"utf-8\"?><patient>\n" + 
               "    <description>description</description>\n" + 
               "    <name>John Doe</name>\n" + 
               "    <tubeComment OverrideMode=\"false\" time=\"2015-11-22T12:33\" tech=\"SCC\">some comment</tubeComment>\n" + 
               "</patient>";
      assertEquals(expected, str);
   }

   
   public void testNoDecryption() throws ReaderException, MappingException
   {
      String inputXml= 
            "<?xml version=\"1.0\" encoding=\"utf-8\"?><patient>\n" + 
            "    <description>description</description>\n" + 
            "    <name>John Doe</name>\n" + 
            "    <tubeComment OverrideMode=\"false\" time=\"2015-11-22T12:33\" tech=\"SCC\">some comment</tubeComment>\n" + 
            "</patient>";
      XmlSAXReader reader = new XmlSAXReader();
      Patient pat;
      pat = (Patient)reader.read(Patient.class, inputXml);
      assertEquals("John Doe", pat.name);
      assertEquals("description", pat.desc);
      assertEquals("some comment", pat.tubeComment.somevalue);
      assertEquals("SCC", pat.tubeComment.tech);
      assertEquals("2015-11-22T12:33", pat.tubeComment.time);
      assertNull(pat.id);
   }
}
