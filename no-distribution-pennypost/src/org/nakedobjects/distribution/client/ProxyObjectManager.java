package org.nakedobjects.distribution.client;

import org.nakedobjects.distribution.DistributionInterface;
import org.nakedobjects.distribution.InstanceSet;
import org.nakedobjects.distribution.ObjectData;
import org.nakedobjects.distribution.ObjectReference;
import org.nakedobjects.distribution.RemoteException;
import org.nakedobjects.distribution.RemoteObjectFactory;
import org.nakedobjects.distribution.SessionId;
import org.nakedobjects.object.InstancesCriteria;
import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.LoadedObjects;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectContext;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectStore;
import org.nakedobjects.object.ObjectNotFoundException;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.TypedNakedCollection;
import org.nakedobjects.object.UnsupportedFindException;
import org.nakedobjects.object.UpdateNotifier;
import org.nakedobjects.object.defaults.AbstractNakedObjectManager;
import org.nakedobjects.object.io.Memento;
import org.nakedobjects.object.security.Session;
import org.nakedobjects.utility.NotImplementedException;

import java.util.Hashtable;

import org.apache.log4j.Logger;


public final class ProxyObjectManager extends AbstractNakedObjectManager {
    final static Logger LOG = Logger.getLogger(ProxyObjectManager.class);
    private DistributionInterface connection;
    private LoadedObjects loadedObjects;
    private UpdateNotifier notifier;
    private SessionId sessionId;
    private RemoteObjectFactory factory;
    private NakedObjectContext context;
    
    public void setConnection(DistributionInterface connection) {
        this.connection = connection;
    }
   
    public void setFactory(RemoteObjectFactory factory) {
        this.factory = factory;
    }
  
    public void setSession(Session session) {
        sessionId = factory.createSessionId(session);
    }
   
    public void setContext(NakedObjectContext context) {
        this.context = context;
    }
    
    public ProxyObjectManager(UpdateNotifier notifier, LoadedObjects loadedObjects) {
        this.notifier = notifier;
       this.loadedObjects = loadedObjects;
    }

    public void abortTransaction() {
        LOG.debug("transactions (abort) IGNORED in proxy");
    }

    public final Oid createOid(NakedObject object) {
        throw new NakedObjectRuntimeException("Invalid call to proxy object manager");
    }

    public synchronized void destroyObject(NakedObject object) {
        LOG.debug("destroyObject " + object);
        connection.destroyObject(sessionId, objectReferenceTo(object));
        loadedObjects.unloaded(object);
    }

    private ObjectReference objectReferenceTo(NakedObject object) {
        return factory.createObjectReference(object);
    }

    private ObjectReference objectReferenceTo(Oid oid, String className) {
        return factory.createObjectReference(oid, className);
    }

    public void endTransaction() {
        LOG.debug("transactions (end) IGNORED in proxy");
    }

    public TypedNakedCollection allInstances(NakedObjectSpecification cls, boolean includeSubclasses) {
        LOG.debug("getInstances of " + cls);
        InstanceSet instanceSet = connection.allInstances(sessionId, cls.getFullName(), false);
        return instanceSet.recreateInstances(loadedObjects);
    }

    public TypedNakedCollection findInstances(NakedObjectSpecification cls, String criteria, boolean includeSubclasses) throws UnsupportedFindException {
        LOG.debug("getInstances of " + cls + " with " + criteria);
        InstanceSet instanceSet;
        try {
            instanceSet = connection.findInstances(sessionId, cls.getFullName(), criteria);
	        return instanceSet.recreateInstances(loadedObjects);
        } catch (RemoteException e) {
           	throw new UnsupportedFindException(e.getRemoteMessage());
        }
    }

    public TypedNakedCollection findInstances(NakedObject pattern, boolean includeSubclasses) throws UnsupportedFindException {
        LOG.debug("getInstances like " + pattern);
        InstanceSet instanceSet;
        try {
            instanceSet = connection.findInstances(sessionId, factory.createObjectDataGraph(pattern));
	        return instanceSet.recreateInstances(loadedObjects);
        } catch (RemoteException e) {
         	throw new UnsupportedFindException(e.getRemoteMessage());
        }
    }

