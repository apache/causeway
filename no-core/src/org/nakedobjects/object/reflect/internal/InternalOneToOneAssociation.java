package org.nakedobjects.object.reflect.internal;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.control.DefaultHint;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.MemberIdentifier;
import org.nakedobjects.object.reflect.OneToOnePeer;
import org.nakedobjects.object.reflect.valueadapter.StringAdapter;
import org.nakedobjects.utility.NotImplementedException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Category;


public class InternalOneToOneAssociation extends InternalField implements OneToOnePeer {
	private final static Category LOG = Category.getInstance(InternalOneToOneAssociation.class);
    protected Method addMethod;
    protected Method removeMethod;
    protected Method setMethod;

    public InternalOneToOneAssociation(String name, Class type, Method get, Method set, Method add, Method remove,
        Method about) {
        super(name, type, get, about, false);
        this.setMethod = set;
        this.addMethod = add;
        removeMethod = remove;
    }
    
    public Hint getHint(MemberIdentifier identifier, NakedObject object, Naked associate) {
        Method aboutMethod = getAboutMethod();
		
		//Class parameter = setMethod.getParameterTypes()[0];
        Class parameter = getMethod.getReturnType();
		if(associate != null && !parameter.isAssignableFrom(associate.getObject().getClass())) {
			InternalAbout about = new InternalAbout();
			//about.unmodifiable("Invalid type: field must be set with a " + NakedObjects.getSpecificationLoader().loadSpecification(parameter.getName()));
			return about;
		}

        if (aboutMethod == null) {
			return new DefaultHint();
        }

        try {
            InternalAbout about = new InternalAbout();
        		Object[] parameters;
                if(aboutMethod.getParameterTypes().length == 2) {
        		    parameters = new Object[] { about, associate == null ? null : associate.getObject() };
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

        return new DefaultHint();
    }

    public boolean hasAddMethod() {
        return addMethod != null;
    }

    public void setValue(MemberIdentifier identifier, NakedObject inObject, Object value) {
        LOG.debug("set value "  + getName() + " in " + inObject + " - " + value);

        try {
            if (setMethod == null) {
//                    throw new IllegalStateException("can't intialise " + getName() + "  in " +
//                        inObject.getClass() + " as there is no set method");
                
//                    BusinessValue value = (BusinessValue) getMethod.invoke(inObject.getObject(), new Object[] {});
   //                 value.parseUserEntry((String) setValue);
            } else {
                	setMethod.invoke(inObject.getObject(), new Object[] { value });
            }
        } catch (InvocationTargetException e) {
            LOG.error("Exception executing " + setMethod, e.getTargetException());
            
            if (e.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException) e.getTargetException();
            } else {
                throw new RuntimeException(e.getTargetException().getMessage());
            }
        } catch (IllegalAccessException ignore) {
            LOG.error("Illegal access of " + setMethod, ignore);
        } 
    }
    
    /**
     Set the data in an NakedObject.  Passes in an existing object to for the EO to reference.
     */
    public void initValue(MemberIdentifier identifier, NakedObject inObject, Object setValue) {
        LOG.debug("init value " + getName() + " in " + inObject.getOid() + " - " + setValue);

        try {
            setMethod.invoke(inObject.getObject(), new Object[] { setValue });
        } catch (InvocationTargetException e) {
            LOG.error("Exception executing " + setMethod, e.getTargetException());
            
            if (e.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException) e.getTargetException();
            } else {
                throw new RuntimeException(e.getTargetException().getMessage());
            }
        } catch (IllegalAccessException ignore) {
            LOG.error("Illegal access of " + setMethod, ignore);
        } 
    }

