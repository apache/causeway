package org.nakedobjects.distribution;

import org.nakedobjects.object.DirtyObjectSet;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.TypedNakedCollection;
import org.nakedobjects.object.defaults.AbstractNakedObjectManager;
import org.nakedobjects.object.defaults.collection.InstanceCollectionVector;
import org.nakedobjects.object.persistence.InstancesCriteria;
import org.nakedobjects.object.persistence.ObjectNotFoundException;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.persistence.UnsupportedFindException;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.security.Session;
import org.nakedobjects.utility.DebugString;
import org.nakedobjects.utility.NotImplementedException;

import java.util.Hashtable;

import org.apache.log4j.Logger;


// TODO this class replaces most of AbstractNakedObjectManager, therefore just
// implement NakedObjectManager
public final class ProxyObjectManager extends AbstractNakedObjectManager {
    final static Logger LOG = Logger.getLogger(ProxyObjectManager.class);
    private ClientDistribution connection;
    private final Hashtable nakedClasses = new Hashtable();
    private DataFactory objectDataFactory;
    private Session session;
    private DirtyObjectSet updateNotifier;

    public void abortTransaction() {
        connection.abortTransaction(session);
    }

    public void addObjectChangedListener(DirtyObjectSet listener) {}

    public TypedNakedCollection allInstances(NakedObjectSpecification specification, boolean includeSubclasses) {
        LOG.debug("getInstances of " + specification);
        ObjectData data[] = connection.allInstances(session, specification.getFullName(), false);
        return convertToNakedObjects(specification, data);
    }

    private TypedNakedCollection convertToNakedObjects(NakedObjectSpecification specification, ObjectData[] data) {
        NakedObject[] instances = new NakedObject[data.length];
        for (int i = 0; i < data.length; i++) {
            instances[i] = DataHelper.recreateObject(data[i]);
        }
        return new InstanceCollectionVector(specification, instances);
    }

    public Oid createOid(Naked object) {
        throw new NotExpectedException();
    }

    public synchronized void destroyObject(NakedObject object) {
        LOG.debug("destroyObject " + object);
        connection.destroyObject(session, object.getOid(), object.getSpecification().getFullName());
        unloaded(object);
    }

    public void endTransaction() {
        connection.endTransaction(session);
    }

    public TypedNakedCollection findInstances(InstancesCriteria criteria) throws UnsupportedFindException {
        LOG.debug("getInstances of " + criteria.getSpecification() + " with " + criteria);
        ObjectData[] instances = connection.findInstances(session, criteria);
        return convertToNakedObjects(criteria.getSpecification(), instances);
    }

    public String getDebugData() {
        DebugString debug = new DebugString();

        debug.appendln(0, "Connection", connection);
        debug.appendln();

        debug.appendTitle(super.getDebugTitle());
        debug.append(super.getDebugData());

        return debug.toString();
    }

    public String getDebugTitle() {
        return "Proxy Object Manager";
    }

    protected NakedObject[] getInstances(InstancesCriteria criteria) {
        // TODO this is not required in PROXY; move the super class
        // implementations down to LocalObjectManeger
        throw new NotImplementedException();
    }

    protected NakedObject[] getInstances(NakedObjectSpecification specification, boolean includeSubclasses) {
        // TODO this is not required in PROXY; move the super class
        // implementations down to LocalObjectManeger
        throw new NotImplementedException();
    }

    public NakedClass getNakedClass(NakedObjectSpecification nakedClass) {
        if (nakedClasses.contains(nakedClass)) {
            return (NakedClass) nakedClasses.get(nakedClass);
        }

        NakedClass cls;
        cls = new NakedClass(nakedClass.getFullName());
        nakedClasses.put(nakedClass, cls);
        return cls;
    }

    public synchronized NakedObject getObject(Oid oid, NakedObjectSpecification hint) throws ObjectNotFoundException {
        throw new NotImplementedException();
        /*
         * if (loadedObjects().isLoaded(oid)) { LOG.debug("getObject (from
         * already loaded objects) " + oid); return
         * loadedObjects().getLoadedObject(oid); } else { LOG.debug("getObject
         * (remotely from server)" + oid); ObjectData data =
         * connection.getObject(session, oid, hint.getFullName()); return
         * DataHelper.recreateObject(data); }
         *  
         */

    }

    public boolean hasInstances(NakedObjectSpecification specification) {
        LOG.debug("hasInstances of " + specification);
        return connection.hasInstances(session, specification.getFullName());
    }

    public void init() {}

    public synchronized void makePersistent(NakedObject object) {
        LOG.debug("makePersistent " + object);
        Oid[] oid = connection.makePersistent(session, objectDataFactory.createObjectData(object, 0));
        makePersistent(object, oid[0]);
    }

    public int numberOfInstances(NakedObjectSpecification specification) {
        LOG.debug("numberOfInstance of " + specification);
        return connection.numberOfInstances(session, specification.getFullName());
    }

    public void objectChanged(NakedObject object) {
        LOG.debug("objectChanged " + object + " - ignored by proxy manager ");
    }

    public void reset() {}

    public synchronized void resolveImmediately(NakedObject object) {
        LOG.debug("resolve " + object);
        if (object.isResolved() || !object.isPersistent()) {
            return;
        }

        Oid oid = object.getOid();
        NakedObjectSpecification hint = object.getSpecification();

        LOG.debug("resolve object (remotely from server)" + oid);
        ObjectData data = connection.resolveImmediately(session, oid, hint.getFullName());
        DataHelper.update(data, updateNotifier);
    }

    public void resolveLazily(NakedObject object, NakedObjectField field) {}

    public void saveChanges() {
        LOG.debug("saveChanges - ignored by proxy manager");
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
    public void set_ObjectDataFactory(DataFactory factory) {
        this.objectDataFactory = factory;
    }

    /**
     * .NET property
     * 
     * @property
     */
    public void set_UpdateNotifier(DirtyObjectSet updateNotifier) {
        this.updateNotifier = updateNotifier;
    }

    public void setConnection(ClientDistribution connection) {
        this.connection = connection;
    }

    public void setObjectDataFactory(DataFactory factory) {
        this.objectDataFactory = factory;
    }

    public void setUpdateNotifier(DirtyObjectSet updateNotifier) {
        this.updateNotifier = updateNotifier;
    }

    public void shutdown() {
        super.shutdown();
    }

    public void startTransaction() {
        connection.startTransaction(session);
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