package org.nakedobjects.object.reflect;


import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.control.Validity;
import org.nakedobjects.security.SecurityContext;


public class Value extends Field {
	private final ValueIf delegatedTo;
	
	public Value(String name, Class type, ValueIf value) {
        super(name, type);
        delegatedTo = value;
    }

	public void clear(NakedObject inObject) {
        ((NakedValue) delegatedTo.get(inObject)).clear();
	}
	
    public About getAbout(SecurityContext context, NakedObject object) {
    	return delegatedTo.getAbout(context, object);
    }
    
    public boolean canAccess(SecurityContext context, NakedObject object) {
		return getAbout(context, object).canAccess().isAllowed();
	}
    
    public String getLabel(SecurityContext context, NakedObject object) {
    	About about = getAbout(context, object);

    	return getLabel(about);
    }

    /**
     Set the data in an NakedObject.  Passes in an existing object to for the object to reference.
     */
    public void initData(NakedObject inObject, Object setValue) {
    	delegatedTo.restoreValue(inObject, setValue);
    }

    public void set(NakedObject inObject, String setValue) throws InvalidEntryException {
        if (isDerived()) {
            throw new IllegalStateException("Can't set an attribute that is derived: " + getName());
        }
        NakedValue value = (NakedValue) delegatedTo.get(inObject);
    
        delegatedTo.parseValue(value, setValue);
        Validity validity = new Validity(value, getLabel(null, inObject));
        delegatedTo.isValid(inObject, validity);
        if(validity.isValid()) {  
            delegatedTo.setValue(inObject, value);
        } else {
	        String originalValue = value.title().toString();
            delegatedTo.parseValue(value, originalValue);
            throw new InvalidEntryException(validity.getReason());
        }
 
        //      delegatedTo.setValue(inObject, setValue);
    }

    public String toString() {
        return "Value [" + super.toString() + ",derived=" + isDerived() + "]";
    }

	public Naked get(NakedObject fromObject) {
		return delegatedTo.get(fromObject);
	}

	public boolean isDerived() {
		return delegatedTo.isDerived();
	}

	public boolean hasAbout() {
		return delegatedTo.hasAbout();
	}
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
