package org.nakedobjects.persistence.sql2.mysql;

import java.util.Enumeration;
import java.util.Hashtable;


public class Lookup {
	private Hashtable lookup;
	
	public Lookup(Hashtable lookup) {
		this.lookup = lookup;
	}

	public String lookup(String key) {
		return (key == null) ? "" : (String) lookup.get(key);
	}
	
	public String key(String lookupValue) {
		for(Enumeration e = lookup.keys(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			String value = lookup(key);
			
			if(value.equals(lookupValue)) {
				return key;
			}
		}
		
		return "";
	}
	
	public String toString() {
		return "Lookup " + lookup;
	}
}
