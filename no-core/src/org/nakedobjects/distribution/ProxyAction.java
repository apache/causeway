package org.nakedobjects.distribution;

import org.nakedobjects.object.LoadedObjects;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.ActionPeer;
import org.nakedobjects.object.reflect.ActionParameterSet;
import org.nakedobjects.object.reflect.Action.Type;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.object.security.Session;

import org.apache.log4j.Logger;


public final class ProxyAction implements ActionPeer {
    final static Logger LOG = Logger.getLogger(ProxyAction.class);
    private ClientDistribution connection;
    private boolean fullProxy = false;
    private ActionPeer local;
    private final LoadedObjects loadedObjects;
    private final ObjectDataFactory objectDataFactory;

    public ProxyAction(final ActionPeer local, final ClientDistribution connection, final LoadedObjects loadedObjects,
            ObjectDataFactory objectDataFactory) {
        this.local = local;
        this.connection = connection;
        this.loadedObjects = loadedObjects;
        this.objectDataFactory = objectDataFactory;
    }

    public Naked execute(NakedObject target, Naked[] parameters) {
        if (isPersistent(target)) {
            String[] parameterTypes = pararmeterTypes();
            ObjectData[] parameterObjectData = parameterValues(parameters);
            ObjectData targetObjectData = connection.executeAction(ClientSession.getSession(), getType().getName(), getName(),
                    parameterTypes, target.getOid(), target.getSpecification().getFullName(), parameterObjectData);
            NakedObject returnedObject;
            returnedObject = targetObjectData == null ? null : targetObjectData.recreate(loadedObjects);
            return returnedObject;
        } else {
            return local.execute(target, parameters);
        }
    }

    private ObjectData[] parameterValues(Naked[] parameters) {
        ObjectData parameterObjectData[] = new ObjectData[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            if(parameters[i] != null) {
                parameterObjectData[i] = objectDataFactory.createObjectData((NakedObject) parameters[i], 0);
            }
        }
        return parameterObjectData;
    }

    private String[] pararmeterTypes() {
        NakedObjectSpecification[] parameterTypes = parameterTypes();
        String[] parameterTypeNames = new String[parameterTypes.length];
        for (int i = 0; i < parameterTypeNames.length; i++) {
            parameterTypeNames[i] = parameterTypes[i].getFullName();
        }
        return parameterTypeNames;
    }

    public Hint getHint(Session session, NakedObject object, Naked[] parameters) {
        if (isPersistent(object) && fullProxy) {
            String[] parameterTypes = pararmeterTypes();
            ObjectData[] parameterObjectData = parameterValues(parameters);
            return connection.getActionHint(session, getType().getName(), getName(), parameterTypes, object.getOid(), object
                    .getSpecification().getFullName(), parameterObjectData);
        } else {
            return local.getHint(session, object, parameters);
        }
    }

    public String getName() {
        return local.getName();
    }

    public int getParameterCount() {
        return local.getParameterCount();
    }

    public Type getType() {
        return local.getType();
    }

    public boolean hasHint() {
        return local.hasHint();
    }

    private boolean isPersistent(NakedObject object) {
        return object.getOid() != null;
    }

    public NakedObjectSpecification[] parameterTypes() {
        return local.parameterTypes();
    }

    public NakedObjectSpecification returnType() {
        return local.returnType();
    }

    public ActionParameterSet getParameters(Session session, NakedObject object, NakedObjectSpecification[] parameterTypes) {
        return local.getParameters(session, object, parameterTypes);
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