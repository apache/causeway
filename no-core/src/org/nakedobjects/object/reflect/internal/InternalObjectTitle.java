package org.nakedobjects.object.reflect.internal;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.reflect.ObjectTitle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Category;


public class InternalObjectTitle implements ObjectTitle {
    private final static Category LOG = Category.getInstance(InternalObjectTitle.class);

    private Method titleMethod;

    public InternalObjectTitle(Method titleMethod) {
        this.titleMethod = titleMethod;
    }

    public String title(NakedObject object) {
        try {
    /*        if(object == null) {
                return "";
            }
        */
            Object title = titleMethod.invoke(object.getObject(), new Object[0]);
            return title == null ? "" : title.toString();
        } catch (InvocationTargetException e) {
            LOG.error("exception executing " + titleMethod, e.getTargetException());
        } catch (IllegalAccessException ignore) {
            LOG.error("illegal access of " + titleMethod, ignore);
        }

        return "title error...";
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