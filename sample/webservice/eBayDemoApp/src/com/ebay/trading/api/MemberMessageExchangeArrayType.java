// Generated by xsd compiler for android/java
// DO NOT CHANGE!
package com.ebay.trading.api;

import java.io.Serializable;
import com.leansoft.nano.annotation.*;
import java.util.List;

/**
 * 
 * Container for messages. Returned for GetMemberMessages if messages that meet the request criteria exist.
 * 
 */
public class MemberMessageExchangeArrayType implements Serializable {

    private static final long serialVersionUID = -1L;

	@Element(name = "MemberMessageExchange")
	@Order(value=0)
	public List<MemberMessageExchangeType> memberMessageExchange;	
	
    
}