package org.nakedobjects.persistence.sql;

import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.object.value.Date;
import org.nakedobjects.object.value.FloatingPointNumber;
import org.nakedobjects.object.value.Logical;
import org.nakedobjects.object.value.Money;
import org.nakedobjects.object.value.Option;
import org.nakedobjects.object.value.Password;
import org.nakedobjects.object.value.Percentage;
import org.nakedobjects.object.value.SerialNumber;
import org.nakedobjects.object.value.TextString;
import org.nakedobjects.object.value.Time;
import org.nakedobjects.object.value.TimeStamp;
import org.nakedobjects.object.value.URLString;
import org.nakedobjects.object.value.WholeNumber;
import org.nakedobjects.utility.Configuration;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;


public class TypeMapper {
	private final static String PREFIX = "sql-object-store.typemapper";
	private static TypeMapper instance;
	private String id;
	private Hashtable types = new Hashtable();
	
	public static TypeMapper getInstance() {
		if(instance == null) {
			instance = new TypeMapper();
		}
		
		instance.id = "id";
		
		Configuration params = Configuration.getInstance();
		Properties properties = params.getPropertySubset(PREFIX);
		Enumeration e = properties.keys();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			if(key.equals("OID")) {
				instance.id = properties.getProperty("OID");
			} else {
				instance.types.put(key, properties.getProperty(key));
			}
		}
		return instance;
	}
	
	public String typeFor(String type) {
		return (String) types.get(type);
	}
	
    public String valueAsDBString(Field field, NakedValue value) throws ObjectStoreException {
        Class type = field.getType();

        if (TextString.class.isAssignableFrom(type)) {
            return "'" +((TextString) value).stringValue() + "'";
        }

        if (WholeNumber.class.isAssignableFrom(type)) {
            return String.valueOf(((WholeNumber) value).intValue());
        }

        if (Date.class.isAssignableFrom(type)) {
        	if(value == null) {
        		return "";
        	} else {
        		//return "'" + new java.sql.Date(((java.util.Date) value).getTime()).toString() + "'";
        		String ts = value.saveString();
        		if(ts.equals("NULL")) {
        			return ts;
        		}
            	String dbts = ts.substring(0,4) + "-" + ts.substring(4,6) + "-" + ts.substring(6,8);
            	return "'" + dbts + "'";
        	}
        }

        if (Money.class.isAssignableFrom(type)) {
            return String.valueOf(((Money) value).doubleValue());
        }

        if (Percentage.class.isAssignableFrom(type)) {
            return String.valueOf(((Percentage) value).doubleValue());
        }

        if (FloatingPointNumber.class.isAssignableFrom(type)) {
            return String.valueOf(((FloatingPointNumber) value).floatValue());
        }

        if (Time.class.isAssignableFrom(type)) {
            // converting to milliseconds
            //return "'" + new java.sql.Time(((Time) value).longValue() * 1000).toString() + "'";
        	String ts = value.saveString();
    		if(ts.equals("NULL")) {
    			return ts;
    		}
        	String dbts = ts.substring(0,2) + ":" + ts.substring(2,4);
        	return "'" + dbts + "'";
        }

        if (TimeStamp.class.isAssignableFrom(type)) {
      //      return "'" + new Timestamp(((java.util.Date) value).getTime()).toString() + "'";
        	String ts = value.saveString();
    		if(ts.equals("NULL")) {
    			return ts;
    		}
        	String dbts = ts.substring(0,4) + "-" + ts.substring(4,6) + "-" + ts.substring(6,8) + " " + ts.substring(8,10) + ":" + ts.substring(10,12);
        	return "'" + dbts + "'";
        }

        if (Option.class.isAssignableFrom(type)) {
            return "'" +((Option) value).stringValue() + "'";
        }

        if (Logical.class.isAssignableFrom(type)) {
       //     return "'" + (((Logical) value).booleanValue() ? "true" : "false") + "'";
            return "" + (((Logical) value).booleanValue() ? "1" : "0") + "";
        }

        if (URLString.class.isAssignableFrom(type)) {
            return "'" +((URLString) value).stringValue() + "'";
        }

        if (Password.class.isAssignableFrom(type)) {
            return "'" +((Password) value).stringValue() + "'";
        }
        
        if (SerialNumber.class.isAssignableFrom(type)) {
            return String.valueOf(((SerialNumber) value).longValue());
        }


        //LOG.error("No suitable column type for" + type);
        throw new ObjectStoreException("No suitable column type for" + type);
    }
    
    /*
    public void setFromDBColumn(Field field, NakedObject object, ResultSet result)
    	throws ObjectStoreException {
	
	String fieldName = columns[no];
	
	try {
        
        String value = result.getString(fieldName);
        if(value != null) {
        	if(field instanceof OneToOneAssociation) {
        		object.set(field.getName(), new SimpleOid(result.getLong(fieldName)));
        	} else if(field.getType() == Logical.class) {
        		object.set(field.getName(), value.toLowerCase());

        	} else if(field.getType() == Date.class) {
        		Date t = new Date();
        		t.setValue(result.getDate(fieldName));

        		object.set(field.getName(), t.saveString());
        		
        	} else if(field.getType() == TimeStamp.class) {
        		TimeStamp t = new TimeStamp();
        		t.setValue(result.getTimestamp(fieldName).getTime());

        		object.set(field.getName(), t.saveString());
        		
        	} else if(field.getType() == Time.class) {
        		Time t = new Time();
        		t.setValue(result.getTime(fieldName));

        		object.set(field.getName(), t.saveString());
        		
        	} else {
        		object.set(field.getName(), value);
        	}
        }
    } catch (SQLException e) {
//    	LOG.error("Error with results for " + fieldName, e);
        throw new ObjectStoreException(e);
    }
}
*/
    
	public String id() {
		return id;
	}
}


/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2004 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */