package org.nakedobjects.distribution.example;


import org.nakedobjects.distribution.ActionType;
import org.nakedobjects.distribution.HintData;
import org.nakedobjects.distribution.InstanceSet;
import org.nakedobjects.distribution.ObjectData;
import org.nakedobjects.distribution.ObjectReference;
import org.nakedobjects.distribution.ParameterSet;
import org.nakedobjects.distribution.RemoteObjectFactory;
import org.nakedobjects.distribution.SessionId;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.TypedNakedCollection;
import org.nakedobjects.object.UnsupportedFindException;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.Action.Type;
import org.nakedobjects.object.security.Session;

public class SimpleRemoteObjectFactory implements RemoteObjectFactory {
    private long sessionId = 99L;

    public HintData createAboutData(Hint hint) {
        return new SimpleAboutData(hint);
    }

    public ActionType createActionType(Type type) {
        return new SimpleActionType(type);
    }

    public InstanceSet createInstancesSet(TypedNakedCollection instances) {
        return new SimpleInstanceSet(instances, this);
    }

    public ObjectData createObjectData(NakedObject object) {
        return new SimpleObjectData(object, false, this);
    }

    public ObjectData createObjectDataGraph(NakedObject object) {
        return new SimpleObjectData(object, true, this);
    }

    public ObjectReference createObjectReference(NakedObject object) {
        return new SimpleObjectReference(object);
    }

    public ObjectReference createObjectReference(Oid oid, String className) {
        return new SimpleObjectReference(oid, className);
    }

    public ParameterSet createParameterSet(Naked[] parameters) {
        return null;
    }

    public Exception createRemoteException(UnsupportedFindException e) {
        return null;
    }

    public SessionId createSessionId(Session session) {
        return new SimpleSessionId(sessionId);
    }

}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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