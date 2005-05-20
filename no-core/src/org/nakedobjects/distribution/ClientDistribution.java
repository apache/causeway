package org.nakedobjects.distribution;

import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.persistence.InstancesCriteria;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.security.Session;


public interface ClientDistribution {

    void abortTransaction(Session session);

    ObjectData[] allInstances(Session session, String fullName, boolean includeSubclasses);

    void clearAssociation(Session session, String fieldIdentifier, Oid objectOid, String objectType, Oid associateOid,
            String associateType);

    void destroyObject(Session session, Oid oid, String type);

    void endTransaction(Session session);

    Data executeAction(Session session, String actionType, String actionIdentifier, String[] parameterTypes, Oid objectOid,
            String objectType, Data[] parameters);

    ObjectData[] findInstances(Session session, InstancesCriteria criteria);

    Hint getActionHint(Session session, String actionType, String actionIdentifier, String[] parameterTypes, Oid objectOid,
            String objectType, Data[] parameters);

    // TODO change name to reflect the fact that it is being used in resolves - see ProxyObjectMan.resolve...()
    ObjectData getObject(Session session, Oid oid, String fullName);

    boolean hasInstances(Session session, String fullName);

    Oid[] makePersistent(Session session, ObjectData object);

    int numberOfInstances(Session sessionId, String fullName);

    void setAssociation(Session session, String fieldIdentifier, Oid objectOid, String objectType, Oid associateOid,
            String associateType);

    void setValue(Session session, String fieldIdentifier, Oid objectOid, String objectType, Object associate);

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