     public void clearAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {
        LOG.debug("clear association " + getName() + " from " + inObject);

            try {
                if (removeMethod != null) {
                    removeMethod.invoke(inObject.getObject(), new Object[] { associate.getObject() });
                } else {
                    setMethod.invoke(inObject.getObject(), new Object[] { null });
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("set method expects a " + getType().getFullName() +
                    " object; not a " + associate.getClass().getName());
            } catch (InvocationTargetException e) {
                LOG.error("Exception executing " + setMethod, e.getTargetException());
                throw (RuntimeException) e.getTargetException();
            } catch (IllegalAccessException ignore) {
                LOG.error("Illegal access of " + setMethod, ignore);
                throw new RuntimeException(ignore.getMessage());
            }
    }

     public void initAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {
        LOG.debug("init association " + getName() + " in " + inObject + " - " + associate);

        try {
            setMethod.invoke(inObject.getObject(), new Object[] { associate.getObject() });
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(setMethod + " method doesn't expect a " + associate.getClass().getName());
        } catch (InvocationTargetException e) {
            LOG.error("Exception executing " + setMethod, e.getTargetException());

            if (e.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException) e.getTargetException();
            } else {
                throw new RuntimeException(e.getTargetException().getMessage());
            }
        } catch (IllegalAccessException ignore) {
            LOG.error("Illegal access of " + setMethod, ignore);
            throw new RuntimeException(ignore.getMessage());
        }
    }
     
    /**
     * Set the data in an NakedObject. Passes in an existing object to for the
     * EO to reference.
     */
    public void setAssociation(MemberIdentifier identifier, NakedObject inObject, NakedObject associate) {
        LOG.debug("set association " + getName() + " in " + inObject + " - " + associate);

            try {
                if (associate == null) {
                    if (removeMethod != null) {
                        removeMethod.invoke(inObject.getObject(), new Object[] { getAssociation(inObject) });
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
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(setMethod + " method doesn't expect a " +
                    associate.getClass().getName());
            } catch (InvocationTargetException e) {
                LOG.error("Exception executing " + setMethod, e.getTargetException());

                if (e.getTargetException() instanceof RuntimeException) {
                    throw (RuntimeException) e.getTargetException();
                } else {
                    throw new RuntimeException(e.getTargetException().getMessage());
                }
            } catch (IllegalAccessException ignore) {
                LOG.error("Illegal access of " + setMethod, ignore);
                throw new RuntimeException(ignore.getMessage());
            }
    }

    public String toString() {
        String methods = (getMethod == null ? "" : "GET") +
            (setMethod == null ? "" : " SET") + (addMethod == null ? "" : " ADD") +
            (removeMethod == null ? "" : " REMOVE");

        return "Association [name=\"" + getName() + "\", method=" + getMethod + ",about=" +
        getAboutMethod() + ", methods=" + methods + ", type=" + getType() + " ]";
    }

	public Naked getAssociation(MemberIdentifier identifier, NakedObject fromObject) {
		return getAssociation(fromObject);
	}
	
	public Object getExtension(Class cls) {
        return null;
    }
	
    private Naked getAssociation(NakedObject fromObject) {
        try {
             Object obj = getMethod.invoke(fromObject.getObject(), new Object[0]);
            
            if(obj == null)  {
                if(getType().isOfType(NakedObjects.getSpecificationLoader().loadSpecification(String.class)) ) {
                    return new StringAdapter("");
                }
                
                throw new NakedObjectRuntimeException(getType().getFullName());
            } else {
                Naked adapter = NakedObjects.getObjectLoader().createAdapterForValue(obj);
                if(adapter == null) {
                    adapter = NakedObjects.getObjectLoader().createAdapterForTransient(obj);
                }
                return adapter;
//                NakedObjects.getPojoAdapterFactory().createAdapter(obj);
            }
            
        } catch (InvocationTargetException e) {
             LOG.error("Exception executing " + getMethod, e.getTargetException());
             throw new NakedObjectRuntimeException(e);
        } catch (IllegalAccessException ignore) {
            LOG.error("Illegal access of " + getMethod, ignore);
            throw new NakedObjectRuntimeException(ignore);
        }
    }

    public boolean isEmpty(MemberIdentifier identifier, NakedObject inObject) {
        throw new NotImplementedException();
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