package org.nakedobjects.object.reflect.defaults;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedError;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.ObjectNotFoundException;
import org.nakedobjects.object.TransactionException;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.control.defaults.SimpleActionAbout;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.ActionSpecification.Type;
import org.nakedobjects.object.security.Session;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;


public class JavaAction extends JavaMember implements Action {
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

    public NakedObject execute(NakedObject inObject, Naked[] parameters) {
        if (parameters.length != paramCount) {
            LOG.error(actionMethod + " requires " + paramCount + " parameters, not " + parameters.length);
        }
        NakedObjectManager objectManager = inObject.getContext().getObjectManager();

        try {
            LOG.debug("Action: invoke " + inObject + "." + getName());
            objectManager.startTransaction();

            /*
             * TODO the object that we are invoking this method on, and the
             * parameters, need to be part of the transaction, and not the same
             * objects that other clients are using.
             */
            Object result;
            if(inObject.getOid() == null || !requiresTransaction()) {
                // non-persistent
                result = actionMethod.invoke(inObject, parameters);
            } else {
                // persistent
	            NakedObject transactionObject = objectManager.getObject(inObject.getOid(), inObject.getSpecification());
	            
	            Naked[] transactionParameters = new Naked[parameters.length];
	            for (int i = 0; i < parameters.length; i++) {
	                if(parameters[i] instanceof NakedObject) {
	                    NakedObject parameter = (NakedObject) parameters[i];
                        transactionParameters[i] = objectManager.getObject(parameter.getOid(), parameter.getSpecification());
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
            e.fillInStackTrace();
            
            if(e.getTargetException() instanceof TransactionException) {
	            objectManager.abortTransaction();
	            
	            if(JavaReflector.isStrict()) {
	                new ReflectionErrorDialog("Exception whilst reflectively getting about for " + getName() + " on " + inObject, e);
	                throw new NakedObjectRuntimeException("Exception whilst reflectively getting about for " + getName() + " on " + inObject, e);
	             } else {
	        	    LOG.info("TransactionException thrown while executing " + actionMethod + " " + e.getTargetException().getMessage());
	             }

        	} else {
	            invocationException("Exception executing " + actionMethod + "; aborted", e);
        	}
        	
            
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

    public About getAbout(Session session, NakedObject object, Naked[] parameters) {
        if (parameters.length != paramCount) {
            LOG.error(actionMethod + " requires " + paramCount + " parameters, not " + parameters.length);
        }

        Method aboutMethod = getAboutMethod();

        if (aboutMethod == null) { return new DefaultAbout(); }

        try {
            About about;
            about = new SimpleActionAbout(session, object, parameters);

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
            invocationException("Exception whilst reflectively getting about for " + getName() + " on " + object, e);
        } catch (IllegalAccessException e) {
            if(JavaReflector.isStrict()) {
                throw new NakedObjectRuntimeException("Exception whilst reflectively getting about for " + getName() + " on " + object, e);
            } else {
                LOG.error("Illegal access of " + aboutMethod, e);
            }
       }

        return new DefaultAbout();
    }

    public int getParameterCount() {
        return paramCount;
    }

    public Type getType() {
        return type;
    }

    private NakedObjectSpecification nakedClass(Class returnType) {
        return NakedObjectSpecificationLoader.getInstance().loadSpecification(returnType.getName());
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