    public synchronized NakedObject getObject(Oid oid, NakedObjectSpecification hint) throws ObjectNotFoundException {
        if (loadedObjects.isLoaded(oid)) {
            LOG.debug("getObject (from already loaded objects) " + oid);
            return loadedObjects.getLoadedObject(oid);
        } else {
            LOG.debug("getObject (remotely from server)" + oid);
            try {
	            ObjectData graph;
                graph = connection.getObjectRequest(sessionId, objectReferenceTo(oid, hint.getFullName()));
	            NakedObject object = graph.recreateObject(loadedObjects, getContext());
	            return object;
            } catch (RemoteException e) {
             	throw new ObjectNotFoundException(e.getRemoteMessage());
            }
        }
    }

    /**
     * @deprecated
     */
    public NakedObjectStore getObjectStore() {
        throw new NotImplementedException();
    }

    public boolean hasInstances(NakedObjectSpecification cls) {
        LOG.debug("hasInstances of " + cls);
        return connection.hasInstances(sessionId, cls.getFullName());
    }

    public void init() {}

    public synchronized void makePersistent(NakedObject object) {
        LOG.debug("makePersistent " + object);
        Oid oid = connection.makePersistentRequest(sessionId, factory.createObjectDataGraph(object));
        object.setOid(oid);
        loadedObjects.loaded(object);
    }

    public int numberOfInstances(NakedObjectSpecification cls) {
        LOG.debug("numberOfInstance of " + cls);
        return connection.numberOfInstances(sessionId, cls.getFullName());
    }

    public void objectChanged(NakedObject object) {
        LOG.debug("objectChanged " + object + " - ignored by proxy manager ");
    }

    public synchronized void resolve(NakedObject object) {
        LOG.debug("resolve " + object);
        if (object.isResolved()) {
            return;
        }

        if (object instanceof InternalCollection) {
            //	new DataForInternalCollectionRequest((InternalCollection)
            // object).update(this);
        } else {
            object.setResolved();
            ObjectData objectData = connection.resolve(sessionId, objectReferenceTo(object));
            objectData.update(object, loadedObjects, context);
        }

    }

    public long serialNumber(String name) {
        LOG.debug("serialNumber " + name);
        return connection.serialNumber(sessionId, name);
    }

    public void shutdown() {
        super.shutdown();
    }

    public void startTransaction() {
        LOG.debug("transactions (start) IGNORED in proxy");
    }

    protected synchronized void updateFromServer(Memento memento) {
        Object oid = memento.getOid();
        LOG.debug("Update for " + oid + " ~ " + memento);
        if (loadedObjects.isLoaded(oid)) {
            NakedObject object = loadedObjects.getLoadedObject(oid);
            memento.updateObject(object, loadedObjects, context);
            notifier.broadcastObjectChanged(object, this);

        } else {
            LOG.debug("Notify for (" + oid + ") ignored; OID not recognised");
        }
    }

    protected NakedObjectContext getContext() {
        return context;
    }

    private final Hashtable nakedClasses = new Hashtable();
    
    public NakedClass getNakedClass(NakedObjectSpecification nakedClass) {
        if(nakedClasses.contains(nakedClass)) {
            return (NakedClass) nakedClasses.get(nakedClass);
        }
        
        NakedClass spec;
        spec = connection.getNakedClass(nakedClass.getFullName());
        nakedClasses.put(nakedClass, spec);
        return spec;
    }
	    
	protected NakedObject[] getInstances(NakedObject pattern, boolean includeSubclasses) throws UnsupportedFindException {
	    // TODO this is not required in PROXY; move the super class implementations down to LocalObjectManeger
	    throw new NotImplementedException();
	}
	
	protected NakedObject[] getInstances(NakedObjectSpecification cls, boolean includeSubclasses) {
	    // TODO this is not required in PROXY; move the super class implementations down to LocalObjectManeger
	    throw new NotImplementedException();
	}
	
	protected NakedObject[] getInstances(NakedObjectSpecification cls, String term, boolean includeSubclasses) throws UnsupportedFindException {
	    // TODO this is not required in PROXY; move the super class implementations down to LocalObjectManeger
	    throw new NotImplementedException();
	}
	
	protected NakedObject[] getInstances(InstancesCriteria criteria, boolean includeSubclasses) {
	    // TODO this is not required in PROXY; move the super class implementations down to LocalObjectManeger
	    throw new NotImplementedException();
   }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2003 Naked Objects Group
 * Ltd This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details. You should have received a copy of the GNU
 * General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA The authors can be contacted via www.nakedobjects.org (the registered
 * address of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking
 * GU21 1NR, UK).
 */
