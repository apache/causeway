package org.nakedobjects.object.reflect.internal;

import org.nakedobjects.object.Action;
import org.nakedobjects.object.ActionParameterSet;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedError;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.Session;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.reflect.ActionPeer;
import org.nakedobjects.object.reflect.MemberIdentifierImpl;
import org.nakedobjects.object.reflect.ReflectionException;
import org.nakedobjects.utility.UnexpectedCallException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.log4j.Logger;


public class InternalAction extends InternalMember implements ActionPeer {
    final static Logger LOG = Logger.getLogger(InternalAction.class);
    private final Method actionMethod;
    private final int paramCount;
    private Action.Type type;

    public InternalAction(String className, String name, Action.Type type, Method action) {
        this.type = type;
        this.actionMethod = action;
        paramCount = action.getParameterTypes().length;

        identifeir = new MemberIdentifierImpl(className, name, getParameterTypes());
    }

    public ActionParameterSet createParameterSet(NakedObject object, Naked[] parameters) {
        throw new UnexpectedCallException();
    }

    public Naked execute(NakedObject inObject, Naked[] parameters) {
        if (parameters.length != paramCount) {
            LOG.error(actionMethod + " requires " + paramCount + " parameters, not " + parameters.length);
        }
        try {
            LOG.debug("action: invoke " + inObject + "." + getIdentifier());
            Object[] executionParameters = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                executionParameters[i] = parameters[i] == null ? null : parameters[i].getObject();
            }
            Object result = actionMethod.invoke(inObject.getObject(), executionParameters);
            LOG.debug("  action result " + result);

            if (result != null && result instanceof Naked) {
                return (Naked) result;
            }
            if (result != null) {
                return NakedObjects.getObjectLoader().createAdapterForTransient(result);
            }
        } catch (InvocationTargetException e) {
            e.fillInStackTrace();
            throw new ReflectionException(e);
        } catch (IllegalAccessException e) {
            LOG.error("illegal access of " + actionMethod, e);
        }

        return null;
    }

    public String getDescription() {
        return "";
    }

    public String getName() {
        return null;
    }

    public int getParameterCount() {
        return paramCount;
    }

    public NakedObjectSpecification[] getParameterTypes() {
        Class[] cls = actionMethod.getParameterTypes();
        NakedObjectSpecification[] naked = new NakedObjectSpecification[cls.length];
        for (int i = 0; i < cls.length; i++) {
            naked[i] = nakedClass(cls[i]);
        }
        return naked;
    }

    public NakedObjectSpecification getReturnType() {
        Class returnType = actionMethod.getReturnType();
        boolean hasReturn = returnType != void.class && returnType != NakedError.class;
        return hasReturn ? nakedClass(returnType) : null;
    }

    public Action.Target getTarget() {
        return Action.DEFAULT;
    }

    public Action.Type getType() {
        return type;
    }

    public Consent hasValidParameters(NakedObject object, Naked[] parameters) {
        return Allow.DEFAULT;
    }

    public boolean isAuthorised(Session session) {
        return true;
    }

    public boolean isOnInstance() {
        return !Modifier.isStatic(actionMethod.getModifiers());
    }

    public Consent isUsable(NakedObject target) {
        return Allow.DEFAULT;
    }

    public Consent isVisible(NakedObject target) {
        return Allow.DEFAULT;
    }

    private NakedObjectSpecification nakedClass(Class returnType) {
        return NakedObjects.getSpecificationLoader().loadSpecification(returnType.getName());
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