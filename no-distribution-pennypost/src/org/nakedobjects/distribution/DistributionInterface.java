package org.nakedobjects.distribution;

import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.Oid;


public interface DistributionInterface {

    AboutData aboutAction(SessionId sessionId, ObjectReference target, ActionType actionType, String actionName,
            ParameterSet parameters);

    AboutData aboutValue(SessionId sessionId, ObjectReference target, String fieldName);

    void associateObject(SessionId sessionId, ObjectReference target, String fieldName, ObjectReference associate);

    void destroyObject(SessionId sessionId, ObjectReference target);

    void dissociateObject(SessionId sessionId, ObjectReference target, String fieldName, ObjectReference associate);

    ObjectData executeAction(SessionId sessionId, ObjectReference target, ActionType actionType, String actionName,
            ParameterSet parameters);

    ObjectData getAssociation(SessionId sessionId, ObjectReference target, String fieldName);

    InstanceSet findInstances(SessionId sessionId, String cls, String criteria) throws RemoteException;

    InstanceSet findInstances(SessionId sessionId, ObjectData pattern) throws RemoteException;

    InstanceSet allInstances(SessionId sessionId, String cls, boolean includeSubclasses);

    NakedClass getNakedClass(String string);

    ObjectData getObjectRequest(SessionId sessionId, ObjectReference reference) throws RemoteException;

    boolean hasInstances(SessionId sessionId, String cls);

    Oid makePersistentRequest(SessionId sessionId, ObjectData object);

    int numberOfInstances(SessionId sessionId, String cls);

    ObjectData resolve(SessionId sessionId, ObjectReference reference);

    void saveValue(SessionId sessionId, ObjectReference target, String fieldName, String encodedValue)
            throws RemoteException;

    long serialNumber(SessionId sessionId, String name);
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
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