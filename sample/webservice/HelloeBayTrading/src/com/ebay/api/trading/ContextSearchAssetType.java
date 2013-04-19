// Generated by xsd compiler for android/java
// DO NOT CHANGE!
package com.ebay.api.trading;

import java.io.Serializable;
import com.leansoft.nano.annotation.*;

/**
 * 
 * Score and rank for a keyword identified for a web page.
 * 
 */
public class ContextSearchAssetType implements Serializable {

    private static final long serialVersionUID = -1L;

	@Element(name = "Keyword")
	@Order(value=0)
	public String keyword;	
	
	@Element(name = "Category")
	@Order(value=1)
	public CategoryType category;	
	
	@Element(name = "Ranking")
	@Order(value=2)
	public Integer ranking;	
	
    
}