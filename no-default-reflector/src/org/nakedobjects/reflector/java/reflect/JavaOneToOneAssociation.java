package org.nakedobjects.reflector.java.reflect;

import org.nakedobjects.application.NakedObjectRuntimeException;
import org.nakedobjects.application.ValueParseException;
import org.nakedobjects.application.value.BusinessValue;
import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.TextEntryParseException;
import org.nakedobjects.object.control.DefaultHint;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.OneToOnePeer;
import org.nakedobjects.object.reflect.PojoAdapter;
import org.nakedobjects.object.security.Session;
import org.nakedobjects.reflector.java.control.SimpleFieldAbout;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Hashtable;

import org.apache.log4j.Category;


public class JavaOneToOneAssociation extends JavaField implements OneToOnePeer {
    private final static Category LOG = Category.getInstance(JavaOneToOneAssociation.class);
    protected Method addMethod;
    protected Method removeMethod;
    protected Method setMethod;

    public JavaOneToOneAssociation(String name, Class type, Method get, Method set, Method add, Method remove, Method about) {
        super(name, type, get, about, false);
        this.setMethod = set;
        this.addMethod = add;
        removeMethod = remove;
    }

    public Hint getHint(Session session, NakedObject object, NakedObject associate) {
        Method aboutMethod = getAboutMethod();

        //Class parameter = setMethod.getParameterTypes()[0];
        Class parameter = getMethod.getReturnType();
        if (associate != null && !parameter.isAssignableFrom(associate.getObject().getClass())) {
            SimpleFieldAbout about = new SimpleFieldAbout(session, object.getObject());
            about.unmodifiable("Invalid type: field must be set with a "
                    + NakedObjectSpecificationLoader.getInstance().loadSpecification(parameter.getName()));
            return about;
        }

        if (aboutMethod == null) {
            return new DefaultHint();
        }

        try {
            SimpleFieldAbout about = new SimpleFieldAbout(session, object.getObject());
            Object[] parameters;
            if (aboutMethod.getParameterTypes().length == 2) {
                parameters = new Object[] { about, associate == null ? null : associate.getObject() };
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

        return new DefaultHint();
    }

    public boolean hasAddMethod() {
        return addMethod != null;
    }

    /**
     * Set the data in an NakedObject. Passes in an existing object to for the
     * EO to reference.
     */
    public void initValue(NakedObject inObject, Object setValue) {
        LOG.debug("local initValue() " + inObject.getOid() + "/" + setValue);

        try {
            if (setMethod == null) {
                BusinessValue value = (BusinessValue) getMethod.invoke(inObject.getObject(), new Object[] {});
                if(setValue instanceof String) {
	                value.parseUserEntry((String) setValue);
                } else if(setValue instanceof BusinessValue) {
                    value.copyObject((BusinessValue) setValue);
                }
                
            } else {
                setMethod.invoke(inObject.getObject(), new Object[] { adaptValue((String) setValue) });
            }
        } catch (InvocationTargetException e) {
            invocationException("Exception executing " + setMethod, e);
        } catch (IllegalAccessException ignore) {
            LOG.error("Illegal access of " + setMethod, ignore);
        } catch (ValueParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setValue(NakedObject inObject, Object setValue) {
        LOG.debug("local setValue() " + inObject.getOid() + "/" + getName() + "/" + setValue);

        try {
            NakedObjectManager objectManager = inObject.getContext().getObjectManager();
            objectManager.startTransaction();

            if (setMethod == null) {
                BusinessValue value = (BusinessValue) getMethod.invoke(inObject.getObject(), new Object[] {});
                value.parseUserEntry((String) setValue);
            } else {
                setMethod.invoke(inObject.getObject(), new Object[] { setValue });
            }
        
            objectManager.endTransaction();
        } catch (InvocationTargetException e) {
            invocationException("Exception executing " + setMethod, e);
        } catch (IllegalAccessException ignore) {
            LOG.error("Illegal access of " + setMethod, ignore);
        } catch (ValueParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void clearAssociation(NakedObject inObject, NakedObject associate) {
        LOG.debug("local clear association " + inObject + "/" + associate);

        try {
	        NakedObjectManager objectManager = inObject.getContext().getObjectManager();
	        objectManager.startTransaction();
	
	        if (removeMethod != null) {
                removeMethod.invoke(inObject.getObject(), new Object[] { associate.getObject() });
            } else {
                setMethod.invoke(inObject.getObject(), new Object[] { null });
            }
            objectManager.endTransaction();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("set method expects a " + getType().getFullName() + " object; not a "
                    + associate.getClass().getName());
        } catch (InvocationTargetException e) {
            invocationException("Exception executing " + setMethod, e);
        } catch (IllegalAccessException ignore) {
            LOG.error("Illegal access of " + setMethod, ignore);
            throw new RuntimeException(ignore.getMessage());
        }
    }

    /**
     * Set the data in an NakedObject. Passes in an existing object to for the
     * EO to reference.
     */
    public void initAssociation(NakedObject inObject, NakedObject associate) {
        LOG.debug("local set association " + getName() + " in " + inObject + " with " + associate);

        try {
            setMethod.invoke(inObject.getObject(), new Object[] { associate.getObject() });
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(setMethod + " method doesn't expect a " + associate.getClass().getName());
        } catch (InvocationTargetException e) {
            invocationException("Exception executing " + setMethod, e);
        } catch (IllegalAccessException ignore) {
            LOG.error("Illegal access of " + setMethod, ignore);
            throw new RuntimeException(ignore.getMessage());
        }
    }

    public void setAssociation(NakedObject inObject, NakedObject associate) {
        LOG.debug("local set association " + getName() + " in " + inObject + " with " + associate);

        try {
            NakedObjectManager objectManager = inObject.getContext().getObjectManager();
            objectManager.startTransaction();

            if (associate == null) {
                if (removeMethod != null) {
                    removeMethod.invoke(inObject.getObject(), new Object[] { get(inObject) });
                } else {
                    setMethod.invoke(inObject.getObject(), new Object[] { associate.getObject() });
                }
            } else {
                if (hasAddMethod()) {
                    addMethod.invoke(inObject.getObject(), new Object[] { associate.getObject() });
                } else {
                    setMethod.invoke(inObject.getObject(), new Object[] { associate.getObject() });
                }
            }

            objectManager.endTransaction();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(setMethod + " method doesn't expect a " + associate.getClass().getName());
        } catch (InvocationTargetException e) {
            invocationException("Exception executing " + setMethod, e);
        } catch (IllegalAccessException ignore) {
            LOG.error("Illegal access of " + setMethod, ignore);
            throw new RuntimeException(ignore.getMessage());
        }
    }

    public String toString() {
        String methods = (getMethod == null ? "" : "GET") + (setMethod == null ? "" : " SET") + (addMethod == null ? "" : " ADD")
                + (removeMethod == null ? "" : " REMOVE");

        return "Association [name=\"" + getName() + "\", method=" + getMethod + ",about=" + getAboutMethod() + ", methods="
                + methods + ", type=" + getType() + " ]";
    }

    public NakedObject getAssociation(NakedObject fromObject) {
        return (NakedObject) get(fromObject);
    }

    private Naked get(NakedObject fromObject) {
        try {
            Object obj = getMethod.invoke(fromObject.getObject(), new Object[0]);

            if (obj == null) {
                return null;
            } else {
                return PojoAdapter.createAdapter(obj);
            }

        } catch (InvocationTargetException e) {
            invocationException("Exception executing " + getMethod, e);
            return null;
        } catch (IllegalAccessException ignore) {
            LOG.error("Illegal access of " + getMethod, ignore);
            throw new NakedObjectRuntimeException(ignore);
        }
    }

    public void parseTextEntry(NakedObject fromObject, String text) throws TextEntryParseException, InvalidEntryException {
        if (setMethod == null) {
            try {
                Object obj = getMethod.invoke(fromObject.getObject(), new Object[0]);
                BusinessValue value = (BusinessValue) obj;
                value.parseUserEntry(text);
                fromObject.markDirty();
            } catch (InvocationTargetException e) {
                invocationException("Exception executing " + getMethod, e);
            } catch (IllegalAccessException ignore) {
                LOG.error("Illegal access of " + getMethod, ignore);
                throw new NakedObjectRuntimeException("Illegal access of " + getMethod, ignore);
            } catch (ValueParseException e) {
                throw new TextEntryParseException(e);
            }

        } else {
            try {
                setMethod.invoke(fromObject.getObject(), new Object[] { adaptValue(text) });
            } catch (InvocationTargetException e) {
                invocationException("Exception executing " + setMethod, e);
            } catch (IllegalAccessException ignore) {
                LOG.error("Illegal access of " + setMethod, ignore);
                throw new NakedObjectRuntimeException("Illegal access of " + setMethod, ignore);
            }

        }
    }

    private Hashtable adapters = new Hashtable();
    {
        adapters.put(NakedObjectSpecificationLoader.getInstance().loadSpecification(java.lang.String.class) , new StringAdapter());
        adapters.put(NakedObjectSpecificationLoader.getInstance().loadSpecification(Date.class), new DateAdapter());
        adapters.put(NakedObjectSpecificationLoader.getInstance().loadSpecification(float.class), new FloatAdapter());
    }
    
    private Object adaptValue(String text) {
        JavaValueAdapter adapter = (JavaValueAdapter) adapters.get(getType()); 
        if(adapter == null) {
            throw new NakedObjectRuntimeException("No adapter found for " + getType());
        }
        return adapter.parse(text);
    }

    public boolean isEmpty(NakedObject fromObject) {
        try {
            Object obj = getMethod.invoke(fromObject.getObject(), new Object[0]);
            if (obj instanceof BusinessValue) {
                BusinessValue value = (BusinessValue) obj;
                return value.isEmpty();
            } else {
                return obj == null;
            }
        } catch (InvocationTargetException e) {
            invocationException("Exception executing " + getMethod, e);
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