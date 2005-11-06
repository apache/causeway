package org.nakedobjects.object.reflect.internal;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.reflect.MemberIdentifierImpl;
import org.nakedobjects.object.reflect.OneToManyPeer;
import org.nakedobjects.utility.NakedObjectRuntimeException;
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

    public InternalOneToManyAssociation(String className, String name, Class type, Method get, Method add, Method remove) {
        super(type, get, false);
        this.addMethod = add;
        this.removeMethod = remove;
        
        identifeir = new MemberIdentifierImpl(className, name);
   }

    public void addAssociation(NakedObject inObject, NakedObject associate) {
        LOG.debug("add association to " + getIdentifier() + " in " + inObject + " - " + associate);
        try {
            addMethod.invoke(inObject.getObject(), new Object[] { associate.getObject() });
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("set method expects a " + getType().getFullName() + " object; not a "
                    + associate.getClass().getName() + ", " + e.getMessage());
        } catch (InvocationTargetException e) {
            LOG.error("exception executing " + addMethod, e.getTargetException());
            throw (RuntimeException) e.getTargetException();
        } catch (IllegalAccessException ignore) {
            LOG.error("illegal access of " + addMethod, ignore);
            throw new RuntimeException(ignore.getMessage());
        }
    }
    
    public void initAssociation(NakedObject inObject, NakedObject associate) {
        LOG.debug("init association " + getIdentifier() + " in " + inObject + " - " + associate);

        try {
            addMethod.invoke(inObject.getObject(), new Object[] { associate.getObject() });
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("set method expects a " + getType().getFullName() + " object; not a "
                    + associate.getClass().getName() + ", " + e.getMessage());
        } catch (InvocationTargetException e) {
            LOG.error("exception executing " + addMethod, e.getTargetException());
            throw (RuntimeException) e.getTargetException();
        } catch (IllegalAccessException ignore) {
            LOG.error("illegal access of " + addMethod, ignore);
            throw new RuntimeException(ignore.getMessage());
        }
    }


    public NakedCollection getAssociations(NakedObject fromObject) {
        return (NakedCollection) get(fromObject);
    }

    public void removeAllAssociations(NakedObject inObject) {
        try {
            clearMethod.invoke(inObject, null);
        } catch (InvocationTargetException e) {
            LOG.error("exception executing " + clearMethod, e.getTargetException());
            throw (RuntimeException) e.getTargetException();
        } catch (IllegalAccessException ignore) {
            LOG.error("illegal access of " + clearMethod, ignore);
            throw new RuntimeException(ignore.getMessage());
        }
    }

    /**
     * Remove an associated object (the element) from the specified NakedObject
     * in the association field represented by this object.
     */
    public void removeAssociation(NakedObject inObject, NakedObject associate) {
        LOG.debug("remove association " + associate + " from field " + getIdentifier() + " in " + inObject);

        try {
            removeMethod.invoke(inObject.getObject(), new Object[] { associate.getObject() });
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("remove method expects a " + getType().getFullName() + " object; not a "
                    + associate.getClass().getName());
        } catch (InvocationTargetException e) {
            LOG.error("exception executing " + addMethod, e.getTargetException());
            throw (RuntimeException) e.getTargetException();
        } catch (IllegalAccessException ignore) {
            LOG.error("illegal access of " + addMethod, ignore);
            throw new RuntimeException(ignore.getMessage());
        }
    }

    public String toString() {
        String methods = (getMethod == null ? "" : "GET") + (addMethod == null ? "" : " ADD")
                + (removeMethod == null ? "" : " REMOVE");

        return "OneToManyAssociation [name=\"" + getIdentifier() + "\", method=" + getMethod
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
             LOG.error("exception executing " + getMethod, e.getTargetException());
             throw new NakedObjectRuntimeException(e);
        } catch (IllegalAccessException ignore) {
            LOG.error("illegal access of " + getMethod, ignore);
            throw new NakedObjectRuntimeException(ignore);
        }
    }
    
    public Object getExtension(Class cls) {
        return null;
    }

    public Class[] getExtensions() {
        return new Class[0];
    }
    
    public String getName() {
        return null;
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
            LOG.error("exception executing " + getMethod, e.getTargetException());
            throw new NakedObjectRuntimeException(e);
       } catch (IllegalAccessException ignore) {
           LOG.error("illegal access of " + getMethod, ignore);
           throw new NakedObjectRuntimeException(ignore);
       }
    
    }

    public Consent validToRemove(NakedObject container, NakedObject element) {
        return Allow.DEFAULT;
    }

    public Consent validToAdd(NakedObject container, NakedObject element) {
        return Allow.DEFAULT;
    }

    public Consent isEditable(NakedObject target) {
        return Allow.DEFAULT;
    }

    public Consent isVisible(NakedObject target) {
        return Allow.DEFAULT;
    }

    public boolean isAccessible() {
        return true;
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