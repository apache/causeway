package org.nakedobjects.object.reflect;

import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.TextEntryParseException;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.defaults.AbstractNakedObject;
import org.nakedobjects.object.security.Session;

import java.util.Hashtable;

public class PojoAdapter extends AbstractNakedObject {
    protected static Hashtable pojos = new Hashtable();
    private Object pojo;
 //   private NakedObjectSpecification specification;

    public static PojoAdapter createAdapter(Object pojo, Oid oid) {
        if(pojo == null) {
            return null;
        }
        PojoAdapter nakedObject;
        if(pojos.containsKey(pojo)) {
            nakedObject = (PojoAdapter) pojos.get(pojo);
        } else {
            if(pojo instanceof PojoAdapter) {
                throw new NakedObjectRuntimeException("Warning: adapter is wrapping an adapter: " + pojo);
            }
            nakedObject = new PojoAdapter(pojo);
        }
        
        if (oid != null) {
            if (nakedObject.getOid() == null) {
                nakedObject.setOid(oid);
            } else {
                if (!nakedObject.getOid().equals(oid)) {
                    throw new NakedObjectRuntimeException("Different OID for same Pojo");
                }
            }
        }
        
  //      nakedObject.setContext(NakedObjectContext.getDefaultContext());
        
        return nakedObject;       
    }
    
    public static PojoAdapter createAdapter(Object pojo) {
       return createAdapter(pojo, null);
    }
    
    protected PojoAdapter(Object pojo) {
        this.pojo= pojo;
        pojos.put(pojo, this);
    }

    public Object getObject() {
        return pojo;
    }

    public String titleString() {
        NakedObjectSpecification specification = getSpecification();
        return specification == null ? "" : specification.getTitle().title(this);
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
    
    public NakedObject getField(NakedObjectField field) {
        return (NakedObject) field.get(this);
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

    public NakedObject execute(Action action, Naked[] parameters) {
        NakedObject result = action.execute(this, parameters);
        getObjectManager().saveChanges();
        return result;
    }

    public Hint getHint(Session session, Action action, Naked[] parameterValues) {
        return action.getHint(session, this, parameterValues);
    }

    public Hint getHint(Session session, NakedObjectField field, NakedObject value) {
        if(field instanceof OneToOneAssociation) {
            return ((OneToOneAssociation) field).getHint(session, this, value);
        } else if(field instanceof OneToManyAssociation) {
            return ((OneToManyAssociation) field).getHint(session, this);
        } else {
            throw new NakedObjectRuntimeException();
        }
    }

    public void parseTextEntry(OneToOneAssociation field, String text) throws TextEntryParseException, InvalidEntryException {
        field.parseTextEntry(this, text);
        //getObjectManager().objectChanged(this);
        markDirty();
        getObjectManager().saveChanges();
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