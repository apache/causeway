/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2003 Naked Objects Group
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

package org.nakedobjects.object.reflect;

import org.apache.log4j.Category;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.control.About;
import org.nakedobjects.security.SecurityContext;

import java.io.Serializable;

public class Action extends Member {

    public static class Type implements Serializable {
        private final static long serialVersionUID = 1L;
        private String name;

        private Type(String name) {
            this.name = name;
        }

        public boolean equals(Object object) {
            if (object instanceof Action.Type) {
                Action.Type type = (Type) object;
                return name.equals(type.name);
            } else {
                return false;
            }
        }

        public int hashCode() {
            return name.hashCode();
        }

        public String toString() {
            return name;
        }
    }

    public final static Type DEBUG = new Type("DEBUG");

    public final static Type EXPLORATION = new Type("EXPLORATION");

    final static Category LOG = Category.getInstance(Action.class);

    public final static Type USER = new Type("USER");

    private ActionDelegate actionDelegate;

    public Action(String name, ActionDelegate actionDelegate) {
        super(name);
        this.actionDelegate = actionDelegate;
    }

    public boolean canAccess(SecurityContext context, NakedObject object) {
        return getAbout(context, object).canAccess().isAllowed();
    }
    
    public boolean canUse(SecurityContext context, NakedObject object) {
        return getAbout(context, object).canUse().isAllowed();
    }

    public NakedObject execute(NakedObject object) {
        return execute(object, new NakedObject[0]);
    }

    public NakedObject execute(NakedObject object, NakedObject parameter1) {
        return execute(object, new NakedObject[] { parameter1 });
    }

    public NakedObject execute(NakedObject object, NakedObject[] parameters) {
        return actionDelegate.execute(object, parameters);
    }

    public About getAbout(SecurityContext context, NakedObject object) {
        return getAbout(context, object, new NakedObject[0]);
    }

    public About getAbout(SecurityContext context, NakedObject object, NakedObject parameter1) {
        return getAbout(context, object, new NakedObject[] { parameter1 });
    }

    public About getAbout(SecurityContext context, NakedObject object, NakedObject[] parameters) {
        if (hasAbout()) {
            return actionDelegate.getAbout(context, object, parameters);
        } else {
            return new DefaultAbout();
        }
    }

    /**
     * Return a label string that is specified in the About, if there is one, or
     * is derived from method name, if there is no About or its name is set to
     * null.
     */
    public String getLabel(SecurityContext context, NakedObject object) {
        About about = getAbout(context, object, new NakedObject[getParameterCount()]);

        return getLabel(about);
    }

    public int getParameterCount() {
        return actionDelegate.getParameterCount();
    }

    public Type getType() {
        return actionDelegate.getType();
    }

    public boolean hasAbout() {
        return actionDelegate.hasAbout();
    }

    /**
     * Returns true if the represented action returns something, else returns
     * false.
     */
    public boolean hasReturn() {
        return returns() != null;
    }

    public NakedClass[] parameters() {
        return actionDelegate.parameterTypes();
    }

    public NakedClass returns() {
        return actionDelegate.returnType();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Action [");
        sb.append(super.toString());
        sb.append(",type=");
        sb.append(getType());
        sb.append(",returns=");
        sb.append(returns());
        sb.append(",parameters={");
        for (int i = 0; i < parameters().length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(parameters()[i]);
        }
        sb.append("}]");
        return sb.toString();
    }

}