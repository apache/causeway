package org.nakedobjects.persistence.file;

import org.nakedobjects.object.InstancesCriteria;
import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.LoadedObjects;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.persistence.CreateObjectCommand;
import org.nakedobjects.object.persistence.DestroyObjectCommand;
import org.nakedobjects.object.persistence.NakedObjectStore;
import org.nakedobjects.object.persistence.ObjectNotFoundException;
import org.nakedobjects.object.persistence.ObjectStoreException;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.persistence.PersistenceCommand;
import org.nakedobjects.object.persistence.SaveObjectCommand;
import org.nakedobjects.object.persistence.UnsupportedFindException;
import org.nakedobjects.object.persistence.defaults.SerialOid;
import org.nakedobjects.object.reflect.NakedObjectAssociation;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;

import org.apache.log4j.Logger;


public class XmlObjectStore implements NakedObjectStore {
    private static final Logger LOG = Logger.getLogger(XmlObjectStore.class);

    private DataManager dataManager;
    private LoadedObjects loadedObjects;

    public void abortTransaction() {}

    private NakedObjectSpecification classFor(String type) {
        return NakedObjectSpecificationLoader.getInstance().loadSpecification(type);
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

    private Object createSkeletalObject(Oid oid, String type) {
        if (loadedObjects.isLoaded(oid)) {
            return loadedObjects.getLoadedObject(oid);
        } else {
            LOG.debug("Creating skeletal object of " + type + " " + oid);
            NakedObjectSpecification cls = NakedObjectSpecificationLoader.getInstance().loadSpecification(type);
            NakedObject object = (NakedObject) cls.acquireInstance();
            object.setOid(oid);

            return object;
        }
    }

    public void endTransaction() {}

    public String getDebugData() {
        StringBuffer data = new StringBuffer();
        data.append("DataManager " + dataManager);
        data.append("ObjectManager " + loadedObjects);
        return data.toString();
    }

    public String getDebugTitle() {
        return "MementoObjectStore";
    }

    public NakedObject[] getInstances(InstancesCriteria criteria, boolean includeSubclasses) throws ObjectStoreException,
            UnsupportedFindException {
        throw new UnsupportedFindException();
    }

    public NakedObject[] getInstances(NakedObject pattern, boolean includeSubclasses) throws ObjectStoreException {
        LOG.debug("getInstances like " + pattern);
        ObjectData patternData = createObjectData(pattern, false);
        NakedObject[] instances = getInstances(patternData, null);
        return instances;
    }

    private NakedObject[] getInstances(NakedObjectSpecification cls) throws ObjectStoreException {
        LOG.debug("getInstances of " + cls);
        ObjectData patternData = new ObjectData(cls, null);
        NakedObject[] instances = getInstances(patternData, null);
        return instances;
    }

    public NakedObject[] getInstances(NakedObjectSpecification cls, boolean includeSubclasses) throws ObjectStoreException {
        LOG.debug("getInstances of " + cls);
        if (includeSubclasses) {
            NakedObject[] instances = getInstances(cls);
            NakedObjectSpecification[] subclasses = cls.subclasses();
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
            return getInstances(cls);
        }
    }

    public NakedObject[] getInstances(NakedObjectSpecification cls, String pattern, boolean includeSubclasses)
            throws ObjectStoreException, UnsupportedFindException {
        LOG.debug("getInstances like " + pattern);
        ObjectData patternData = new ObjectData(cls, null);
        NakedObject[] instances = getInstances(patternData, pattern);
        return instances;
    }

    private NakedObject[] getInstances(ObjectData patternData, String title) throws ObjectStoreException {
        ObjectDataVector data = dataManager.getInstances(patternData);
        NakedObject[] instances = new NakedObject[data.size()];
        int count = 0;

        String titlePattern = title == null ? "" : title.toLowerCase();

        for (int i = 0; i < data.size(); i++) {
            ObjectData instanceData = data.element(i);
            LOG.debug("instance data " + instanceData);

            SerialOid oid = instanceData.getOid();
            NakedObject instance;
            if (loadedObjects.isLoaded(oid)) {
                instance = loadedObjects.getLoadedObject(oid);
            } else {
                instance = (NakedObject) createSkeletalObject(oid, instanceData.getClassName());
                //                instance.setContext(NakedObjectContext.getDefaultContext());
                loadedObjects.loaded(instance);
            }
            initObject(instance, instanceData);
            if (!instance.isResolved()) {
                instance.setResolved();
            }

            if (title == null || instance.titleString().toLowerCase().indexOf(titlePattern) >= 0) {
                instances[count++] = instance;
            }
        }

        NakedObject[] array = new NakedObject[count];
        System.arraycopy(instances, 0, array, 0, count);
        return array;
    }

    public LoadedObjects getLoadedObjects() {
        return loadedObjects;
    }

    public NakedClass getNakedClass(String name) throws ObjectNotFoundException, ObjectStoreException {
        /*
         * NakedObject[] instances = getInstances(NAKED_CLASS_SPEC, true); for
         * (int i = 0, len = instances.length; i < len; i++) { NakedClass cls =
         * (NakedClass) instances[i]; if(cls.getName().equals(name)) { return
         * cls; } }
         */
        throw new ObjectNotFoundException();
    }

    public NakedObject getObject(Oid oid, NakedObjectSpecification hint) throws ObjectNotFoundException, ObjectStoreException {
        LOG.debug("getObject " + oid);
        Data data = dataManager.loadData((SerialOid) oid);
        LOG.debug("Data read " + data);

        NakedObject object;

        if (data instanceof ObjectData) {
            object = recreateObject((ObjectData) data);
        } else if (data instanceof CollectionData) {
            object = (NakedObject) classFor(data.getClassName()).acquireInstance();
        } else {
            throw new ObjectNotFoundException();
        }
        return object;
    }

    public boolean hasInstances(NakedObjectSpecification cls, boolean includeSubclasses) {
        LOG.debug("checking instance of " + cls);
        return numberOfInstances(cls, includeSubclasses) > 0;
    }

    public void init() throws ObjectStoreException {}

    private void initObject(NakedObject object, ObjectData data) throws ObjectStoreException {
        NakedObjectField[] fields = object.getSpecification().getFields();

        for (int i = 0; i < fields.length; i++) {
            NakedObjectField field = fields[i];

            if (field.isDerived()) {
                continue;
            }

            if (field.isValue()) {
                object.setValue((OneToOneAssociation) field, data.get(field.getName()));
            } else if (field instanceof OneToManyAssociation) {
                /*
                 * The internal collection is already a part of the object, and
                 * therefore cannot be recreated, but its oid must be set
                 */
                ReferenceVector refs = (ReferenceVector) data.get(field.getName());

                if (refs != null) {
                    for (int j = 0; j < refs.size(); j++) {
                        try {
                            if (loadedObjects.isLoaded(refs.elementAt(j))) {
                                object.initAssociation((NakedObjectAssociation) field, loadedObjects.getLoadedObject(refs
                                        .elementAt(j)));
                            } else {
                                object.initAssociation((NakedObjectAssociation) field, getObject(refs.elementAt(j), null));
                            }
                        } catch (ObjectNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                Oid reference = (Oid) data.get(field.getName());
                LOG.debug("setting field " + field + " with " + reference);

                if (reference != null) {
                    if (loadedObjects.isLoaded(reference)) {
                        NakedObject loadedObject = loadedObjects.getLoadedObject(reference);
                        LOG.debug("using loaded object " + loadedObject);
                        object.initAssociation((OneToOneAssociation) field, loadedObject);
                    } else {
                        Oid oid = reference;
                        NakedObject fieldObject;
                        Data fieldData = (Data) dataManager.loadData((SerialOid) oid);

                        if (fieldData != null) {
                            fieldObject = (NakedObject) classFor(fieldData.getClassName()).acquireInstance();
                        } else {
                            fieldObject = (NakedObject) field.getSpecification().acquireInstance();
                        }

                        fieldObject.setOid(oid);

                        if (fieldObject instanceof InternalCollection) {
                            fieldObject.setResolved();
                        }

                        loadedObjects.loaded(fieldObject);
                        object.initAssociation((OneToOneAssociation) field, fieldObject);
                    }
                }
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
    private NakedObject recreateObject(ObjectData data) throws ObjectStoreException {
        SerialOid oid = data.getOid();
        if (loadedObjects.isLoaded(oid)) {
            return loadedObjects.getLoadedObject(oid);
        }
        NakedObjectSpecification nc = classFor(data.getClassName());
        NakedObject object = (NakedObject) nc.acquireInstance();
        LOG.debug("Recreating object " + nc.getFullName() + "/" + oid);
        object.setOid(oid);
        loadedObjects.loaded(object);
        initObject(object, data);
        object.setResolved();
        return object;
    }

    public void resolve(NakedObject object) throws ObjectStoreException {
        ObjectData data = (ObjectData) dataManager.loadData((SerialOid) object.getOid());
        if (data == null) {
            LOG.warn("Not able to read in data - during resolve - for " + object);
        } else {
            initObject(object, data);
        }
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Expose as a .NET property
     * 
     * @property
     */
    public void set_DataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public void setLoadedObjects(LoadedObjects loadedObjects) {
        this.loadedObjects = loadedObjects;
    }

    /**
     * Expose as a .NET property
     * 
     * @property
     */
    public void set_LoadedObjects(LoadedObjects loadedObjects) {
        this.loadedObjects = loadedObjects;
    }

    public void shutdown() throws ObjectStoreException {}

    public void startTransaction() {}

    public CreateObjectCommand createCreateObjectCommand(final NakedObject object) {
        return new CreateObjectCommand() {
            public void execute() throws ObjectStoreException {
                LOG.debug("createObject " + object);
                Data data = createObjectData(object, true);
                dataManager.insert(data);
            }
        };
    }

    public DestroyObjectCommand createDestroyObjectCommand(final NakedObject object) {
        return new DestroyObjectCommand() {
            public void execute() throws ObjectStoreException {
                dataManager.remove((SerialOid) object.getOid());
            }
        };
    }

    public SaveObjectCommand createSaveObjectCommand(final NakedObject object) {
        return new SaveObjectCommand() {
            public void execute() throws ObjectStoreException {
                LOG.debug("Save object " + object);

                if (object instanceof InternalCollection) {
                    NakedObject parent = ((InternalCollection) object).parent();
                    Data data = createObjectData(parent, true);
                    dataManager.save(data);
                } else {
                    Data data = createObjectData(object, true);
                    dataManager.save(data);
                }
            }
        };
    }

    public void runTransaction(PersistenceCommand[] commands) throws ObjectStoreException {
        for (int i = 0; i < commands.length; i++) {
            PersistenceCommand command = commands[i];
            command.execute();
        }
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
