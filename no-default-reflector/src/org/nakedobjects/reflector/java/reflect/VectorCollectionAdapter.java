package org.nakedobjects.reflector.java.reflect;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.NakedObjectAssociation;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.security.Session;
import org.nakedobjects.utility.NotImplementedException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class VectorCollectionAdapter implements InternalCollection {
    protected static Hashtable pojos = new Hashtable();
    private NakedObjectSpecification specification;
    private Vector collection;
    private NakedObjectSpecification elementSpecification;
    private Oid oid;
    private long version;


    public long getVersion() {
        return version;
    }
    public void setVersion(long version) {
        this.version = version;
    }
    public static Naked createAdapter(Vector pojo, Class type) {
        if(pojo == null) {
            return null;
        }
        VectorCollectionAdapter nakedObject;
        if(false && pojos.containsKey(pojo)) {
            nakedObject = (VectorCollectionAdapter) pojos.get(pojo);
        } else {
            nakedObject = new VectorCollectionAdapter(pojo, type);
        }
        
        return nakedObject;       
    }
    
    
    
    private VectorCollectionAdapter(Vector vector, Class type) {
        this.collection = vector;
        
        pojos.put(vector, this);
        
        Class t = type == null ? Object.class : type;
        elementSpecification = NakedObjects.getSpecificationLoader().loadSpecification(t);
    }

    public boolean contains(NakedObject object) {
        return collection.contains(object.getObject());
    }

    public NakedObject elementAt(int index) {
        Object element = collection.elementAt(index);
        return NakedObjects.getPojoAdapterFactory().createNOAdapter(element);
    }

    public Enumeration elements() {
        final Enumeration elements = collection.elements();
        
        return new Enumeration() {
            public boolean hasMoreElements() {
                return elements.hasMoreElements();
            }

            public Object nextElement() {
                Object element = elements.nextElement();
                return element instanceof NakedObject ? element : NakedObjects.getPojoAdapterFactory().createAdapter(element);
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
            specification = NakedObjects.getSpecificationLoader().loadSpecification(getObject().getClass());
        }
        return specification;
    }

    public String titleString() {
        return "vector...";
    }
    
    public NakedObjectSpecification getElementSpecification() {
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

    public Hint getHint(Action action, Naked[] parameters) {
        return null;
    }

    public Hint getHint(NakedObjectField field, Naked value) {
        return null;
    }

    public boolean isEmpty() {
        throw new NotImplementedException();
    }

    public String toString() {
        StringBuffer s = new StringBuffer();

        s.append("POJO (collection)");
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



    public void sort() {}
    


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