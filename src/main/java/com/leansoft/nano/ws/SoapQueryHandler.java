package com.leansoft.nano.ws;

public interface SoapQueryHandler
{
   /**
    * NOTE: Do not copy the link of soapMessage!
    */
   public void handleRequest(String url, String httpHeaders, StringBuilder soapMessage);
   /**
    * NOTE: Do not copy the link of soapMessage!
    */
   public void handleResponse(int status, String httpHeaders, StringBuilder soapMessage);
}