package org.nakedobjects.example.expenses;

import org.nakedobjects.application.Title;
import org.nakedobjects.application.valueholder.TextString;
import org.nakedobjects.application.valueholder.URLString;


public class Location {
   private final TextString name;
   private final URLString url;
   private final TextString streetAddress;
   private final TextString city;
   private final TextString postCode;
   private final TextString country;

    public static String fieldOrder() {
        return "name, street address, city, post code, country, url";
    }

   public Location() {
      name = new TextString();
      url = new URLString();
      streetAddress = new TextString();
      city = new TextString();
      postCode = new TextString();
      country = new TextString();
   }

   public void actionMap() {
      // get map from web and display/return
   }

   public Title title() {
   	if(!getName().isEmpty()){
			return getName().title();
		}

   	if(!getStreetAddress().isEmpty()){
   		return getStreetAddress().title();
   	}

   	if(!getCity().isEmpty()){
   		return getCity().title();
   	}

   	if(!getCountry().isEmpty()){
   		return getCountry().title();
   	}
   	
   	return new Title("New location");
   }

	public TextString getCity() {
		return city;
	}

	public TextString getCountry() {
		return country;
	}

	public TextString getName() {
		return name;
	}

	public TextString getPostCode() {
		return postCode;
	}

	public TextString getStreetAddress() {
		return streetAddress;
	}

	public URLString getUrl() {
		return url;
	}

}