package org.nakedobjects.object.reflect;


import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
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
        delegatedTo.getValue(inObject).clear();
	}
	
    public About getAbout(SecurityContext context, NakedObject object) {
    	return delegatedTo.getAbout(context, object);
    }
    
    public boolean canAccess(SecurityContext context, NakedObject object) {
		return getAbout(context, object).canAccess().isAllowed();
	}
    
    public boolean canUse(SecurityContext context, NakedObject object) {
		return getAbout(context, object).canUse().isAllowed();
	}
    
    public String getLabel(SecurityContext context, NakedObject object) {
    	About about = getAbout(context, object);

    	return getLabel(about);
    }

    public void restoreValue(NakedObject inObject, String encodedValue) {
    	delegatedTo.restoreValue(inObject, encodedValue);
    }

    public void parseAndSave(NakedObject inObject, String textEntry) throws InvalidEntryException {
        if (isDerived()) {
            throw new NakedObjectRuntimeException("Can't set an value that is derived: " + getName());
        }
        NakedValue value = delegatedTo.getValue(inObject);
    
        String originalValue = value.title().toString();
        delegatedTo.parseValue(value, textEntry);
        Validity validity = new Validity(value, getLabel(null, inObject));
        delegatedTo.isValid(inObject, validity);
        if(validity.isValid()) {  
            delegatedTo.saveValue(inObject, value.saveString());
        } else {
            delegatedTo.parseValue(value, originalValue);
            throw new InvalidEntryException(validity.getReason());
        }
     }

    public void saveEncoded(NakedObject inObject, String encodedValue) throws InvalidEntryException {
        if (isDerived()) {
            throw new NakedObjectRuntimeException("Can't set an value that is derived: " + getName());
        }
        
        delegatedTo.saveValue(inObject, encodedValue);
     }


    public String toString() {
        return "Value [" + super.toString() + ",derived=" + isDerived() + "]";
    }

	public Naked get(NakedObject fromObject) {
		return getValue(fromObject);
	}
	
	public NakedValue getValue(NakedObject fromObject) {
		return delegatedTo.getValue(fromObject);
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
