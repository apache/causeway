package org.nakedobjects.reflector.java.reflect;

import org.nakedobjects.object.MemberIdentifier;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectLoader;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.control.DefaultHint;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.OneToManyPeer;
import org.nakedobjects.object.reflect.ReflectionException;
import org.nakedobjects.reflector.java.control.SimpleFieldAbout;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

import org.apache.log4j.Category;


public class JavaOneToManyAssociation extends JavaField implements OneToManyPeer {
    private final static Category LOG = Category.getInstance(JavaOneToManyAssociation.class);
    private Method addMethod;
    private Method removeMethod;
    private Method clearMethod;

    public JavaOneToManyAssociation(String name, Class type, Method get, Method add, Method remove, Method about) {
        super(name, type, get, about, add == null && remove == null);
        this.addMethod = add;
        this.removeMethod = remove;
    }

    public void addAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {
        LOG.debug("local set association " + getName() + " in " + inObject + " with " + associate);
        try {
            addMethod.invoke(inObject.getObject(), new Object[] { associate.getObject() });
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("set method expects a " + getType().getFullName() + " object; not a "
                    + associate.getClass().getName() + ", " + e.getMessage());
        } catch (InvocationTargetException e) {
            invocationException("Exception executing " + addMethod, e);
        } catch (IllegalAccessException ignore) {
            LOG.error("illegal access of " + addMethod, ignore);
            throw new RuntimeException(ignore.getMessage());
        }
    }

    public void initAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {
        LOG.debug("init association " + getName() + " in " + inObject + " with " + associate);
        try {
            Object obj = getMethod.invoke(inObject.getObject(), new Object[0]);

            if (obj instanceof Vector) {
                ((Vector) obj).addElement(associate.getObject());
            } else {
                throw new ReflectionException();
            }

            //            addMethod.invoke(inObject.getObject(), new Object[] { associate.getObject() });
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("set method expects a " + getType().getFullName() + " object; not a "
                    + associate.getClass().getName() + ", " + e.getMessage());
        } catch (InvocationTargetException e) {
            invocationException("Exception executing " + addMethod, e);
        } catch (IllegalAccessException ignore) {
            LOG.error("illegal access of " + addMethod, ignore);
            throw new RuntimeException(ignore.getMessage());
        }
    }

    public Hint getHint(MemberIdentifier identifier, NakedObject object, NakedObject element, boolean add) {
        if (hasHint()) {
            Method aboutMethod = getAboutMethod();
            try {
                SimpleFieldAbout hint = new SimpleFieldAbout(NakedObjects.getCurrentSession(), object.getObject());
                Object[] parameters;
                if (aboutMethod.getParameterTypes().length == 3) {
                    parameters = new Object[] { hint, element == null ? null : element.getObject(), new Boolean(add) };
                } else {
                    // default about
                    parameters = new Object[] { hint };
                }
                aboutMethod.invoke(object.getObject(), parameters);
                if (hint.getDescription().equals("")) {
                    hint.setDescription("Add " + element.getObject() + " to field " + getName());
                }
                return hint;
            } catch (InvocationTargetException e) {
                invocationException("Exception executing " + aboutMethod, e);
            } catch (IllegalAccessException ignore) {
                LOG.error("illegal access of " + aboutMethod, ignore);
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

    public Class[] getExtensions() {
        return new Class[0];
    }
    
    public void removeAllAssociations(MemberIdentifier identifier, NakedObject inObject) {
        try {
            clearMethod.invoke(inObject, null);
        } catch (InvocationTargetException e) {
            invocationException("Exception executing " + clearMethod, e);
        } catch (IllegalAccessException ignore) {
            LOG.error("illegal access of " + clearMethod, ignore);
            throw new RuntimeException(ignore.getMessage());
        }
    }

    /**
     * Remove an associated object (the element) from the specified NakedObject in the association field
     * represented by this object.
     */
    public void removeAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {
        LOG.debug("local clear association " + associate + " from field " + getName() + " in " + inObject);

        try {
            removeMethod.invoke(inObject.getObject(), new Object[] { associate.getObject() });
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage() + " object: " + inObject.getObject() + ", parameter: "
                    + associate.getObject());
        } catch (InvocationTargetException e) {
            invocationException("Exception executing " + addMethod, e);
        } catch (IllegalAccessException ignore) {
            LOG.error("illegal access of " + addMethod, ignore);
            throw new RuntimeException(ignore.getMessage());
        }
    }

    public String toString() {
        String methods = (getMethod == null ? "" : "GET") + (addMethod == null ? "" : " ADD")
                + (removeMethod == null ? "" : " REMOVE");

        return "OneToManyAssociation [name=\"" + getName() + "\", method=" + getMethod + ",about=" + getAboutMethod()
                + ", methods=" + methods + ", type=" + getType() + " ]";
    }

    private NakedCollection get(NakedObject fromObject) {
        try {
            Object collection = getMethod.invoke(fromObject.getObject(), new Object[0]);

            NakedCollection adapter;
            if (collection == null) {
                return null;
            } else {
                NakedObjectLoader objectLoader = NakedObjects.getObjectLoader();
                adapter = objectLoader.getAdapterForElseCreateAdapterForCollection(fromObject, getName(), getType(), collection);
                if (adapter == null) {
                    throw new ReflectionException("no adapter created");
                } else {
                    return adapter;
                }
            }
        } catch (InvocationTargetException e) {
            invocationException("Exception executing " + getMethod, e);
            throw new ReflectionException(e);
        } catch (IllegalAccessException ignore) {
            LOG.error("illegal access of " + getMethod, ignore);
            throw new ReflectionException(ignore);
        }
    }

    public boolean isEmpty(MemberIdentifier identifier, NakedObject fromObject) {
        try {
            Object obj = getMethod.invoke(fromObject.getObject(), new Object[0]);

            if (obj == null) {
                return true;
            } else if (obj instanceof Vector) {
                return ((Vector) obj).isEmpty();
            } else {
                throw new ReflectionException();
            }
        } catch (InvocationTargetException e) {
            invocationException("Exception executing " + getMethod, e);
            throw new ReflectionException(e);
        } catch (IllegalAccessException ignore) {
            LOG.error("illegal access of " + getMethod, ignore);
            throw new ReflectionException(ignore);
        }
    }

    public void initOneToManyAssociation(MemberIdentifier identifier, NakedObject fromObject, NakedObject[] instances) {
        try {
            Object obj = getMethod.invoke(fromObject.getObject(), new Object[0]);

            if (obj instanceof Vector) {
                ((Vector) obj).removeAllElements();
                for (int i = 0; i < instances.length; i++) {
                    ((Vector) obj).addElement(instances[i].getObject());
                }
            } else if (obj instanceof Object[]) {
                 throw new ReflectionException("not set up to deal with arrays");   
            } else {
                throw new ReflectionException("Can't initialise " + obj);
            }

        } catch (InvocationTargetException e) {
            LOG.error("exception executing " + getMethod, e.getTargetException());
            throw new ReflectionException(e);
       } catch (IllegalAccessException ignore) {
           LOG.error("illegal access of " + getMethod, ignore);
           throw new ReflectionException(ignore);
       }
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */
