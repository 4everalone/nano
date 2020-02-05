package com.leansoft.nano;

import java.util.List;

import com.leansoft.nano.annotation.Attribute;
import com.leansoft.nano.annotation.Element;
import com.leansoft.nano.annotation.RootElement;

class ToyPart {
	@com.leansoft.nano.annotation.Element(name = "Name")
	public String name;	
}

class DogToy  {

	@com.leansoft.nano.annotation.Element(name = "Name")
	public String name;	
	
	@com.leansoft.nano.annotation.Element(name = "Color")
	public String color;	
   
	@com.leansoft.nano.annotation.Element(name = "Parts" , namespace="a.third.namespace")
   public List<ToyPart> toyParts;
    
}
@RootElement(name="MyPet", namespace="a.b.c")
public class Bulldog {

	@Element(name="Name")
	public String name;
	
	@Attribute(name="Age")
	public Integer age;
	
	@Attribute(name="Color")
	public String color;
	
	@Element(name="Desc")
	public String desc;
	
	@Element(name="Children")
	public List<Bulldog> children;
   
	@Element(name="toys", namespace="d.e.f")
	List<DogToy> toys;

   @Element(name="favoriteToy", namespace="d.e.f")
	DogToy favoriteToy;
	
}
