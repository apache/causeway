package org.nakedobjects.distribution;

import org.nakedobjects.object.InstancesCriteria;
import org.nakedobjects.object.LoadedObjects;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectContext;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.ObjectNotFoundException;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.TypedNakedCollection;
import org.nakedobjects.object.UnsupportedFindException;
import org.nakedobjects.object.defaults.AbstractNakedObjectManager;
import org.nakedobjects.object.defaults.collection.InstanceCollectionVector;
import org.nakedobjects.object.security.Session;
import org.nakedobjects.utility.NotImplementedException;

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;


public final class ProxyObjectManager extends AbstractNakedObjectManager {
    final static Logger LOG = Logger.getLogger(ProxyObjectManager.class);
    private ClientDistribution connection;
    private NakedObjectContext context;
    private LoadedObjects loadedObjects;
    private final Hashtable nakedClasses = new Hashtable();
    private ObjectDataFactory objectDataFactory;
    private Session session;

    public void abortTransaction() {
        LOG.debug("transactions (abort) IGNORED in proxy");
    }

    public TypedNakedCollection allInstances(NakedObjectSpecification specification, boolean includeSubclasses) {
        LOG.debug("getInstances of " + specification);
        ObjectData data[] = connection.allInstances(session, specification.getFullName(), false);
        return convertToNakedObjects(specification, data);
    }

    private TypedNakedCollection convertToNakedObjects(NakedObjectSpecification specification, ObjectData[] data) {
        NakedObject[] instances = new NakedObject[data.length];
        for (int i = 0; i < data.length; i++) {
            instances[i] = data[i].recreate(loadedObjects);
        }
        return new InstanceCollectionVector(specification, instances);
    }

    /*
     * protected synchronized void updateFromServer(Memento memento) { Object
     * oid = memento.getOid(); LOG.debug("Update for " + oid + " ~ " + memento);
     * if (loadedObjects.isLoaded(oid)) { NakedObject object =
     * loadedObjects.getLoadedObject(oid); memento.updateObject(object,
     * loadedObjects, context); notifier.broadcastObjectChanged(object, this); }
     * else { LOG.debug("Notify for (" + oid + ") ignored; OID not recognised"); } }
     */

    public Oid createOid(Naked object) {
        throw new NotExpectedException();
    }

    public synchronized void destroyObject(NakedObject object) {
        LOG.debug("destroyObject " + object);
        connection.destroyObject(session, object.getOid(), object.getSpecification().getFullName());
        loadedObjects.unloaded(object);
    }

    public void endTransaction() {
        LOG.debug("transactions (end) IGNORED in proxy");
    }

    public TypedNakedCollection findInstances(NakedObject pattern, boolean includeSubclasses) throws UnsupportedFindException {
        throw new NotImplementedException("distribution version of this method has been removed");
    }

    public TypedNakedCollection findInstances(NakedObjectSpecification specification, String criteria, boolean includeSubclasses)
            throws UnsupportedFindException {
        LOG.debug("getInstances of " + specification + " with " + criteria);
        ObjectData[] instances = connection.findInstances(session, specification.getFullName(), criteria, includeSubclasses);
        return convertToNakedObjects(specification, instances);
    }

    protected NakedObjectContext getContext() {
        return context;
    }

    protected NakedObject[] getInstances(InstancesCriteria criteria, boolean includeSubclasses) {
        // TODO this is not required in PROXY; move the super class
        // implementations down to LocalObjectManeger
        throw new NotImplementedException();
    }

    protected NakedObject[] getInstances(NakedObject pattern, boolean includeSubclasses) throws UnsupportedFindException {
        // TODO this is not required in PROXY; move the super class
        // implementations down to LocalObjectManeger
        throw new NotImplementedException();
    }

    protected NakedObject[] getInstances(NakedObjectSpecification specification, boolean includeSubclasses) {
        // TODO this is not required in PROXY; move the super class
        // implementations down to LocalObjectManeger
        throw new NotImplementedException();
    }

