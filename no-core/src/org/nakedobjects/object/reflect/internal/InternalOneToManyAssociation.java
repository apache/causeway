package org.nakedobjects.object.reflect.internal;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.control.DefaultHint;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.OneToManyPeer;
import org.nakedobjects.object.security.Session;
import org.nakedobjects.utility.NotImplementedException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

import org.apache.log4j.Category;


public class InternalOneToManyAssociation extends InternalField implements OneToManyPeer {
    private final static Category LOG = Category.getInstance(InternalOneToManyAssociation.class);
    private Method addMethod;
    private Method removeMethod;
    private Method clearMethod;

    public InternalOneToManyAssociation(String name, Class type, Method get, Method add, Method remove, Method about) {
        super(name, type, get, about, false);
        this.addMethod = add;
        this.removeMethod = remove;
   }

    public void addAssociation(NakedObject inObject, NakedObject associate) {
        LOG.debug("local set association " + getName() + " in " + inObject + " with " + associate);

        NakedObjectManager objectManager = inObject.getContext().getObjectManager();
        objectManager.startTransaction();

        try {
            addMethod.invoke(inObject.getObject(), new Object[] { associate.getObject() });
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("set method expects a " + getType().getFullName() + " object; not a "
                    + associate.getClass().getName() + ", " + e.getMessage());
        } catch (InvocationTargetException e) {
            LOG.error("Exception executing " + addMethod, e.getTargetException());
            throw (RuntimeException) e.getTargetException();
        } catch (IllegalAccessException ignore) {
            LOG.error("Illegal access of " + addMethod, ignore);
            throw new RuntimeException(ignore.getMessage());
        }

        objectManager.endTransaction();

    }
    
    public void initAssociation(NakedObject inObject, NakedObject associate) {
        LOG.debug("local set association " + getName() + " in " + inObject + " with " + associate);

        try {
            addMethod.invoke(inObject.getObject(), new Object[] { associate.getObject() });
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("set method expects a " + getType().getFullName() + " object; not a "
                    + associate.getClass().getName() + ", " + e.getMessage());
        } catch (InvocationTargetException e) {
            LOG.error("Exception executing " + addMethod, e.getTargetException());
            throw (RuntimeException) e.getTargetException();
        } catch (IllegalAccessException ignore) {
            LOG.error("Illegal access of " + addMethod, ignore);
            throw new RuntimeException(ignore.getMessage());
        }
    }

    public Hint getHint(Session session, NakedObject object, NakedObject element, boolean add) {
        if (hasHint()) {
            Method aboutMethod = getAboutMethod();
            try {
                InternalAbout about = new InternalAbout();
                Object[] parameters;
                if (aboutMethod.getParameterTypes().length == 3) {
                    parameters = new Object[] { about, element, new Boolean(add) };
                } else {
                    // default about
                    parameters = new Object[] { about };
                }
                aboutMethod.invoke(object.getObject(), parameters);
                return about;
            } catch (InvocationTargetException e) {
               LOG.error("Exception executing " + aboutMethod, e.getTargetException());
            } catch (IllegalAccessException ignore) {
                LOG.error("Illegal access of " + aboutMethod, ignore);
            }
            return null;
        } else {
            return new DefaultHint();
        }
    }

    public NakedCollection getAssociations(NakedObject fromObject) {
        return (NakedCollection) get(fromObject);
    }

    public void removeAllAssociations(NakedObject inObject) {
        try {
            clearMethod.invoke(inObject, null);
        } catch (InvocationTargetException e) {
            LOG.error("Exception executing " + clearMethod, e.getTargetException());
            throw (RuntimeException) e.getTargetException();
        } catch (IllegalAccessException ignore) {
            LOG.error("Illegal access of " + clearMethod, ignore);
            throw new RuntimeException(ignore.getMessage());
        }
    }

    /**
     * Remove an associated object (the element) from the specified NakedObject
     * in the association field represented by this object.
     */
    public void removeAssociation(NakedObject inObject, NakedObject associate) {
        LOG.debug("local clear association " + associate + " from field " + getName() + " in " + inObject);

        try {
            removeMethod.invoke(inObject.getObject(), new Object[] { associate.getObject() });
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("remove method expects a " + getType().getFullName() + " object; not a "
                    + associate.getClass().getName());
        } catch (InvocationTargetException e) {
            LOG.error("Exception executing " + addMethod, e.getTargetException());
            throw (RuntimeException) e.getTargetException();
        } catch (IllegalAccessException ignore) {
            LOG.error("Illegal access of " + addMethod, ignore);
            throw new RuntimeException(ignore.getMessage());
        }
    }

    public String toString() {
        String methods = (getMethod == null ? "" : "GET") + (addMethod == null ? "" : " ADD")
                + (removeMethod == null ? "" : " REMOVE");

        return "OneToManyAssociation [name=\"" + getName() + "\", method=" + getMethod + ",about=" + getAboutMethod()
                + ", methods=" + methods + ", type=" + getType() + " ]";
    }
    

    private Naked get(NakedObject fromObject) {
        try {
             Object obj = getMethod.invoke(fromObject.getObject(), new Object[0]);
            
            if(obj == null)  {
                  return null;
             } else if(obj instanceof Vector) {
                 return new InternalCollectionVectorAdapter((Vector) obj, type);
             } else {
                 throw new NakedObjectRuntimeException();
             }
             
        } catch (InvocationTargetException e) {
             LOG.error("Exception executing " + getMethod, e.getTargetException());
             throw new NakedObjectRuntimeException(e);
        } catch (IllegalAccessException ignore) {
            LOG.error("Illegal access of " + getMethod, ignore);
            throw new NakedObjectRuntimeException(ignore);
        }
    }

    public boolean isEmpty(NakedObject inObject) {
        throw new NotImplementedException();
    }

    public void initOneToManyAssociation(NakedObject fromObject, NakedObject[] instances) {
        try {
            Object obj = getMethod.invoke(fromObject.getObject(), new Object[0]);
           
            if(obj instanceof Vector) {
                ((Vector) obj).removeAllElements();
                for (int i = 0; i < instances.length; i++) {
	                ((Vector) obj).addElement(instances[i].getObject());
                }
            } else {
                throw new NakedObjectRuntimeException();
            }
            
       } catch (InvocationTargetException e) {
            LOG.error("Exception executing " + getMethod, e.getTargetException());
            throw new NakedObjectRuntimeException(e);
       } catch (IllegalAccessException ignore) {
           LOG.error("Illegal access of " + getMethod, ignore);
           throw new NakedObjectRuntimeException(ignore);
       }
    
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */