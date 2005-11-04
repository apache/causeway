package org.nakedobjects.distribution;

import org.nakedobjects.object.InstancesCriteria;
import org.nakedobjects.object.Session;
import org.nakedobjects.object.control.Hint;


public interface Distribution {

    void abortTransaction(Session session);

    ObjectData[] allInstances(Session session, String fullName, boolean includeSubclasses);

    void clearAssociation(Session session, String fieldIdentifier, ReferenceData target, ReferenceData associate);

    void destroyObject(Session session, ReferenceData object);

    void endTransaction(Session session);

    Data executeAction(Session session, String actionType, String actionIdentifier, ObjectData target, Data[] parameters);

    ObjectData[] findInstances(Session session, InstancesCriteria criteria);

    Hint getActionHint(Session session, String actionType, String actionIdentifier, ObjectData target, Data[] parameters);

    ObjectData resolveImmediately(Session session, ReferenceData target);

    Data resolveField(Session session, ReferenceData data, String name);

    boolean hasInstances(Session session, String fullName);

    ObjectData makePersistent(Session session, ObjectData object);

    int numberOfInstances(Session sessionId, String fullName);

    void setAssociation(Session session, String fieldIdentifier, ReferenceData target, ReferenceData associate);

    void setValue(Session session, String fieldIdentifier, ReferenceData target, Object associate);

    void startTransaction(Session session);
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