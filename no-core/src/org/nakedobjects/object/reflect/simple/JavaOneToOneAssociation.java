/*
    Naked Objects - a framework that exposes behaviourally complete
    business objects directly to the user.
    Copyright (C) 2000 - 2003  Naked Objects Group Ltd

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
package org.nakedobjects.object.reflect.simple;

import org.nakedobjects.object.NakedClassManager;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.control.FieldAbout;
import org.nakedobjects.object.reflect.OneToOneAssociationIF;
import org.nakedobjects.security.SecurityContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Category;


public class JavaOneToOneAssociation extends JavaField implements OneToOneAssociationIF {
	private final static Category LOG = Category.getInstance(JavaOneToOneAssociation.class);
    protected Method addMethod;
    protected Method removeMethod;
    protected Method setMethod;

    public JavaOneToOneAssociation(String name, Class type, Method get, Method set, Method add, Method remove,
        Method about) {
        super(name, type, get, about, false);
        this.setMethod = set;
        this.addMethod = add;
        removeMethod = remove;
    }
    
    public About getAbout(SecurityContext context, NakedObject object, NakedObject associate) {
        Method aboutMethod = getAboutMethod();
		
		Class parameter = setMethod.getParameterTypes()[0];
		if(associate != null && !parameter.isAssignableFrom(associate.getClass())) {
			FieldAbout about = new FieldAbout(context, object);
			about.unmodifiable("Invalid type: field must be set with a " + NakedClassManager.getInstance().getNakedClass(parameter.getName()));
			return about;
		}

        if (aboutMethod == null) {
			return new DefaultAbout();
        }

        try {
        		FieldAbout about = new FieldAbout(context, object);
        		Object[] parameters;
                if(aboutMethod.getParameterTypes().length == 2) {
        		    parameters = new Object[] { about, associate };
        		} else {
        		    // default about
        		    parameters = new Object[] { about };
        		}
    		    aboutMethod.invoke(object, parameters);
        		return about;
        } catch (InvocationTargetException e) {
            LOG.error("Exception executing " + aboutMethod, e.getTargetException());
        } catch (IllegalAccessException ignore) {
            LOG.error("Illegal access of " + aboutMethod, ignore);
        }

        return null;
    }

    public boolean hasAddMethod() {
        return addMethod != null;
    }

    /**
     Set the data in an NakedObject.  Passes in an existing object to for the EO to reference.
     */
    public void initData(NakedObject inObject, Object setValue) {
        LOG.debug("local initData() " + inObject.getOid() + "/" + setValue);

        try {
            if (inObject instanceof NakedCollection) {
                throw new RuntimeException();

                //Collection c = (Collection) getMemberMethod().invoke(inObject, new Object[] {});
                //c.removeAll();
                //c.add((Collection) setValue);
            } else {
                if (setMethod == null) {
                    throw new IllegalStateException("can't intialise " + getName() + "  in " +
                        inObject.getClass() + " as there is no set method");
                }

                setMethod.invoke(inObject, new Object[] { setValue });
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

     public void clearAssociation(NakedObject inObject, NakedObject associate) {
        LOG.debug("local clear association " + inObject + "/" + associate);

            try {
                if (removeMethod != null) {
                    removeMethod.invoke(inObject, new Object[] { associate });
                } else {
                    setMethod.invoke(inObject, new Object[] { null });
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("set method expects a " + getType().getName() +
                    " object; not a " + associate.getClass().getName());
            } catch (InvocationTargetException e) {
                LOG.error("Exception executing " + setMethod, e.getTargetException());
                throw (RuntimeException) e.getTargetException();
            } catch (IllegalAccessException ignore) {
                LOG.error("Illegal access of " + setMethod, ignore);
                throw new RuntimeException(ignore.getMessage());
            }
    }

    /**
     Set the data in an NakedObject.  Passes in an existing object to for the EO to reference.
     */
    public void setAssociation(NakedObject inObject, NakedObject associate) {
        LOG.debug("local set association " + getName() + " in " + inObject + " with " + associate);

            try {
                NakedObjectManager objectManager = NakedObjectManager.getInstance();
                objectManager.startTransaction();

                if (associate == null) {
                    if (removeMethod != null) {
                        removeMethod.invoke(inObject, new Object[] { get(inObject) });
                    } else {
                        setMethod.invoke(inObject, new Object[] { associate });
                    }
                } else {
                    if (hasAddMethod()) {
                        addMethod.invoke(inObject, new Object[] { associate });
                    } else {
                        setMethod.invoke(inObject, new Object[] { associate });
                    }
                }

                objectManager.endTransaction();
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

	public NakedObject getAssociation(NakedObject fromObject) {
		return (NakedObject) get(fromObject);
	}
}
