package org.nakedobjects.object.reflect;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.ReflectorFactory;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.defaults.AbstractNakedObject;
import org.nakedobjects.object.reflect.valueadapter.DateAdapter;
import org.nakedobjects.object.reflect.valueadapter.StringAdapter;
import org.nakedobjects.object.security.Session;

import java.util.Date;
import java.util.Hashtable;

public class PojoAdapter extends AbstractNakedObject {
    protected static Hashtable pojos = new Hashtable();
    private static ReflectorFactory reflectorFactory;
    private Object pojo;


    public static void setReflectorFactory(ReflectorFactory reflectorFactory) {
        PojoAdapter.reflectorFactory = reflectorFactory;
    }
    
    public static void set_ReflectorFactory(ReflectorFactory reflectorFactory) {
        PojoAdapter.reflectorFactory = reflectorFactory;
    }
    
    public static NakedObject createNOAdapter(Object pojo) {
        return (NakedObject) createAdapter(pojo);
    }

    private static Naked createAdapter(Object pojo, Oid oid) {
        if(pojo == null) {
            return null;
        }
        Naked nakedObject;
        if(pojos.containsKey(pojo)) {
            nakedObject = (PojoAdapter) pojos.get(pojo);
        } else {
            if(pojo instanceof PojoAdapter) {
                throw new NakedObjectRuntimeException("Warning: adapter is wrapping an adapter: " + pojo);
            }
            
            if(pojo instanceof String) {
                nakedObject = new StringAdapter((String) pojo);
            } else if(pojo instanceof Date) {
                    nakedObject = new DateAdapter((Date) pojo);
            } else {
                nakedObject = reflectorFactory.createAdapter(pojo);
            }
            if(nakedObject == null) {
                nakedObject = new PojoAdapter(pojo);
            }
        }
        return nakedObject;       
    }
    
    public static Naked createAdapter(Object pojo) {
       return createAdapter(pojo, null);
    }
    
    protected PojoAdapter(Object pojo) {
        this.pojo= pojo;
        pojos.put(pojo, this);
        
        if(pojo.getClass().getName().endsWith("String")) {
            new RuntimeException().printStackTrace();
        }
    }

    public Object getObject() {
        return pojo;
    }

    public String titleString() {
        NakedObjectSpecification specification = getSpecification();
        String title =  specification == null ? null : specification.getTitle().title(this);
        if (title == null) {
            return "A " + specification.getSingularName().toLowerCase();
        } else {
            return title;
        }
    }
    
    public String toString() {
        return "POJO " + super.toString() + " " + titleString();
    }
    
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        
        if(obj instanceof PojoAdapter) {
            PojoAdapter object = (PojoAdapter) obj;
            return object.getObject().equals(getObject());
        }
        return false;
    }

    public String getLabel(Session session, Action action) {
        return action.getLabel(session, this);
    }
    
    public void clear(NakedObjectAssociation specification, NakedObject associate) {
        specification.clearAssociation(this, associate);
    }
    
    public Naked getField(NakedObjectField field) {
        return field.get(this);
    }
    
    public NakedObject getAssociation(OneToOneAssociation field) {
        return (NakedObject) field.get(this);
    }

    public NakedValue getValue(OneToOneAssociation field) {
        return (NakedValue) field.get(this);
    }

    public void clear(OneToOneAssociation specification) {
        specification.clear(this);    
    }

    public void initOneToManyAssociation(OneToManyAssociation field, NakedObject[] instances) {
        field.initOneToManyAssociation(this, instances);
    }
    
    public void setAssociation(NakedObjectAssociation field, NakedObject associatedObject) {
        field.setAssociation(this, associatedObject);
        getObjectManager().saveChanges();
    }
    
    public void initAssociation(NakedObjectAssociation field, NakedObject associatedObject) {
        field.initAssociation(this, associatedObject);
    }

    public void setValue(OneToOneAssociation field, Object object) {
        field.setValue(this, object);
    }

    public void initValue(OneToOneAssociation field, Object object) {
        field.initValue(this, object);
    }

    public String getLabel(Session session, NakedObjectField field) {
        return field.getLabel(session, this);
    }

    public boolean canAccess(Session session, NakedObjectField specification) {
        return specification.canAccess(session, this);
    }

    public boolean canAccess(Session session, Action action) {
        return action.canAccess(session, this);
    }

    public boolean canUse(Session session, NakedObjectField field) {
        return  field.canAccess(session, this);
    }

    public Naked execute(Action action, Naked[] parameters) {
        Naked result = action.execute(this, parameters);
        getObjectManager().saveChanges();
        return result;
    }

    public Hint getHint(Session session, Action action, Naked[] parameterValues) {
        return action.getHint(session, this, parameterValues);
    }

    public Hint getHint(Session session, NakedObjectField field, Naked value) {
        if(field instanceof OneToOneAssociation) {
            return ((OneToOneAssociation) field).getHint(session, this, value);
        } else if(field instanceof OneToManyAssociation) {
            return ((OneToManyAssociation) field).getHint(session, this);
        } else {
            throw new NakedObjectRuntimeException();
        }
    }
    
    public boolean isEmpty(NakedObjectField field) {
        return field.isEmpty(this);
    }

    public boolean isParsable() {
        return  getSpecification().isParsable();
    }
    
    
    public ActionParameterSet getParameters(Session session, Action action, NakedObjectSpecification[] parameterTypes) {
        return action.getParameters(session, this, parameterTypes);
    }
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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