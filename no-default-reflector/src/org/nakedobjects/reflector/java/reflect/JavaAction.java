package org.nakedobjects.reflector.java.reflect;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.application.NakedObjectRuntimeException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedError;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.DefaultHint;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.persistence.defaults.TransactionException;
import org.nakedobjects.object.reflect.ActionParameterSet;
import org.nakedobjects.object.reflect.ActionPeer;
import org.nakedobjects.object.reflect.MemberIdentifier;
import org.nakedobjects.object.reflect.PojoAdapter;
import org.nakedobjects.object.reflect.ReflectriveActionException;
import org.nakedobjects.object.reflect.Action.Type;
import org.nakedobjects.object.security.Session;
import org.nakedobjects.reflector.java.control.SimpleActionAbout;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;


public class JavaAction extends JavaMember implements ActionPeer {
    final static Logger LOG = Logger.getLogger(JavaAction.class);
    private final Method actionMethod;
    private final int paramCount;
    private Type type;

    public JavaAction(String name, Type type, Method action, Method about) {
        super(name, about);
        this.type = type;
        this.actionMethod = action;
        paramCount = action.getParameterTypes().length;
    }

    public Naked execute(MemberIdentifier identifier, NakedObject inObject, Naked[] parameters) throws ReflectriveActionException {
        if (parameters.length != paramCount) {
            LOG.error(actionMethod + " requires " + paramCount + " parameters, not " + parameters.length);
        }

        try {
            Object[] executionParameters = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                executionParameters[i] = parameters[i] == null ? null : parameters[i].getObject();
            }
            Object result = actionMethod.invoke(inObject.getObject(), executionParameters);
            LOG.debug(" action result " + result);
            if (result != null) { 
                return PojoAdapter.createAdapter(result);
            }

        } catch (InvocationTargetException e) {
            e.fillInStackTrace();
            if(e.getTargetException() instanceof TransactionException) {
        	    throw new ReflectriveActionException("TransactionException thrown while executing " + actionMethod + " " + e.getTargetException().getMessage(), e.getTargetException());
        	} else {
	            invocationException("Exception executing " + actionMethod, e);
        	}
        	
        } catch (IllegalAccessException e) {
            throw new ReflectriveActionException("Illegal access of " + actionMethod, e);
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
            SimpleActionAbout about;
            about = new SimpleActionAbout(session, object.getObject(), parameters);

            if (aboutMethod.getName().equals("aboutActionDefault")) {
                aboutMethod.invoke(object.getObject(), new Object[] { about });
            } else {
                Object[] longParams = new Object[parameters.length + 1];
                longParams[0] = about;
                for (int i = 1; i < longParams.length; i++) {
                        longParams[i] = parameters[i - 1] == null ? null : parameters[i - 1].getObject();
                }
                aboutMethod.invoke(object.getObject(), longParams);
            }

            if (about == null) {
                LOG.error("No about returned from " + aboutMethod + " allowing action by default.");
                return new DefaultHint();
            }
            return about;
        } catch (InvocationTargetException e) {
            invocationException("Exception executing " + aboutMethod, e);
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
        Hint hint= getHint(identifier, session, object, parameters);
        if(hint instanceof SimpleActionAbout) {
            SimpleActionAbout about = (SimpleActionAbout) hint;
            return new ActionParameterSet(about.getDefaultParameterValues(), about.getParameterLabels());
        }  else if (hint instanceof DefaultHint) {
            return null;
        }else {
            throw new NakedObjectRuntimeException();
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