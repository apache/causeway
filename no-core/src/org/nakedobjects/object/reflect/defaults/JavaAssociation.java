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
package org.nakedobjects.object.reflect.defaults;

import org.nakedobjects.object.NakedObject;

import java.lang.reflect.Method;


public abstract class JavaAssociation extends JavaField {
//	private final static Category LOG = Category.getInstance(JavaAssociation.class);
    protected Method addMethod;
    protected Method removeMethod;

    public JavaAssociation(String name, Class type, Method get, Method add, 
    		Method remove, Method about) {
        super(name, type, get, about, false);
        this.addMethod = add;
        removeMethod = remove;
    }
    
     /**
     Set the data in an NakedObject.  Passes in an existing object to for the EO to reference.
     */
    public void initData(NakedObject inObject, Object setValue) {
  /*      LOG.debug("Association.init() " + inObject + "/" + setValue);

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
        } catch (IllegalAccessException ignore) {
            LOG.error("Illegal access of " + setMethod, ignore);
        }
    */
    	}


    public abstract void addAssociation(NakedObject inObject, NakedObject associate);
    
    public abstract void removeAssociation(NakedObject inObject, NakedObject associate);
}