    protected NakedObject[] getInstances(NakedObjectSpecification specification, String term, boolean includeSubclasses)
            throws UnsupportedFindException {
        // TODO this is not required in PROXY; move the super class
        // implementations down to LocalObjectManeger
        throw new NotImplementedException();
    }

    public NakedClass getNakedClass(NakedObjectSpecification nakedClass) {
        if (nakedClasses.contains(nakedClass)) {
            return (NakedClass) nakedClasses.get(nakedClass);
        }

        NakedClass cls;
        // cls = connection.getNakedClass(nakedClass.getFullName());
        cls = new NakedClass(nakedClass.getFullName());
        nakedClasses.put(nakedClass, cls);
        return cls;
    }

    public synchronized NakedObject getObject(Oid oid, NakedObjectSpecification hint) throws ObjectNotFoundException {
        if (loadedObjects.isLoaded(oid)) {
            LOG.debug("getObject (from already loaded objects) " + oid);
            return loadedObjects.getLoadedObject(oid);
        } else {
            LOG.debug("getObject (remotely from server)" + oid);
            ObjectData data = connection.getObject(session, oid, hint.getFullName());
            return data.recreate(loadedObjects);
        }
    }

    public boolean hasInstances(NakedObjectSpecification specification) {
        LOG.debug("hasInstances of " + specification);
        return connection.hasInstances(session, specification.getFullName());
    }

    public void init() {}

    public synchronized void makePersistent(NakedObject object) {
        LOG.debug("makePersistent " + object);
        Oid[] oid = connection.makePersistent(session, objectDataFactory.createObjectData(object, 0));
        object.setOid(oid[0]);
        loadedObjects.loaded(object);
    }

    public int numberOfInstances(NakedObjectSpecification specification) {
        LOG.debug("numberOfInstance of " + specification);
        return connection.numberOfInstances(session, specification.getFullName());
    }

    public void objectChanged(NakedObject object) {
        LOG.debug("objectChanged " + object + " - ignored by proxy manager ");
    }

    public synchronized void resolve(NakedObject object) {
        LOG.debug("resolve " + object);
        if (object.isResolved() || !object.isPersistent()) {
            return;
        }

        /*
         * if (object instanceof InternalCollection) { // new
         * DataForInternalCollectionRequest((InternalCollection) //
         * object).update(this); } else { object.setResolved();
         * connection.resolve(session, object.getOid());
         * objectData.update(object, loadedObjects, context); }
         */
        //     throw new NotImplementedException();
        LOG.warn("Not yet resolving");
    }

    public void saveChanges() {
        LOG.debug("Saving changes");
        Enumeration e = loadedObjects.dirtyObjects();
        while (e.hasMoreElements()) {
            NakedObject object = (NakedObject) e.nextElement();
            LOG.debug("  " + object);
            objectChanged(object);
            object.clearPersistDirty();
        }
    }

    public long serialNumber(String name) {
        LOG.debug("serialNumber " + name);
        return connection.serialNumber(session, name);
    }

    /**
     * .NET property
     * 
     * @property
     */
    public void set_Connection(ClientDistribution connection) {
        this.connection = connection;
    }

    /**
     * .NET property
     * 
     * @property
     */
    public void set_LoadedObjects(LoadedObjects loadedObjects) {
        this.loadedObjects = loadedObjects;
    }

    /**
     * .NET property
     * 
     * @property
     */
    public void set_ObjectDataFactory(ObjectDataFactory factory) {
        this.objectDataFactory = factory;
    }

    public void setConnection(ClientDistribution connection) {
        this.connection = connection;
    }

    public void setContext(NakedObjectContext context) {
        this.context = context;
    }

    public void setLoadedObjects(LoadedObjects loadedObjects) {
        this.loadedObjects = loadedObjects;
    }

    public void setObjectDataFactory(ObjectDataFactory factory) {
        this.objectDataFactory = factory;
    }

    public void shutdown() {
        super.shutdown();
    }

    public void startTransaction() {
        LOG.debug("transactions (start) IGNORED in proxy");
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
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
