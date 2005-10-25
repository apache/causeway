package org.nakedobjects.distribution;

import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.persistence.InstancesCriteria;
import org.nakedobjects.object.security.Session;

public class DummyDistribution implements Distribution {

    private ObjectData makePersistentResults;

    public void abortTransaction(Session session) {}

    public ObjectData[] allInstances(Session session, String fullName, boolean includeSubclasses) {
        return null;
    }

    public void clearAssociation(Session session, String fieldIdentifier, ReferenceData target, ReferenceData associate) {}

    public void destroyObject(Session session, ReferenceData object) {}

    public void endTransaction(Session session) {}

    public Data executeAction(Session session, String actionType, String actionIdentifier, ObjectData target, Data[] parameters) {
        return null;
    }

    public ObjectData[] findInstances(Session session, InstancesCriteria criteria) {
        return null;
    }

    public Hint getActionHint(Session session, String actionType, String actionIdentifier, ObjectData target, Data[] parameters) {
        return null;
    }

    public ObjectData resolveImmediately(Session session, ReferenceData target) {
        return null;
    }

    public Data resolveField(Session session, ReferenceData data, String name) {
        return null;
    }

    public boolean hasInstances(Session session, String fullName) {
        return false;
    }

    public ObjectData makePersistent(Session session, ObjectData object) {
        return makePersistentResults;
    }

    public int numberOfInstances(Session sessionId, String fullName) {
        return 0;
    }

    public void setAssociation(Session session, String fieldIdentifier, ReferenceData target, ReferenceData associate) {}

    public void setValue(Session session, String fieldIdentifier, ReferenceData target, Object associate) {}

    public void startTransaction(Session session) {}

    public void setupMakePersistentResults(ObjectData makePersistentResults) {
        this.makePersistentResults = makePersistentResults;
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