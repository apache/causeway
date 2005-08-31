package org.nakedobjects.persistence.file;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectLoader;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.object.persistence.CreateObjectCommand;
import org.nakedobjects.object.persistence.DestroyObjectCommand;
import org.nakedobjects.object.persistence.InstancesCriteria;
import org.nakedobjects.object.persistence.NakedObjectStore;
import org.nakedobjects.object.persistence.ObjectNotFoundException;
import org.nakedobjects.object.persistence.ObjectManagerException;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.persistence.PersistenceCommand;
import org.nakedobjects.object.persistence.SaveObjectCommand;
import org.nakedobjects.object.persistence.UnsupportedFindException;
import org.nakedobjects.object.persistence.defaults.SerialOid;
import org.nakedobjects.object.reflect.NakedObjectAssociation;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.utility.Assert;
import org.nakedobjects.utility.DebugString;

import org.apache.log4j.Logger;


public class XmlObjectStore implements NakedObjectStore {
    private static final Logger LOG = Logger.getLogger(XmlObjectStore.class);
    private DataManager dataManager;
    private NakedObjectLoader objectLoader;

    public void abortTransaction() {
        LOG.debug("transaction aborted");
    }

    private NakedObjectSpecification specFor(Data data) {
        return NakedObjects.getSpecificationLoader().loadSpecification(data.getClassName());
    }

    public CreateObjectCommand createCreateObjectCommand(final NakedObject object) {
        return new CreateObjectCommand() {
            public void execute() throws ObjectManagerException {
                LOG.debug("  create object " + object);
                Data data = createObjectData(object, true);
                dataManager.insert(data);
            }

            public NakedObject onObject() {
                return object;
            }

            public String toString() {
                return "CreateObjectCommand [object=" + object + "]";
            }
        };
    }

    public DestroyObjectCommand createDestroyObjectCommand(final NakedObject object) {
        return new DestroyObjectCommand() {
            public void execute() throws ObjectManagerException {
                LOG.debug("  destroy object " + object);
                dataManager.remove((SerialOid) object.getOid());
            }

            public NakedObject onObject() {
                return object;
            }

            public String toString() {
                return "DestroyObjectCommand [object=" + object + "]";
            }
        };
    }

    private ObjectData createObjectData(NakedObject object, boolean ensurePersistent) {
        LOG.debug("Compiling object data for " + object);

        ObjectData data;
        data = new ObjectData(object.getSpecification(), (SerialOid) object.getOid());

        NakedObjectField[] fields = object.getSpecification().getFields();

        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isDerived()) {
                // don't persist derived fields
                continue;
            }

            Naked field = object.getField(fields[i]);
            Naked fieldContent = field;
            String fieldName = fields[i].getName();

