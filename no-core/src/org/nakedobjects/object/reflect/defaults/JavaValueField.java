package org.nakedobjects.object.reflect.defaults;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectApplicationException;
import org.nakedobjects.object.NakedObjectDefinitionException;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.ValueParseException;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.control.FieldAbout;
import org.nakedobjects.object.control.Validity;
import org.nakedobjects.object.control.defaults.SimpleFieldAbout;
import org.nakedobjects.object.reflect.ReflectionException;
import org.nakedobjects.object.reflect.ValueField;
import org.nakedobjects.object.security.Session;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Category;


public class JavaValueField extends JavaField implements ValueField {
    private final static Category LOG = Category.getInstance(JavaValueField.class);
    private Method validMethod;

    public JavaValueField(String name, Class type, Method get, Method about, Method validMethod,
        boolean isDerived) {
        super(name, type, get, about, isDerived);
        this.validMethod = validMethod;
    }

    public About getAbout(Session session, NakedObject object) {
        if (hasAbout()) {
            Method aboutMethod = getAboutMethod();
            
            try {
                FieldAbout about = new SimpleFieldAbout(session, object);
                aboutMethod.invoke(object, new Object[] { about });
                return about; 
            } catch (InvocationTargetException e) {
                LOG.error("Exception executing " + aboutMethod,
                e.getTargetException());
            } catch (IllegalAccessException ignore) {
                LOG.error("Illegal access of " + aboutMethod, ignore);
            }
            
            return null;
        } else {
            return new DefaultAbout();
        }
    }

    public NakedValue getValue(NakedObject fromObject) {
        return (NakedValue) get(fromObject);
    }
    
    public Naked get(NakedObject fromObject) {
        Naked value = super.get(fromObject);
        if(value == null) {
            throw new NakedObjectDefinitionException("Value field '" + getName() + "', in " + fromObject + ",must not return null");
        }
        return value;
    }
    
    public void restoreValue(NakedObject inObject, String encodedValue) {
        NakedValue nakedValue = (NakedValue) get(inObject);
    	if(nakedValue == null) {
    		throw new ReflectionException("Value field '" + getName() + "' not set up in " + inObject);
    	}
        LOG.debug("restoreValue() " + getName() + " in " + inObject + "/" + encodedValue);
        nakedValue.restoreString(encodedValue);
    }

    public void saveValue(NakedObject inObject, String encodedValue) {
        NakedObjectManager objectManager = inObject.getContext().getObjectManager();
        objectManager.startTransaction();
        ((NakedValue) get(inObject)).restoreString(encodedValue);
        objectManager.objectChanged(inObject);
        objectManager.endTransaction();
    }
    
    public void parseValue(NakedValue value, String textEntry) throws ValueParseException {
        value.parse(textEntry);
    }
    
    public void isValid(NakedObject inObject, Validity validity) {
        if(validMethod != null) {
            try {
                validMethod.invoke(inObject, new Object[] { validity });
            } catch (IllegalArgumentException e) {
                throw new NakedObjectRuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new NakedObjectRuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new NakedObjectApplicationException(e);
            }
        }
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