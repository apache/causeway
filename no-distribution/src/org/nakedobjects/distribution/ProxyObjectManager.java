package org.nakedobjects.distribution;

import org.nakedobjects.distribution.client.DestroyObjectRequest;
import org.nakedobjects.distribution.client.GetInstancesRequest;
import org.nakedobjects.distribution.client.GetObjectRequest;
import org.nakedobjects.distribution.client.HasInstancesRequest;
import org.nakedobjects.distribution.client.MakePersistentRequest;
import org.nakedobjects.distribution.client.NumberOfInstancesRequest;
import org.nakedobjects.distribution.client.ResolveRequest;
import org.nakedobjects.distribution.client.SerialNumberRequest;
import org.nakedobjects.object.LoadedObjects;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.NakedObjectMemento;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectStore;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.UpdateNotifier;
import org.nakedobjects.object.collection.InternalCollection;
import org.nakedobjects.utility.Log;
import org.nakedobjects.utility.NotImplementedException;

import java.io.Serializable;
import java.util.Vector;

import org.apache.log4j.Logger;


public final class ProxyObjectManager extends NakedObjectManager {
    final static Logger LOG = Logger.getLogger(ProxyObjectManager.class);
    private DistributionInterface connection;
    private Log log;
    private UpdateNotifier notifier;
    private LoadedObjects loadedObjects;

    public ProxyObjectManager(UpdateNotifier notifier, DistributionInterface connection) {
        this.notifier = notifier;

         this.connection = connection;
        connection.init();
        
        loadedObjects = new LoadedObjects();
        
        new ProxyClassManager();
    }

    public void abortTransaction() {
        throw new NakedObjectRuntimeException();
    }

    public final Object createOid() {
    	throw new NakedObjectRuntimeException();
    }
   
    public synchronized void destroyObject(NakedObject object) {
    	LOG.debug("destroyObject " + object);
        new DestroyObjectRequest(object).execute();
        loadedObjects.unloaded(object);
    }
    
    public void endTransaction() {
        throw new NakedObjectRuntimeException();
    }

    public Vector getInstances(NakedClass cls) {
        return new GetInstancesRequest(cls).getElements(loadedObjects);
    }
    
    public Vector getInstances(NakedClass cls, String criteria) {
        throw new NotImplementedException();
    }
    
   public Vector getInstances(NakedObject pattern) {
        if (pattern == null) {
            throw new NullPointerException();
        }
        
        return new GetInstancesRequest(pattern).getElements(loadedObjects);
    }
    
    public synchronized NakedObject getObject(Object oid, NakedClass hint) {
        if (loadedObjects.isLoaded(oid)) {
        	LOG.debug("getObject (from already loaded objects) " + oid);
            return loadedObjects.getLoadedObject(oid);
        } else {
        	LOG.debug("getObject (remotely from server)" + oid);
            NakedObject object = new GetObjectRequest(oid, hint).getObject();
			loadedObjects.loaded(object);
			return object;
        }
    }

    /**
     * @deprecated
     */
    public NakedObjectStore getObjectStore() {
		throw new NotImplementedException();
	}


    public boolean hasInstances(NakedClass cls) {
    	LOG.debug("hasInstances of " + cls);
        return new HasInstancesRequest(cls).hasInstances();
    }

    public void init() {
        Request.init(this, loadedObjects);
    }
    
    protected void log() {
        log.log();
    }

    protected void log(String logEntry) {
        log.log(logEntry);
    }

    public synchronized void makePersistent(NakedObject object) {
    	LOG.debug("makePersistent " + object);
        new MakePersistentRequest(object).makePersistent();
        loadedObjects.loaded(object);
    }

    public String name() {
        return "Proxy Object Store";
    }

    public int numberOfInstances(NakedClass cls) {
    	LOG.debug("numberOfInstance of " + cls);
        return new NumberOfInstancesRequest(cls).size();
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
            //	new DataForInternalCollectionRequest((InternalCollection) object).update(this);
            ;
        } else {
	        object.setResolved();
            new ResolveRequest(object).update(object, loadedObjects);
        }

    }

    protected Serializable send(Request request) {
        LOG.debug("send request " + request);
        Serializable response = connection.execute(request);
        LOG.debug("response to request " + request + " ~ " + (response == null ? "EMPTY RESPONSE" : response));
		return response;
    }

    public long serialNumber(String id) {
    	LOG.debug("serialNumber " + id);
        try {
			return new SerialNumberRequest(id).getSerialNumber();
		} catch (ObjectStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new NakedObjectRuntimeException(e);
		}
    }

     public void shutdown() {
        connection.shutdown();
        super.shutdown();
    }

    public void startTransaction() {
        throw new NakedObjectRuntimeException();
    }
	
    public LoadedObjects getLoadedObjects() {
        return loadedObjects;
    }
    
	protected synchronized void updateFromServer(NakedObjectMemento memento) {
		Object oid = memento.getOid();
		LOG.debug("Update for " + oid + " ~ " + memento);
		if(loadedObjects.isLoaded(oid)) {
			NakedObject object = loadedObjects.getLoadedObject(oid);
			memento.updateNakedObject(object, loadedObjects);
	        notifier.broadcastObjectChanged(object, this);

		} else {
			LOG.debug("Notify for (" + oid + ") ignored; OID not recognised");
		}
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
