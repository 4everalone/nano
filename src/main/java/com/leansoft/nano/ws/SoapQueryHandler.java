package com.leansoft.nano.ws;

import java.util.Map;

public interface SoapQueryHandler
{
   /**
    * NOTE: Do not copy the link of soapMessage!
    */
   public void handleRequest(String url, Map<String, String> httpHeaders, StringBuilder soapMessage);
   /**
    * NOTE: Do not copy the link of soapMessage!
    */
   public void handleResponse(int status, Map<String, String> httpHeaders, StringBuilder soapMessage);
}