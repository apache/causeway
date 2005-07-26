package org.nakedobjects.reflector.java.reflect;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.application.collection.InternalCollection;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.DefaultHint;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.MemberIdentifier;
import org.nakedobjects.object.reflect.OneToManyPeer;
import org.nakedobjects.object.reflect.ReflectionException;
import org.nakedobjects.reflector.java.control.SimpleFieldAbout;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Category;


public class JavaInternalCollection extends JavaField implements OneToManyPeer {
    private final static Category LOG = Category.getInstance(JavaInternalCollection.class);
    private Method addMethod;
    private Method removeMethod;
    private Method clearMethod;

    public JavaInternalCollection(String name, Class type, Method get, Method add, Method remove, Method about) {
        super(name, type, get, about, false);
        this.addMethod = add;
        this.removeMethod = remove;
//        this.clearMethod = clearMethod;
   }

    public void addAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {
        LOG.debug("local set association " + getName() + " in " + inObject + " with " + associate);
        try {
            if(addMethod != null) {
                addMethod.invoke(inObject.getObject(), new Object[] { associate.getObject() });
            } else {
                InternalCollection collection = (InternalCollection) getMethod.invoke(inObject.getObject(), new Object[0]);
                collection.add(associate.getObject());
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("set method expects a " + getType().getFullName() + " object; not a "
                    + associate.getClass().getName() + ", " + e.getMessage());
        } catch (InvocationTargetException e) {
            invocationException("Exception executing " + addMethod, e);
        } catch (IllegalAccessException ignore) {
            LOG.error("Illegal access of " + addMethod, ignore);
            throw new RuntimeException(ignore.getMessage());
        }
    }
    
    public void initAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {
        LOG.debug("local set association " + getName() + " in " + inObject + " with " + associate);
        try {
            if(addMethod != null) {
                addMethod.invoke(inObject.getObject(), new Object[] { associate.getObject() });
            } else {
                InternalCollection collection = (InternalCollection) getMethod.invoke(inObject.getObject(), new Object[0]);
                collection.add(associate.getObject());
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("set method expects a " + getType().getFullName() + " object; not a "
                    + associate.getClass().getName() + ", " + e.getMessage());
        } catch (InvocationTargetException e) {
            invocationException("Exception executing " + addMethod, e);
        } catch (IllegalAccessException ignore) {
            LOG.error("Illegal access of " + addMethod, ignore);
            throw new RuntimeException(ignore.getMessage());
        }
    }

    public Hint getHint(MemberIdentifier identifier, NakedObject object, NakedObject element, boolean add) {
        if (hasHint()) {
            Method aboutMethod = getAboutMethod();
            try {
                SimpleFieldAbout about = new SimpleFieldAbout(NakedObjects.getCurrentSession(), object.getObject());
                Object[] parameters;
                if (aboutMethod.getParameterTypes().length == 3) {
                    parameters = new Object[] { about, element == null ? null : element.getObject(), new Boolean(add) };
                } else {
                    // default about
                    parameters = new Object[] { about };
                }
                aboutMethod.invoke(object.getObject(), parameters);
                return about;
            } catch (InvocationTargetException e) {
               invocationException("Exception executing " + aboutMethod, e);
            } catch (IllegalAccessException ignore) {
                LOG.error("Illegal access of " + aboutMethod, ignore);
            }
            return null;
        } else {
            return new DefaultHint();
        }
    }

    public NakedCollection getAssociations(MemberIdentifier identifier, NakedObject fromObject) {
        return (NakedCollection) get(fromObject);
    }

    public Object getExtension(Class cls) {
        return null;
    }
    
    public void removeAllAssociations(MemberIdentifier identifier, NakedObject inObject) {
        try {
            clearMethod.invoke(inObject, null);
        } catch (InvocationTargetException e) {
            invocationException("Exception executing " + clearMethod, e);
        } catch (IllegalAccessException ignore) {
            LOG.error("Illegal access of " + clearMethod, ignore);
            throw new RuntimeException(ignore.getMessage());
        }
    }

    /**
     * Remove an associated object (the element) from the specified NakedObject
     * in the association field represented by this object.
     */
    public void removeAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {
        LOG.debug("local clear association " + associate + " from field " + getName() + " in " + inObject);

        try {
            if(removeMethod != null) {
	            removeMethod.invoke(inObject.getObject(), new Object[] { associate.getObject() });
            } else {
                InternalCollection collection = (InternalCollection) getMethod.invoke(inObject.getObject(), new Object[0]);
                collection.remove(associate.getObject());
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("remove method expects a " + getType().getFullName() + " object; not a "
                    + associate.getClass().getName());
        } catch (InvocationTargetException e) {
            invocationException("Exception executing " + addMethod, e);
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
            InternalCollection collection = (InternalCollection) getMethod.invoke(fromObject.getObject(), new Object[0]);
            return InternalCollectionAdapter.createAdapter(collection, type);
        } catch (InvocationTargetException e) {
             invocationException("Exception executing " + getMethod, e);
             throw new ReflectionException(e);
        } catch (IllegalAccessException ignore) {
            LOG.error("Illegal access of " + getMethod, ignore);
            throw new ReflectionException(ignore);
        }
    }
   
    public boolean isEmpty(MemberIdentifier identifier, NakedObject fromObject) {
        try {
            InternalCollection collection = (InternalCollection) getMethod.invoke(fromObject.getObject(), new Object[0]);
            return collection.isEmpty();
        } catch (InvocationTargetException e) {
            invocationException("Exception executing " + getMethod, e);
            throw new ReflectionException(e);
        } catch (IllegalAccessException ignore) {
            LOG.error("Illegal access of " + getMethod, ignore);
            throw new ReflectionException(ignore);
        }
    }
    

    public void initOneToManyAssociation(MemberIdentifier identifier, NakedObject fromObject, NakedObject[] instances) {
        try {
            InternalCollection collection = (InternalCollection) getMethod.invoke(fromObject.getObject(), new Object[0]);
            collection.removeAllElements();
            for (int i = 0; i < instances.length; i++) {
                collection.add(instances[i].getObject());
            }
       } catch (InvocationTargetException e) {
            LOG.error("Exception executing " + getMethod, e.getTargetException());
            throw new ReflectionException(e);
       } catch (IllegalAccessException ignore) {
           LOG.error("Illegal access of " + getMethod, ignore);
           throw new ReflectionException(ignore);
       }
    }
    
    /**
    return the object type, as a Class object, that the method returns.
    */
   public NakedObjectSpecification getType() {
       return type == null ? NakedObjects.getSpecificationLoader().loadSpecification(Object.class) : super.getType();
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