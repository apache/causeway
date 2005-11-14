package org.nakedobjects.object.reflect;

import org.nakedobjects.object.Action;
import org.nakedobjects.object.ActionParameterSet;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.control.Consent;

import org.apache.log4j.Logger;


public class ActionImpl extends AbstractNakedObjectMember implements Action {
    private final static Logger LOG = Logger.getLogger(Action.class);

    public static Action.Type getType(String type) {
        Action.Type[] types = new Action.Type[] { Action.DEBUG, Action.EXPLORATION, Action.USER };
        for (int i = 0; i < types.length; i++) {
            if (types[i].getName().equals(type)) {
                return types[i];
            }
        }
        throw new IllegalArgumentException();
    }

    private ActionPeer reflectiveAdapter;

    public ActionImpl(String className, String methodName, ActionPeer actionDelegate) {
        super(methodName);
        this.reflectiveAdapter = actionDelegate;
    }

    public NakedObjectSpecification[] getParameterTypes() {
        return reflectiveAdapter.getParameterTypes();
    }

    public Naked execute(final NakedObject object, final Naked[] parameters) {
        LOG.debug("execute action " + object + "." + getId());
        Naked[] params = parameters == null ? new Naked[0] : parameters;
        Naked result = reflectiveAdapter.execute(object, params);
        return result;
    }

    public Action.Target getTarget() {
        return reflectiveAdapter.getTarget();
    }

    public Action.Type getType() {
        return reflectiveAdapter.getType();
    }

    public String getDescription() {
        return reflectiveAdapter.getDescription();
    }

    public Object getExtension(Class cls) {
        return reflectiveAdapter.getExtension(null);
    }

    public Class[] getExtensions() {
        return reflectiveAdapter.getExtensions();
    }
    
    /**
     * Return the default label for this member. This is based on the name of this member.
     * 
     * @see #getId()
     */
    public String getName() {
        String label = reflectiveAdapter.getName();
        return label == null ? defaultLabel : label;
    }

    public int getParameterCount() {
        return reflectiveAdapter.getParameterCount();
    }

    public ActionParameterSet getParameterSet(NakedObject object) {
        ActionParameterSet parameters = reflectiveAdapter.createParameterSet(object, parameterStubs());
        if (parameters != null) {
            parameters.checkParameters(reflectiveAdapter.getIdentifier().toString(), getParameterTypes());
        }
        return parameters;
    }

    public NakedObjectSpecification getReturnType() {
        return reflectiveAdapter.getReturnType();
    }

    /**
     * Returns true if the represented action returns something, else returns false.
     */
    public boolean hasReturn() {
        return getReturnType() != null;
    }

    public Consent isUsable(NakedObject target) {
        return reflectiveAdapter.isUsable(target);
    }

    public boolean isAuthorised() {
        return reflectiveAdapter.isAuthorised(NakedObjects.getCurrentSession());
    }

    public Consent isVisible(NakedObject target) {
        return reflectiveAdapter.isVisible(target);
    }

    public Naked[] parameterStubs() {
        Naked[] parameterValues;
        int paramCount = getParameterCount();
        parameterValues = new Naked[paramCount];
        NakedObjectSpecification[] parameters = getParameterTypes();
        for (int i = 0; i < paramCount; i++) {
            NakedObjectSpecification parameter = parameters[i];
            if (parameter.isValue()) {
                parameterValues[i] = NakedObjects.getObjectLoader().createValueInstance(parameter);
            } else {
                parameterValues[i] = null;
            }
        }
        return parameterValues;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Action [");
        sb.append(super.toString());
        sb.append(",type=");
        sb.append(getType());
        sb.append(",returns=");
        sb.append(getReturnType());
        sb.append(",parameters={");
        for (int i = 0; i < getParameterTypes().length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(getParameterTypes()[i]);
        }
        sb.append("}]");
        return sb.toString();
    }

    public Consent hasValidParameters(NakedObject object, Naked[] parameters) {
        return reflectiveAdapter.hasValidParameters(object, parameters);
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

