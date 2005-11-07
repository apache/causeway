package org.nakedobjects.reflector.java.reflect;

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
import org.nakedobjects.object.control.DefaultHint;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.ActionParameterSetImpl;
import org.nakedobjects.object.reflect.ActionPeer;
import org.nakedobjects.object.reflect.MemberIdentifier;
import org.nakedobjects.object.reflect.ReflectionException;
import org.nakedobjects.object.reflect.ReflectiveActionException;
import org.nakedobjects.object.transaction.TransactionException;
import org.nakedobjects.reflector.java.control.SimpleActionAbout;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

/*
 * TODO (in all Java...Peer classes) make all methods throw ReflectiveActionException when 
 * an exception occurs when calling a method reflectively (see execute method).  Then instead of 
 * calling invocationExcpetion() the exception will be passed though, and dealt with generally by 
 * the reflection package (which will be the same for all reflectors and will allow the message to
 * be better passed back to the client).
 */
public class JavaAction extends JavaMember implements ActionPeer {
    final static Logger LOG = Logger.getLogger(JavaAction.class);
    private final Method actionMethod;
    private final int paramCount;
    private Action.Type type;
    private Action.Target target;

    public JavaAction(MemberIdentifier identifier, Action.Type type, Action.Target target, Method action, Method about) {
        super(identifier, about);
        this.type = type;
        this.actionMethod = action;
        this.target = target;
        paramCount = action.getParameterTypes().length;
    }

    public Naked execute(NakedObject inObject, Naked[] parameters) throws ReflectiveActionException {
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
                Naked adapter;
                adapter = NakedObjects.getObjectLoader().createAdapterForCollection(result, null);
                if(adapter == null) {
                    adapter = NakedObjects.getObjectLoader().getAdapterFor(result);
                    if(adapter == null) {
                        adapter = NakedObjects.getObjectLoader().createAdapterForTransient(result);
                    }
                }
                return adapter;
            } else {
                return null;
            }

        } catch (InvocationTargetException e) { 
            if(e.getTargetException() instanceof TransactionException) {
        	    throw new ReflectiveActionException("TransactionException thrown while executing " + actionMethod + " " + e.getTargetException().getMessage(), e.getTargetException());
        	} else {
	            invocationException("Exception executing " + actionMethod, e);
	            return null;
        	}
        	
        } catch (IllegalAccessException e) {
            throw new ReflectiveActionException("Illegal access of " + actionMethod, e);
        }

    }

    public Object getExtension(Class cls) {
        return null;
    }

    public String getName() {
        return null;
    }
    
    private Hint getHint(MemberIdentifier identifier, NakedObject object, Naked[] parameters) {
        if(parameters == null) {
            parameters = new Naked[0];
        }
        if (parameters.length != paramCount) {
            LOG.error(actionMethod + " requires " + paramCount + " parameters, not " + parameters.length);
        }

        Method aboutMethod = getAboutMethod();

        if (aboutMethod == null) { return new DefaultHint(); }

        try {
            SimpleActionAbout hint;
            hint = new SimpleActionAbout(NakedObjects.getCurrentSession(), object.getObject(), parameters);

            if (aboutMethod.getName().equals("aboutActionDefault")) {
                aboutMethod.invoke(object.getObject(), new Object[] { hint });
            } else {
                Object[] longParams = new Object[parameters.length + 1];
                longParams[0] = hint;
                for (int i = 1; i < longParams.length; i++) {
                        longParams[i] = parameters[i - 1] == null ? null : parameters[i - 1].getObject();
                }
                aboutMethod.invoke(object.getObject(), longParams);
            }

            if (hint == null) {
                LOG.error("no about returned from " + aboutMethod + " allowing action by default.");
                return new DefaultHint();
            }
            if(hint.getDescription().equals("")) {
                hint.setDescription("Invoke action " + getIdentifier());
            }

            return hint;
        } catch (InvocationTargetException e) {
            invocationException("Exception executing " + aboutMethod, e);
        } catch (IllegalAccessException ignore) {
            LOG.error("illegal access of " + aboutMethod, ignore);
        }

        return new DefaultHint();
    }

    public int getParameterCount() {
        return paramCount;
    }

    public Action.Type getType() {
        return type;
    }
    
    public Action.Target getTarget() {
        return target;
    }

    private NakedObjectSpecification specification(Class returnType) {
        return NakedObjects.getSpecificationLoader().loadSpecification(returnType.getName());
    }

    public NakedObjectSpecification[] getParameterTypes() {
        Class[] cls = actionMethod.getParameterTypes();
        NakedObjectSpecification[] naked = new NakedObjectSpecification[cls.length];
        for (int i = 0; i < cls.length; i++) {
            naked[i] = specification(cls[i]);
        }
        return naked;
    }

    public NakedObjectSpecification getReturnType() {
        Class returnType = actionMethod.getReturnType();
        boolean hasReturn = returnType != void.class && returnType != NakedError.class;
        return hasReturn ? specification(returnType) : null;
    }
    
    public ActionParameterSet createParameterSet(NakedObject object, Naked[] parameters) {
        Hint hint= getHint(getIdentifier(), object, parameters);
        if(hint instanceof SimpleActionAbout) {
            SimpleActionAbout about = (SimpleActionAbout) hint;
            return new ActionParameterSetImpl(about.getDefaultParameterValues(), about.getParameterLabels(), about.getRequired());
        }  else if (hint instanceof DefaultHint) {
            return null;
        }else {
            throw new ReflectionException();
        }
    }
    
    public String toString() {
        StringBuffer parameters = new StringBuffer();
        Class[] types = actionMethod.getParameterTypes();
        if(types.length == 0) {
            parameters.append("none");
        }
        for (int i = 0; i < types.length; i++) {
            if(i > 0) {
                parameters.append("/");
            }
            parameters.append(types[i]);
        }
        return "JavaAction [name=" + actionMethod.getName()  + ",type=" + type.getName() + ",parameters=" + parameters + "]";
    }

    public Consent isUsable(NakedObject target) {
        return Allow.DEFAULT;
    }

    public Consent hasValidParameters(NakedObject object, Naked[] parameters) {
        return getHint(null, object, parameters).canUse();
   }

    public String getDescription() {
        return "";
    }

    public Consent isVisible(NakedObject target) {
        return Allow.DEFAULT;
    }

    public boolean isAuthorised(Session session) {
        return true;
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