package org.nakedobjects.distribution.reflect;


import org.nakedobjects.distribution.ObjectProxy;
import org.nakedobjects.distribution.ObjectRequest;
import org.nakedobjects.distribution.RequestContext;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedClassManager;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.collection.InstanceCollection;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.ActionDelegate;
import org.nakedobjects.object.reflect.Action.Type;

import java.util.Enumeration;
import java.util.Vector;


public class ActionRequest extends ObjectRequest {
    private final static long serialVersionUID = 1L;
    private String actionName;
    private ObjectProxy parameters[];
    private Type actionType;
    
    public ActionRequest(NakedObject object, ActionDelegate action, NakedObject parameters[]) {
        super(object);
        this.actionName = action.getName();
        this.actionType = action.getType();
        this.parameters = new ObjectProxy[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getOid() == null) {
                throw new IllegalArgumentException("All parameters must have an OID to used in a message");
            }
            this.parameters[i] = new ObjectProxy(parameters[i]);
        }
    }

    public NakedObject executeAction() throws ObjectStoreException {
        sendRequest();

        if (response == null) {
            return null;
        } else if (response instanceof ObjectProxy[]) {
        	Object[] array = (Object[]) response;
        	
        	NakedClass cls = NakedClassManager.getInstance().getNakedClass((String) array[0]);
        	Vector elements = new Vector();
        	for (int i = 1; i < array.length; i++) {
        		ObjectProxy proxy = (ObjectProxy) array[i];
				NakedObject recreatedObject = proxy.recreateObject(getLoadedObjects());
				elements.addElement(recreatedObject);
			}
        	
        	return new InstanceCollection(cls, elements);
        } else {
        	ObjectProxy proxy = (ObjectProxy) response;
            return proxy.recreateObject(getLoadedObjects());
        }
    }

    protected void generateResponse(RequestContext server) {
        try {
            NakedObject object = getObject(server.getLoadedObjects());
            int parameterCount = parameters.length;
            NakedObject parameters[] = new NakedObject[parameterCount];
			NakedClass parameterTypes[] = new NakedClass[parameterCount];
			
            NakedObjectManager objectManager = server.getObjectManager();
            for (int i = 0; i < parameterCount; i++) {
                NakedObject parameter = objectManager.getObject(parameters[i].getOid(), parameters[i].getNakedClass());
                parameters[i] = parameter;
                parameterTypes[i] = parameter.getNakedClass();
            }
            
            Action action = object.getNakedClass().getObjectAction(actionType, actionName, parameterTypes);
            NakedObject result = action.execute(object, parameters);

            
            if(result == null) {
            	response = null;
            } else if(result instanceof InstanceCollection) {
            	InstanceCollection instances = (InstanceCollection) result;
            	Object[] array = new Object[instances.size() + 1];
            	int index = 0;
            	array[index] = instances.getType().getName();
            	Enumeration e = instances.elements();
            	while (e.hasMoreElements()) {
            		index++;
					array[index] = new ObjectProxy((NakedObject) e.nextElement());
				}
            	response = array;

            } else {
            	response = new ObjectProxy(result);	
            }
        } catch (ObjectStoreException e) {
            response = e;
        }
    }

    public String toString() {
    	String params = "";
    	for (int i = 0; i < parameters.length; i++) {
			params = (i > 0) ? "," : "" + parameters[i];
		}
        return "ActionRequest [" + externalOid + "." + actionName + "(" + params + ")]";
    }
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2003  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/
