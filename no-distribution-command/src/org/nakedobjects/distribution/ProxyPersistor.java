package org.nakedobjects.distribution;

import org.nakedobjects.object.DirtyObjectSet;
import org.nakedobjects.object.InstancesCriteria;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.NakedReference;
import org.nakedobjects.object.ObjectNotFoundException;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.OneToOneAssociation;
import org.nakedobjects.object.Persistable;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.object.Session;
import org.nakedobjects.object.TypedNakedCollection;
import org.nakedobjects.object.UnsupportedFindException;
import org.nakedobjects.object.defaults.AbstracObjectPersistor;
import org.nakedobjects.object.defaults.InstanceCollectionVector;
import org.nakedobjects.object.defaults.NakedClassImpl;
import org.nakedobjects.utility.DebugString;
import org.nakedobjects.utility.NotImplementedException;

import java.util.Hashtable;

import org.apache.log4j.Logger;


// TODO this class replaces most of AbstractNakedObjectManager, therefore just
// implement NakedObjectManager
public final class ProxyPersistor extends AbstracObjectPersistor {
    final static Logger LOG = Logger.getLogger(ProxyPersistor.class);
    private Distribution connection;
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
            instances[i] = (NakedObject) DataHelper.restore(data[i]);
        }
        return new InstanceCollectionVector(specification, instances);
    }

    public synchronized void destroyObject(NakedObject object) {
        LOG.debug("destroyObject " + object);
        connection.destroyObject(session, objectDataFactory.createReference(object));
        // TODO need to do garbage collection instead
        //NakedObjects.getObjectLoader().unloaded(object);
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
        cls = new NakedClassImpl(nakedClass.getFullName());
        nakedClasses.put(nakedClass, cls);
        return cls;
    }

    public synchronized NakedObject getObject(Oid oid, NakedObjectSpecification hint) throws ObjectNotFoundException {
        throw new NotImplementedException();
    }

    public boolean hasInstances(NakedObjectSpecification specification, boolean includeSubclasses) {
        LOG.debug("hasInstances of " + specification);
        return connection.hasInstances(session, specification.getFullName());
    }

    public void init() {}

    public synchronized void makePersistent(NakedObject object) {
        LOG.debug("makePersistent " + object);
        ObjectData updates = connection.makePersistent(session, objectDataFactory.createMakePersistentGraph(object));
        madePersistent(object, updates);
    }

    private void madePersistent(NakedObject object, ObjectData updates) {
        if(updates == null) {
            return;
        }

        if(object.getOid() == null && object.persistable() != Persistable.TRANSIENT) {
            NakedObjects.getObjectLoader().madePersistent(object, updates.getOid());
            object.setOptimisticLock(updates.getVersion());
        }

        Data[] fieldData = updates.getFieldContent();
        NakedObjectField[] fields = object.getSpecification().getFields();
        for (int i = 0; i < fieldData.length; i++) {
            if(fieldData[i] == null) {
                continue;
            }
            if(fields[i].isObject()) {
                NakedObject field = object.getAssociation((OneToOneAssociation) fields[i]);
                ObjectData fieldContent = (ObjectData) updates.getFieldContent()[i];
                if(field != null) {
                    madePersistent(field, fieldContent);
                }
            } else if(fields[i].isCollection()) {
                CollectionData collectionData = (CollectionData) updates.getFieldContent()[i];
                for (int j = 0; j < collectionData.getElements().length; j++) {
                    NakedObject element = ((NakedCollection) object.getField(fields[i])).elementAt(j);
                    ObjectData elementData = collectionData.getElements()[j];
                    madePersistent(element, elementData);
                }
            }
        }
    }

    public int numberOfInstances(NakedObjectSpecification specification, boolean includeSubclasses) {
        LOG.debug("numberOfInstance of " + specification);
        return connection.numberOfInstances(session, specification.getFullName());
    }

    public void objectChanged(NakedObject object) {
        if(object.getResolveState().isTransient()) {
            updateNotifier.addDirty(object);
        } else {
            LOG.debug("objectChanged " + object + " - ignored by proxy manager as it is a persistent object");
        }
    }

    public void reset() {}

    public void reload(NakedObject object) {
        ObjectData update = connection.resolveImmediately(session, objectDataFactory.createReference(object));
        DataHelper.restore(update);
    }
    
    public synchronized void resolveImmediately(NakedObject object) {
        ResolveState resolveState = object.getResolveState();
        if (resolveState.isResolvable(ResolveState.RESOLVING)) {
            Oid oid = object.getOid();
            LOG.debug("resolve object (remotely from server)" + oid);
            ObjectData data = connection.resolveImmediately(session, objectDataFactory.createReference(object));
            DataHelper.restore(data);
        }
    }

    public void resolveField(NakedObject object, NakedObjectField field) {
        if(field.isValue()) {
            return;
        }
        NakedReference reference = (NakedReference) object.getField(field);
        if(reference.getResolveState().isResolved()) {
            return;
        }
        if (! reference.getResolveState().isPersistent()) {
            return;
        }
        
        LOG.info("resolve-eagerly on server " + object + "/" + field.getId());
        Data data = connection.resolveField(session, objectDataFactory.createReference(object), field.getId());
        DataHelper.restore(data);
    }

    public void saveChanges() {
        LOG.debug("saveChanges - ignored by proxy manager");
    }

    /**
     * .NET property
     * 
     * @property
     */
    public void set_Connection(Distribution connection) {
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
        DataHelper.setUpdateNotifer(updateNotifier);
        this.updateNotifier = updateNotifier;
    }

    public void setConnection(Distribution connection) {
        this.connection = connection;
    }

    public void setObjectDataFactory(DataFactory factory) {
        this.objectDataFactory = factory;
    }

    public void setUpdateNotifier(DirtyObjectSet updateNotifier) {
        DataHelper.setUpdateNotifer(updateNotifier);
        this.updateNotifier = updateNotifier;
    }

    public void startTransaction() {
        connection.startTransaction(session);
    }

    public void shutdown() {}
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */