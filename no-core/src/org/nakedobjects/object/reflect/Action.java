package org.nakedobjects.object.reflect;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.DefaultHint;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.security.Session;

import java.io.Serializable;

import org.apache.log4j.Category;


public class Action extends NakedObjectMember {

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

        public String getName() {
            return name;
        }
    }

    private final static Category LOG = Category.getInstance(Action.class);
    public final static Type DEBUG = new Type("DEBUG");
    public final static Type EXPLORATION = new Type("EXPLORATION");
    public final static Type USER = new Type("USER");

    private ActionPeer actionDelegate;

    public static Type getType(String type) {
        Type[] types = new Type[] { DEBUG, EXPLORATION, USER };
        for (int i = 0; i < types.length; i++) {
            if (types[i].name.equals(type)) {
                return types[i];
            }
        }
        throw new IllegalArgumentException();
    }

    public Action(String className, String methodName, ActionPeer actionDelegate) {
        super(methodName, new MemberIdentifier(className, methodName, actionDelegate.parameterTypes()));
        this.actionDelegate = actionDelegate;
    }

    protected Naked execute(final NakedObject object, final Naked[] parameters) {
        LOG.debug("Action: invoke " + object + "." + getName());
        try {
            Naked result = actionDelegate.execute(getIdentifier(), object, parameters == null ?  new Naked[0] : parameters);
            return result;
        } catch (ReflectiveActionException e) {
            // TODO Sort out how to deal with exception during reflection
            throw new NakedObjectRuntimeException(e);
        }
    }

    protected Hint getHint(Session session, NakedObject object, Naked[] parameters) {
        if (hasHint()) {
            if (parameters == null) {
                return actionDelegate.getHint(getIdentifier(), session, object, new NakedObject[0]);
            } else {
                return actionDelegate.getHint(getIdentifier(), session, object, parameters);
            }
        } else {
            return new DefaultHint(getLabel());
        }
    }

    /**
     * Return a label string that is specified in the About, if there is one, or
     * is derived from method name, if there is no About or its name is set to
     * null.
     */
    protected String getLabel(Session session, NakedObject object) {
        Hint about = getHint(session, object, parameterStubs());

        return getLabel(about);
    }

    public int getParameterCount() {
        return actionDelegate.getParameterCount();
    }

    public Type getActionType() {
        return actionDelegate.getType();
    }

    public boolean hasHint() {
        return actionDelegate.hasHint();
    }

    /**
     * Returns true if the represented action returns something, else returns
     * false.
     */
    public boolean hasReturn() {
        return getReturnType() != null;
    }

    public NakedObjectSpecification[] parameters() {
        return actionDelegate.parameterTypes();
    }

    public Naked[] parameterStubs() {
        Naked[] parameterValues;
        int paramCount = getParameterCount();
        parameterValues = new Naked[paramCount];
        NakedObjectSpecification[] parameters = parameters();
        for (int i = 0; i < paramCount; i++) {
            NakedObjectSpecification parameter = parameters[i];
            if (parameter.isValue()) {
                parameterValues[i] = parameter.acquireInstance();
            } else {
                parameterValues[i] = null;
            }
        }
        return parameterValues;
    }

    public NakedObjectSpecification getReturnType() {
        return actionDelegate.returnType();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Action [");
        sb.append(super.toString());
        sb.append(",type=");
        sb.append(getActionType());
        sb.append(",returns=");
        sb.append(getReturnType());
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

    public ActionParameterSet getParameters(Session session, NakedObject object) {
        ActionParameterSet parameters = actionDelegate.getParameters(getIdentifier(), session, object, parameterStubs());
        parameters.checkParameters(getIdentifier().toString(), parameters());
        return parameters;
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

