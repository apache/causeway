package org.nakedobjects.object.reflect.simple;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedClassManager;
import org.nakedobjects.object.NakedError;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.ObjectNotFoundException;
import org.nakedobjects.object.TransactionException;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.control.ActionAbout;
import org.nakedobjects.object.reflect.ActionDelegate;
import org.nakedobjects.object.reflect.Action.Type;
import org.nakedobjects.security.SecurityContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;


public class JavaAction extends JavaMember implements ActionDelegate {
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

    public NakedObject execute(NakedObject object, Naked[] parameters) {
        if (parameters.length != paramCount) {
            LOG.error(actionMethod + " requires " + paramCount + " parameters, not " + parameters.length);
        }
        NakedObjectManager objectManager = NakedObjectManager.getInstance();

        try {
            LOG.debug("Action: invoke " + object + "." + getName());
            objectManager.startTransaction();

            /*
             * TODO the object that we are invoking this method on, and the
             * parameters, need to be part of the transaction, and not the same
             * objects that other clients are using.
             */
            Object result;
            if(object.getOid() == null || !requiresTransaction()) {
                // non-persistent
                result = actionMethod.invoke(object, parameters);
            } else {
                // persistent
	            NakedObject transactionObject = objectManager.getObject(object);
	            
	            Naked[] transactionParameters = new Naked[parameters.length];
	            for (int i = 0; i < parameters.length; i++) {
	                if(parameters[i] instanceof NakedObject) {
	                    transactionParameters[i] = objectManager.getObject((NakedObject) parameters[i]);
	                } else {
	                    transactionParameters[i] = parameters[i];
	                }
                }
	            
	            result = actionMethod.invoke(transactionObject, transactionParameters);
            }
            
            LOG.debug(" action result " + result);

            objectManager.endTransaction();
            if (result != null && result instanceof NakedObject) { return (NakedObject) result; }
        } catch (InvocationTargetException e) {
        	if(e.getTargetException() instanceof TransactionException) {
        	    LOG.info("TransactionException thrown while executing " + actionMethod + " " + e.getTargetException().getMessage());
        	} else {
	            LOG.error("Exception executing " + actionMethod + "; aborted", e.getTargetException());
        	}
            objectManager.abortTransaction();
        } catch (IllegalAccessException e) {
            LOG.error("Illegal access of " + actionMethod, e);
            objectManager.abortTransaction();
        } catch (ObjectNotFoundException e) {
            LOG.error("Non-existing target or parameter used in " + actionMethod, e);
            objectManager.abortTransaction();
        }

        return null;
    }

    public boolean requiresTransaction() {
        return true ; // testing
        
     /*   Class[] exceptions = actionMethod.getExceptionTypes();
        for (int i = 0; i < exceptions.length; i++) {
            if(exceptions[i] == TransactionException.class) {
                return true;
            }
        }
        return false;
     */
        
    }

    public About getAbout(SecurityContext context, NakedObject object, Naked[] parameters) {
        if (parameters.length != paramCount) {
            LOG.error(actionMethod + " requires " + paramCount + " parameters, not " + parameters.length);
        }

        Method aboutMethod = getAboutMethod();

        if (aboutMethod == null) { return new DefaultAbout(); }

        try {
            About about;
            about = new ActionAbout(context, object);

            if (aboutMethod.getName().equals("aboutActionDefault")) {
                aboutMethod.invoke(object, new Object[] { about });
            } else {
                Object[] longParams = new Object[parameters.length + 1];
                longParams[0] = about;
                System.arraycopy(parameters, 0, longParams, 1, parameters.length);
                aboutMethod.invoke(object, longParams);
            }

            if (about == null) {
                LOG.error("No about returned from " + aboutMethod + " allowing action by default.");
                return new DefaultAbout();
            }
            return about;
        } catch (InvocationTargetException e) {
            LOG.error("Exception executing " + aboutMethod, e.getTargetException());
        } catch (IllegalAccessException ignore) {
            LOG.error("Illegal access of " + aboutMethod, ignore);
        }

        return new DefaultAbout();
    }

    public int getParameterCount() {
        return paramCount;
    }

    public Type getType() {
        return type;
    }

    private NakedClass nakedClass(Class returnType) {
        return NakedClassManager.getInstance().getNakedClass(returnType.getName());
    }

    public NakedClass[] parameterTypes() {
        Class[] cls = actionMethod.getParameterTypes();
        NakedClass[] naked = new NakedClass[cls.length];
        for (int i = 0; i < cls.length; i++) {
            naked[i] = nakedClass(cls[i]);
        }
        return naked;
    }

    public NakedClass returnType() {
        Class returnType = actionMethod.getReturnType();
        boolean hasReturn = returnType != void.class && returnType != NakedError.class;
        return hasReturn ? nakedClass(returnType) : null;
    }
}

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