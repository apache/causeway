package org.nakedobjects.reflector.java.reflect;

import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.NakedObjectAssociation;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.PojoAdapter;
import org.nakedobjects.object.security.Session;
import org.nakedobjects.utility.NotImplementedException;

import java.util.Enumeration;
import java.util.Hashtable;

public class InternalCollectionAdapter implements InternalCollection {
    protected static Hashtable pojos = new Hashtable();
    private NakedObjectSpecification specification;
    private org.nakedobjects.application.collection.InternalCollection collection;
    private NakedObjectSpecification elementSpecification;
    private Oid oid;


    public static Naked createAdapter(org.nakedobjects.application.collection.InternalCollection pojo, Class type) {
        if(pojo == null) {
            return null;
        }
        InternalCollectionAdapter nakedObject;
        if(false && pojos.containsKey(pojo)) {
            nakedObject = (InternalCollectionAdapter) pojos.get(pojo);
        } else {
            nakedObject = new InternalCollectionAdapter(pojo, type);
        }
        
        return nakedObject;       
    }
    
    
    
    private InternalCollectionAdapter(org.nakedobjects.application.collection.InternalCollection vector, Class type) {
        this.collection = vector;
        
        pojos.put(vector, this);
        
        Class t = type == null ? Object.class : type;
        elementSpecification = NakedObjectSpecificationLoader.getInstance().loadSpecification(t);
    }

    public boolean contains(NakedObject object) {
        return collection.contains(object.getObject());
    }

    public NakedObject elementAt(int index) {
        Object element = collection.elementAt(index);
        return PojoAdapter.createNOAdapter(element);
    }

    public Enumeration elements() {
        final Enumeration elements = collection.elements();
        
        return new Enumeration() {
            public boolean hasMoreElements() {
                return elements.hasMoreElements();
            }

            public Object nextElement() {
                Object element = elements.nextElement();
                return element instanceof NakedObject ? element : PojoAdapter.createAdapter(element);
            }
        };
    }

    public Enumeration oids() {
        throw new NotImplementedException();
    }

    public int size() {
        return collection.size();
    }

    public String getIconName() {
        return null;
    }

    public Object getObject() {
        return collection;
    }

    public Oid getOid() {
        return oid;
    }

    public void setOid(Oid oid) {
       this.oid = oid;
    }

    public void setResolved() {}

    public void copyObject(Naked object) {}

    public NakedObjectSpecification getSpecification() {
        if(specification == null) {
            specification = NakedObjectSpecificationLoader.getInstance().loadSpecification(getObject().getClass());
        }
        return specification;
    }

    public boolean isSameAs(Naked object) {
        return false;
    }

    public String titleString() {
        return "vector...";
    }
    
    public NakedObjectSpecification getElementSpecification() {
        if(elementSpecification == null) {
            return NakedObjectSpecificationLoader.getInstance().loadSpecification(Object.class);
        }
        return elementSpecification;
    }

    public NakedObject parent() {
        return null;
    }

    public boolean isAggregated() {
        return false;
    }

    public void clear() {}
    
    public void clearAssociation(NakedObjectAssociation specification, NakedObject ref) {}

    public boolean canAccess(Session session, NakedObjectField specification) {
        return false;
    }

    public boolean canAccess(Session session, Action action) {
        return false;
    }

    public boolean canUse(Session session, NakedObjectField field) {
        return false;
    }

    public Naked execute(Action action, Naked[] parameters) {
        return null;
    }

    public Hint getHint(Session session, Action action, Naked[] parameters) {
        return null;
    }

    public Hint getHint(Session session, NakedObjectField field, Naked value) {
        return null;
    }

    public boolean isEmpty() {
        throw new NotImplementedException();
    }

    public String toString() {
        StringBuffer s = new StringBuffer();

        s.append("POJO (collection) ");
        // datatype
        NakedObjectSpecification spec = getSpecification();
        s.append(spec == null ? getClass().getName() : spec.getShortName());
        s.append(" [");

        // obect identifier
        if (oid != null) {
            s.append(":");
            s.append(oid.toString().toUpperCase());
        } else {
            s.append(":-");
        }

        // title
        s.append(" '");
        try {
            s.append(this.titleString());
        } catch (NullPointerException e) {
            s.append("no title");
        }
        s.append("'");
        s.append("]");

        s.append("  " + Long.toHexString(super.hashCode()).toUpperCase());

        s.append(collection);
        return s.toString();

        
        
    
    }


    public void clearViewDirty() {}

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