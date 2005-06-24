package org.nakedobjects.distribution.command;

import org.nakedobjects.distribution.ClientDistribution;
import org.nakedobjects.distribution.Data;
import org.nakedobjects.distribution.DataHelper;
import org.nakedobjects.distribution.ObjectData;
import org.nakedobjects.object.DirtyObjectSet;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.persistence.InstancesCriteria;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.persistence.TitleCriteria;
import org.nakedobjects.object.security.Session;
import org.nakedobjects.utility.NotImplementedException;

import org.apache.log4j.Logger;


public abstract class CommandClient implements ClientDistribution {
    private static final Logger LOG = Logger.getLogger(CommandClient.class);    
    private DirtyObjectSet updateNotifier;

    public ObjectData[] allInstances(Session session, String fullName, boolean includeSubclasses) {
        AllInstances request = new AllInstances(session, fullName, includeSubclasses);
        execute(request);
        return request.getInstances();
    }

    public void clearAssociation(Session session, String fieldIdentifier, Oid objectOid, String objectType, Oid associateOid,
            String associateType) {
        Request request = new ClearAssociation(session, fieldIdentifier, objectOid, objectType, associateOid, associateType);
        execute(request);
    }

    public void destroyObject(Session session, Oid oid, String type) {
        throw new NotImplementedException();
    }

    public Data executeAction(Session session, String actionType, String actionIdentifier, String[] parameterTypes,
            Oid objectOid, String objectType, Data[] parameters) {
        ExecuteAction request = new ExecuteAction(session, actionType, actionIdentifier, parameterTypes, objectOid, objectType,
                parameters);
        execute(request);
        return request.getActionResult();
    }

    public ObjectData[] findInstances(Session session, InstancesCriteria criteria) {
        if(criteria instanceof TitleCriteria) {
	        FindInstancesByTitle request = new FindInstancesByTitle(session, (TitleCriteria) criteria);
	        execute(request);
	        return request.getInstances();
        } else {
            throw new NakedObjectRuntimeException();
        }
    }

    public Hint getActionHint(Session session, String actionType, String actionIdentifier, String[] parameterTypes,
            Oid objectOid, String objectType, Data[] parameters) {
        throw new NotImplementedException();
    }

    public ObjectData resolveImmediately(Session session, Oid oid, String fullName) {
        Resolve request = new Resolve(session, oid, fullName);
        execute(request);
        return request.getUpdateData();
    }

    public boolean hasInstances(Session session, String fullName) {
        HasInstances request = new HasInstances(session, fullName);
        execute(request);
        return request.getFlag();
    }

    public Oid[] makePersistent(Session session, ObjectData data) {
        MakePersistent request = new MakePersistent(session, data);
        execute(request);
        return request.getOids();
    }

    public int numberOfInstances(Session session, String fullName) {
        throw new NotImplementedException();
    }

    private void execute(Request request) {
        Response response = executeRemotely(request);

        if (request.getId() != response.getId()) {
            throw new NakedObjectRuntimeException("Response out of sequence with respect to the request: " + request.getId()
                    + " & " + response.getId() + " respectively");
        }
        request.setResponse(response.getObject());
        
        ObjectData[] updates = response.getUpdates();
        for (int i = 0; i < updates.length; i++) {
            LOG.debug("update " + updates[i]);
            DataHelper.update(updates[i], updateNotifier);
        }
    }

    protected abstract Response executeRemotely(Request request);

    public void setAssociation(Session session, String fieldIdentifier, Oid objectOid, String objectType, Oid associateOid,
            String associateType) {
        Request request = new SetAssociation(session, fieldIdentifier, objectOid, objectType, associateOid, associateType);
        execute(request);
    }

    public void setValue(Session session, String fieldIdentifier, Oid oid, String objectType, Object associate) {
        Request request = new SetValue(session, fieldIdentifier, oid, objectType, associate);
        execute(request);
    }
    
    public void abortTransaction(Session session) {
        Request request = new AbortTransaction(session);
        execute(request);    
    }

    public void endTransaction(Session session) {
        Request request = new EndTransaction(session);
        execute(request);    
    }

    public void startTransaction(Session session) {
        Request request = new StartTransaction(session);
        execute(request);        
    }

    public void setUpdateNotifier(DirtyObjectSet updateNotifier) {
        this.updateNotifier = updateNotifier;
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