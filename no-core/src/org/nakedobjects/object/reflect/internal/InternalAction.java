package org.nakedobjects.object.reflect.internal;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedError;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.DefaultHint;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.ActionParameterSet;
import org.nakedobjects.object.reflect.ActionPeer;
import org.nakedobjects.object.reflect.MemberIdentifier;
import org.nakedobjects.object.reflect.ReflectionException;
import org.nakedobjects.object.reflect.Action.Type;
import org.nakedobjects.object.security.Session;
import org.nakedobjects.utility.UnexpectedCallException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;


public class InternalAction extends InternalMember implements ActionPeer {
    final static Logger LOG = Logger.getLogger(InternalAction.class);
    private final Method actionMethod;
    private final int paramCount;
    private Type type;

    public InternalAction(String name, Type type, Method action, Method about) {
        super(name, about);
        this.type = type;
        this.actionMethod = action;
        paramCount = action.getParameterTypes().length;
    }

    public Naked execute(MemberIdentifier identifier, NakedObject inObject, Naked[] parameters) {
        if (parameters.length != paramCount) {
            LOG.error(actionMethod + " requires " + paramCount + " parameters, not " + parameters.length);
        }
        try {
            LOG.debug("Action: invoke " + inObject + "." + getName());
            Object[] executionParameters = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                executionParameters[i] = parameters[i] == null ? null : parameters[i].getObject();
            }
            Object result = actionMethod.invoke(inObject.getObject(), executionParameters);
            LOG.debug(" action result " + result);

            if (result != null && result instanceof Naked) { return (Naked) result; }
            if (result != null) { return NakedObjects.getPojoAdapterFactory().createAdapter(result); }
        } catch (InvocationTargetException e) {
            e.fillInStackTrace();
            throw new ReflectionException(e);
        } catch (IllegalAccessException e) {
            LOG.error("Illegal access of " + actionMethod, e);
        }

        return null;
    }

    public Hint getHint(MemberIdentifier identifier, Session session, NakedObject object, Naked[] parameters) {
        if (parameters.length != paramCount) {
            LOG.error(actionMethod + " requires " + paramCount + " parameters, not " + parameters.length);
        }

        Method aboutMethod = getAboutMethod();

        if (aboutMethod == null) { return new DefaultHint(); }

        try {
            InternalAbout about;
            about = new InternalAbout();

            if (aboutMethod.getName().equals("aboutActionDefault")) {
                aboutMethod.invoke(object.getObject(), new Object[] { about });
            } else {
                Object[] longParams = new Object[parameters.length + 1];
                longParams[0] = about;
              //  System.arraycopy(parameters, 0, longParams, 1, parameters.length);
                for (int i = 1; i < longParams.length; i++) {
                 //   if(parameters[i - 1] instanceof Naked) {
                  //      longParams[i] = parameters[i - 1];
                 //   } else {
                        longParams[i] = parameters[i - 1] == null ? null : ((NakedObject) parameters[i - 1]).getObject();
                  //  }
                }
                aboutMethod.invoke(object.getObject(), longParams);
            }

            if (about == null) {
                LOG.error("No about returned from " + aboutMethod + " allowing action by default.");
                return new DefaultHint();
            }
            return about;
        } catch (InvocationTargetException e) {
            LOG.error("Exception executing " + aboutMethod, e.getTargetException());
        } catch (IllegalAccessException ignore) {
            LOG.error("Illegal access of " + aboutMethod, ignore);
        }

        return new DefaultHint();
    }

    public int getParameterCount() {
        return paramCount;
    }

    public Type getType() {
        return type;
    }

    private NakedObjectSpecification nakedClass(Class returnType) {
        return NakedObjects.getSpecificationLoader().loadSpecification(returnType.getName());
    }

    public NakedObjectSpecification[] parameterTypes() {
        Class[] cls = actionMethod.getParameterTypes();
        NakedObjectSpecification[] naked = new NakedObjectSpecification[cls.length];
        for (int i = 0; i < cls.length; i++) {
            naked[i] = nakedClass(cls[i]);
        }
        return naked;
    }

    public NakedObjectSpecification returnType() {
        Class returnType = actionMethod.getReturnType();
        boolean hasReturn = returnType != void.class && returnType != NakedError.class;
        return hasReturn ? nakedClass(returnType) : null;
    }

    public ActionParameterSet getParameters(MemberIdentifier identifier, Session session, NakedObject object, Naked[] parameters) {
        throw new UnexpectedCallException();
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