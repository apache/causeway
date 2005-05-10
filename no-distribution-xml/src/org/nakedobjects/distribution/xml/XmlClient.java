package org.nakedobjects.distribution.xml;

import org.nakedobjects.distribution.ClientDistribution;
import org.nakedobjects.distribution.Data;
import org.nakedobjects.distribution.DataHelper;
import org.nakedobjects.distribution.ObjectData;
import org.nakedobjects.distribution.xml.request.AbortTransaction;
import org.nakedobjects.distribution.xml.request.AllInstances;
import org.nakedobjects.distribution.xml.request.ClearAssociation;
import org.nakedobjects.distribution.xml.request.EndTransaction;
import org.nakedobjects.distribution.xml.request.ExecuteAction;
import org.nakedobjects.distribution.xml.request.FindInstancesByTitle;
import org.nakedobjects.distribution.xml.request.HasInstances;
import org.nakedobjects.distribution.xml.request.MakePersistent;
import org.nakedobjects.distribution.xml.request.SetAssociation;
import org.nakedobjects.distribution.xml.request.SetValue;
import org.nakedobjects.distribution.xml.request.StartTransaction;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.persistence.InstancesCriteria;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.persistence.TitleCriteria;
import org.nakedobjects.object.security.Session;
import org.nakedobjects.utility.NotImplementedException;

import com.thoughtworks.xstream.XStream;


public class XmlClient implements ClientDistribution {
    private ClientConnection connection;

    public XmlClient() {
        connection = new ClientConnection();
        connection.init();
    }

    public ObjectData[] allInstances(Session session, String fullName, boolean includeSubclasses) {
        AllInstances request = new AllInstances(session, fullName, includeSubclasses);
        remoteExecute(request);
        return request.getInstances();
    }

    public void clearAssociation(Session session, String fieldIdentifier, Oid objectOid, String objectType, Oid associateOid,
            String associateType) {
        Request request = new ClearAssociation(session, fieldIdentifier, objectOid, objectType, associateOid, associateType);
        remoteExecute(request);
    }

    public void destroyObject(Session session, Oid oid, String type) {
        throw new NotImplementedException();
    }

    public ObjectData executeAction(Session session, String actionType, String actionIdentifier, String[] parameterTypes,
            Oid objectOid, String objectType, Data[] parameters) {
        ExecuteAction request = new ExecuteAction(session, actionType, actionIdentifier, parameterTypes, objectOid, objectType,
                parameters);
        remoteExecute(request);
        return request.getActionResult();
    }

    public ObjectData[] findInstances(Session session, InstancesCriteria criteria) {
        if(criteria instanceof TitleCriteria) {
	        FindInstancesByTitle request = new FindInstancesByTitle(session, (TitleCriteria) criteria);
	        remoteExecute(request);
	        return request.getInstances();
        } else {
            throw new NakedObjectRuntimeException();
        }
    }

    public Hint getActionHint(Session session, String actionType, String actionIdentifier, String[] parameterTypes,
            Oid objectOid, String objectType, Data[] parameters) {
        throw new NotImplementedException();
    }

    public ObjectData getObject(Session session, Oid oid, String fullName) {
        throw new NotImplementedException();
    }

    public boolean hasInstances(Session session, String fullName) {
        HasInstances request = new HasInstances(session, fullName);
        remoteExecute(request);
        return request.getFlag();
    }

    public Oid[] makePersistent(Session session, ObjectData data) {
        MakePersistent request = new MakePersistent(session, data);
        remoteExecute(request);
        return request.getOids();
    }

    public int numberOfInstances(Session session, String fullName) {
        throw new NotImplementedException();
    }

    private void remoteExecute(Request request) {
        XStream xstream = new XStream();
        String requestData = xstream.toXML(request);
        String responseData = connection.request(requestData);
        Response response = (Response) xstream.fromXML(responseData);

        if (request.getId() != response.getId()) {
            throw new NakedObjectRuntimeException("Response out of sequence with respect to the request: " + request.getId()
                    + " & " + response.getId() + " respectively");
        }
        request.setResponse(response.getObject());
        
        ObjectData[] updates = response.getUpdates();
        for (int i = 0; i < updates.length; i++) {
            DataHelper.update(updates[i]);
        }
    }

    public void setAssociation(Session session, String fieldIdentifier, Oid objectOid, String objectType, Oid associateOid,
            String associateType) {
        Request request = new SetAssociation(session, fieldIdentifier, objectOid, objectType, associateOid, associateType);
        remoteExecute(request);
    }

    public void setValue(Session session, String fieldIdentifier, Oid oid, String objectType, Object associate) {
        Request request = new SetValue(session, fieldIdentifier, oid, objectType, associate);
        remoteExecute(request);
    }
    
    public void abortTransaction(Session session) {
        Request request = new AbortTransaction(session);
        remoteExecute(request);    
    }

    public void endTransaction(Session session) {
        Request request = new EndTransaction(session);
        remoteExecute(request);    
    }

    public void startTransaction(Session session) {
        Request request = new StartTransaction(session);
        remoteExecute(request);        
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