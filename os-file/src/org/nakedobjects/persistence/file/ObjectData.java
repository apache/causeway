package org.nakedobjects.persistence.file;

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.SerialOid;
import org.nakedobjects.object.collection.InternalCollection;


/**
 * A logical collection of elements of a specified type
 */
public class ObjectData extends Data {
    private static final Logger LOG = Logger.getLogger(ObjectData.class);
   private Hashtable fields;

    public ObjectData(NakedObjectSpecification type, SerialOid oid) {
        super(type, oid);
        fields = new Hashtable();
    }

    public String toString() {
        return "ObjectData[type=" + getClassName() + ",oid=" + getOid() +
        ",fields=" + fields + "]";
    }

    public void set(String fieldName, Object oid) {
        if (oid == null) {
            fields.remove(fieldName);
        } else {
            fields.put(fieldName, oid);
        }
    }

    void saveValue(String fieldName, NakedValue nakedValue) {
    	if(nakedValue.isEmpty()) {
    		fields.remove(fieldName);
    	} else {
    		fields.put(fieldName, nakedValue.saveString());
    	}
   }
    
    public void set(String fieldName, String value) {
        fields.put(fieldName, value);
    }

    void restoreValue(String fieldName, NakedValue value) {
		String readValue = (String) fields.get(fieldName);
		LOG.debug("setting field " + fieldName + " with '" + readValue + "'");
		if(readValue == null) {
			value.clear();	
		} else {
			value.restoreString(readValue);
		}
    }

	public Object get(String fieldName) {
        return fields.get(fieldName);
    }

    public String value(String fieldName) {
    	return (String) get(fieldName);
    }
    
    public String id(String fieldName) {
    	Object field = get(fieldName);
		return field == null ? null : "" + ((SerialOid) field).getSerialNo();
    }
    
    void initCollection(SerialOid collectionOid, String fieldName) {
            fields.put(fieldName, new ReferenceVector(collectionOid));
    }
    
    void addElement(String fieldName, SerialOid elementOid) {
        if (!fields.containsKey(fieldName)) {
        	throw new RuntimeException();
        }

        ReferenceVector v = (ReferenceVector) fields.get(fieldName);
        v.add(elementOid);
    }

    ReferenceVector elements(String fieldName) {
        return (ReferenceVector) fields.get(fieldName);
    }

    public Enumeration fields() {
        return fields.keys();
    }
    
    void addValue(NakedValue fieldContent, String fieldName) {
    //	LOG.debug("adding value field " + fieldName +" " + fieldContent);
    	saveValue(fieldName, fieldContent);
    }

    void addAssociation(NakedObject fieldContent, String fieldName, boolean ensurePersistent) {
    	boolean notAlreadyPersistent = fieldContent != null && fieldContent.getOid() == null;
        if (ensurePersistent && notAlreadyPersistent) {
    		throw new IllegalStateException("Cannot save an object that is not persistent");
    	}
    //	LOG.debug("adding reference field " + fieldName +" " + fieldContent);		
    	set(fieldName, fieldContent == null ? null : fieldContent.getOid());
    }

    void addInternalCollection(InternalCollection collection, String fieldName, boolean ensurePersistent) {
    	if (ensurePersistent && collection != null && collection.getOid() == null) {
    		throw new IllegalStateException("Cannot save a collection that is not persistent");
    	}
    	
    	SerialOid coid = (SerialOid) collection.getOid();
    	//Enumeration e = collection.elements();
    	
    	initCollection(coid, fieldName);
    	
    	int size = collection.size();
    	
    	for(int i = 0; i < size; i++) {
    	    NakedObject element = collection.elementAt(i);
    	    
	    	   // 		LOG.debug("adding element to internal collection field " + fieldName +" " + element);
    	    Object elementOid = element.getOid();
    	    if (elementOid == null) {
    	        throw new IllegalStateException("Element is not persistent "+element);
    	    }
    	    
    	    addElement(fieldName, (SerialOid) elementOid);
    	}
    	
    	/*while (e.hasMoreElements()) {
    		NakedObject element = (NakedObject) e.nextElement();
   // 		LOG.debug("adding element to internal collection field " + fieldName +" " + element);
    		Object elementOid = element.getOid();
    		if (elementOid == null) {
    			throw new IllegalStateException("Element is not persistent "+element);
    		}
    		
    		addElement(fieldName, (SimpleOid) elementOid);
    	}
  */  }
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2003  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/