            if (fieldContent instanceof InternalCollection) {
                data.addInternalCollection((InternalCollection) fieldContent, fieldName, ensurePersistent);
            } else if (fields[i].isValue()) {
                data.saveValue(fieldName, object.isEmpty(fields[i]), field == null ? null : field.getObject().toString());
            } else {
                data.addAssociation((NakedObject) fieldContent, fieldName, ensurePersistent);
            }
        }

        return data;
    }

    public SaveObjectCommand createSaveObjectCommand(final NakedObject object) {
        return new SaveObjectCommand() {
            public void execute() throws ObjectManagerException {
                LOG.debug("  save object " + object);

                if (object instanceof InternalCollection) {
                    NakedObject parent = ((InternalCollection) object).parent();
                    Data data = createObjectData(parent, true);
                    dataManager.save(data);
                } else {
                    Data data = createObjectData(object, true);
                    dataManager.save(data);
                }
            }

            public NakedObject onObject() {
                return object;
            }

            public String toString() {
                return "SaveObjectCommand [object=" + object + "]";
            }

        };
    }

    public void endTransaction() {
        LOG.debug("end transaction");
    }

    public String getDebugData() {
        DebugString debug = new DebugString();
        debug.appendTitle("Business Objects");
        debug.appendln(dataManager.getDebugData());
        return debug.toString();
    }

    public String getDebugTitle() {
        return "XML Object Store";
    }

    public NakedObject[] getInstances(InstancesCriteria criteria) throws ObjectManagerException, UnsupportedFindException {
        LOG.debug("getInstances of " + criteria.getSpecification() + " where " + criteria);
        ObjectData patternData = new ObjectData(criteria.getSpecification(), null);
        NakedObject[] instances = getInstances(patternData, criteria);
        return instances;
    }

    private NakedObject[] getInstances(NakedObjectSpecification specification) throws ObjectManagerException {
        LOG.debug("getInstances of " + specification);
        ObjectData patternData = new ObjectData(specification, null);
        NakedObject[] instances = getInstances(patternData, null);
        return instances;
    }

    public NakedObject[] getInstances(NakedObjectSpecification specification, boolean includeSubclasses)
            throws ObjectManagerException {
        LOG.debug("getInstances of " + specification);
        if (includeSubclasses) {
            NakedObject[] instances = getInstances(specification);
            NakedObjectSpecification[] subclasses = specification.subclasses();
            for (int i = 0; i < subclasses.length; i++) {
                NakedObject[] subclassInstances = getInstances(subclasses[i], true);
                if (subclassInstances != null) {
                    NakedObject[] in = new NakedObject[instances.length + subclassInstances.length];
                    System.arraycopy(instances, 0, in, 0, instances.length);
                    System.arraycopy(subclassInstances, 0, in, 0, subclassInstances.length);
                    instances = in;
                }
            }
            return instances;
        } else {
            return getInstances(specification);
        }
    }

    private NakedObject[] getInstances(ObjectData patternData, InstancesCriteria criteria) throws ObjectManagerException {
        ObjectDataVector data = dataManager.getInstances(patternData);
        NakedObject[] instances = new NakedObject[data.size()];
        int count = 0;

        for (int i = 0; i < data.size(); i++) {
            ObjectData instanceData = data.element(i);
            LOG.debug("instance data " + instanceData);

            SerialOid oid = instanceData.getOid();

            NakedObjectSpecification spec = specFor(instanceData);
            NakedObject instance = objectLoader.recreateAdapterForPersistent(oid, spec);
            initObject(instance, instanceData);

            if (criteria == null || criteria.matches(instance)) {
                instances[count++] = instance;
            }
        }

        NakedObject[] array = new NakedObject[count];
        System.arraycopy(instances, 0, array, 0, count);
        return array;
    }

    public NakedClass getNakedClass(String name) throws ObjectNotFoundException, ObjectManagerException {
        throw new ObjectNotFoundException();
    }

    public NakedObject getObject(Oid oid, NakedObjectSpecification hint) throws ObjectNotFoundException, ObjectManagerException {
        LOG.debug("getObject " + oid);
        Data data = dataManager.loadData((SerialOid) oid);
        LOG.debug("  data read " + data);

        NakedObject object;

        if (data instanceof ObjectData) {
            object = recreateObject((ObjectData) data);
        } else if (data instanceof CollectionData) {
            throw new NakedObjectRuntimeException();
            //            object = (NakedObject) specFor(data).acquireInstance();
        } else {
            throw new ObjectNotFoundException();
        }
        return object;
    }

    public boolean hasInstances(NakedObjectSpecification cls, boolean includeSubclasses) {
        LOG.debug("checking instance of " + cls);
        return numberOfInstances(cls, includeSubclasses) > 0;
    }

    public void init() throws ObjectManagerException {
        objectLoader = NakedObjects.getObjectLoader();
    }

    private void initObject(NakedObject object, ObjectData data) throws ObjectManagerException {
        if (object.getResolveState().isResolvable(ResolveState.RESOLVING)) {
            objectLoader.start(object, ResolveState.RESOLVING);

            NakedObjectField[] fields = object.getFields();
            for (int i = 0; i < fields.length; i++) {
                NakedObjectField field = fields[i];
                if (field.isDerived()) {
                    continue;
                }

                if (field.isValue()) {
                    object.initValue((OneToOneAssociation) field, data.get(field.getName()));
                } else if (field instanceof OneToManyAssociation) {
                    initObjectSetupCollection(object, data, field);
                } else {
                    initObjectSetupReference(object, data, field);
                }
            }

            objectLoader.end(object);
        }
    }

    private void initObjectSetupReference(NakedObject object, ObjectData data, NakedObjectField field) {
        SerialOid referenceOid = (SerialOid) data.get(field.getName());
        LOG.debug("setting up field " + field + " with " + referenceOid);
        if (referenceOid == null) {
            return;
        }

        Data fieldData = (Data) dataManager.loadData(referenceOid);

        NakedObject reference = objectLoader.recreateAdapterForPersistent(referenceOid, specFor(fieldData));
        object.initAssociation((OneToOneAssociation) field, reference);

        /*
         * if (loadedObjects().isLoaded(referenceOid)) { NakedObject
         * loadedObject = loadedObjects().getLoadedObject(referenceOid);
         * LOG.debug("using loaded object " + loadedObject);
         * object.initAssociation((OneToOneAssociation) field, loadedObject); }
         * else { NakedObject fieldObject; Data fieldData = (Data)
         * dataManager.loadData((SerialOid) referenceOid);
         * 
         * if (fieldData != null) { fieldObject = (NakedObject)
         * specFor(fieldData).acquireInstance(); } else { fieldObject =
         * (NakedObject) field.getSpecification().acquireInstance(); }
         * 
         * fieldObject.setOid(referenceOid);
         * 
         * if (fieldObject instanceof InternalCollection) {
         * fieldObject.setResolved(); }
         * 
         * loadedObjects().loaded(fieldObject);
         * object.initAssociation((OneToOneAssociation) field, fieldObject); }
         */
    }

    private void initObjectSetupCollection(NakedObject object, ObjectData data, NakedObjectField field) {
        /*
         * The internal collection is already a part of the object, and
         * therefore cannot be recreated, but its oid must be set
         */
        ReferenceVector refs = (ReferenceVector) data.get(field.getName());

        if (refs == null) {
            return;
        }

        for (int j = 0; j < refs.size(); j++) {
            try {
                SerialOid elementOid = refs.elementAt(j);
                NakedObject adapter;
                if (objectLoader.isIdentityKnown(elementOid)) {
                    adapter = objectLoader.getAdapterFor(elementOid);
                } else {
                    adapter = getObject(elementOid, null);
                }
                object.initAssociation((NakedObjectAssociation) field, adapter);

                /*
                 * if (loadedObjects().isLoaded(elementOid)) {
                 * object.initAssociation((NakedObjectAssociation) field,
                 * loadedObjects().getLoadedObject( elementOid)); } else {
                 * object.initAssociation((NakedObjectAssociation) field,
                 * getObject(elementOid, null)); }
                 */
            } catch (ObjectNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public String name() {
        return "XML";
    }

    public int numberOfInstances(NakedObjectSpecification cls, boolean includedSubclasses) {
        ObjectData data = new ObjectData(cls, null);
        return dataManager.numberOfInstances(data);
    }

    /*
     * The ObjectData holds all references for internal collections, so the
     * object should haves its internal collection populated by this method.
     */
    private NakedObject recreateObject(ObjectData data) throws ObjectManagerException {
        SerialOid oid = data.getOid();

        NakedObjectSpecification spec = specFor(data);
        NakedObject object = objectLoader.recreateAdapterForPersistent(oid, spec);

        /*
         * the above two lines replaces all this
         * 
         * if (loadedObjects().isLoaded(oid)) { return
         * loadedObjects().getLoadedObject(oid); } NakedObjectSpecification nc =
         * classFor(data.getClassName()); NakedObject object = (NakedObject)
         * nc.acquireInstance(); LOG.debug("Recreating object " +
         * nc.getFullName() + "/" + oid); object.setOid(oid);
         * loadedObjects().loaded(object);
         */
        initObject(object, data);
        //object.setResolved();
        return object;
    }

    public void resolveEagerly(NakedObject object, NakedObjectField field) throws ObjectManagerException {}

    public void resolveImmediately(NakedObject object) throws ObjectManagerException {
        LOG.info("resolve-immediately: " + object);
        ObjectData data = (ObjectData) dataManager.loadData((SerialOid) object.getOid());
        Assert.assertNotNull("Not able to read in data during resolve", object, data);
        initObject(object, data);
    }

    public void reset() {}

    public void runTransaction(PersistenceCommand[] commands) throws ObjectManagerException {
        LOG.info("start execution of transaction");
        for (int i = 0; i < commands.length; i++) {
            PersistenceCommand command = commands[i];
            command.execute();
        }
        LOG.info("end execution");
    }

    /**
     * Expose as a .NET property
     * 
     * @property
     */
    public void set_DataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public void shutdown() throws ObjectManagerException {
        LOG.info("shutdown " + this);
    }

    public void startTransaction() {
        LOG.debug("start transaction");
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
