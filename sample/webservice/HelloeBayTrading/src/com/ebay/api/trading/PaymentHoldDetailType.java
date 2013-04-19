// Generated by xsd compiler for android/java
// DO NOT CHANGE!
package com.ebay.api.trading;

import java.io.Serializable;
import com.leansoft.nano.annotation.*;
import java.util.List;
import java.util.Date;

/**
 * 
 * This type defines the <b>PaymentHoldDetails</b> container, which
 * consists of information related to the payment hold on the order, including the
 * reason why the buyer's payment for the order is being held, the expected
 * release date of the funds into the seller's account, and possible action(s) the
 * seller can take to expedite the payout of funds into their account.
 * 
 */
public class PaymentHoldDetailType implements Serializable {

    private static final long serialVersionUID = -1L;

	@Element(name = "ExpectedReleaseDate")
	@Order(value=0)
	public Date expectedReleaseDate;	
	
	@Element(name = "RequiredSellerActionArray")
	@Order(value=1)
	public RequiredSellerActionArrayType requiredSellerActionArray;	
	
	@Element(name = "NumOfReqSellerActions")
	@Order(value=2)
	public Integer numOfReqSellerActions;	
	
	@Element(name = "PaymentHoldReason")
	@Order(value=3)
	public PaymentHoldReasonCodeType paymentHoldReason;	
	
	@AnyElement
	@Order(value=4)
	public List<Object> any;	
	
    
}