package org.nakedobjects.persistence.file;

import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.LoadedObjects;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectContext;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.NakedObjectStore;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.ObjectNotFoundException;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.UnsupportedFindException;
import org.nakedobjects.object.defaults.LoadedObjectsHashtable;
import org.nakedobjects.object.defaults.SerialOid;
import org.nakedobjects.object.reflect.FieldSpecification;
import org.nakedobjects.object.reflect.OneToOneAssociationSpecification;

import org.apache.log4j.Logger;


/**
 * This object store deals with composite collections by including their contents with the contents
 * of the parent object, i.e. the Associations and OneToManyAssociations are dealt with together
 */
public abstract class MementoObjectStore implements NakedObjectStore {
    private static final Logger LOG = Logger.getLogger(MementoObjectStore.class);
    private DataManager dataManager;
    private LoadedObjects loadedObjects;
    private static final NakedObjectSpecification NAKED_CLASS_SPEC = NakedObjectSpecificationLoader.getInstance().loadSpecification(NakedClass.class);

    public MementoObjectStore(DataManager manager) {
        this.dataManager = manager;
        loadedObjects = new LoadedObjectsHashtable();
    }

    public void abortTransaction() {}

    private NakedObjectSpecification classFor(String type) {
        return NakedObjectSpecificationLoader.getInstance().loadSpecification(type);
    }

    public void createNakedClass(NakedClass cls) throws ObjectStoreException {
        createObject(cls);
    }

    public void createObject(NakedObject object) throws ObjectStoreException {
        LOG.debug("createObject " + object);
        Data data = createObjectData(object, true);
        dataManager.insert(data);
    }

    private ObjectData createObjectData(NakedObject object, boolean ensurePersistent) {
        LOG.debug("Compiling object data for " + object);

        ObjectData data;
        data = new ObjectData(object.getSpecification(), (SerialOid) object.getOid());

        FieldSpecification[] fields = object.getSpecification().getFields();

        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isDerived()) {
                // don't persist derived fields
                continue;
            }

            Naked fieldContent = (Naked) fields[i].get(object);
            String fieldName = fields[i].getName();

            if (fieldContent instanceof InternalCollection) {
                data.addInternalCollection((InternalCollection) fieldContent, fieldName, ensurePersistent);
            } else if (fieldContent instanceof NakedValue) {
                data.addValue((NakedValue) fieldContent, fieldName);
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

    public void destroyObject(NakedObject object) throws ObjectStoreException {
        dataManager.remove((SerialOid) object.getOid());
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

    public NakedObject[] getInstances(NakedObjectSpecification cls, boolean includeSubclasses) throws ObjectStoreException {
        LOG.debug("getInstances of " + cls);
        if(includeSubclasses) {
            NakedObject[] instances = getInstances(cls);
            NakedObjectSpecification[] subclasses = cls.subclasses();
            for (int i = 0; i < subclasses.length; i++) {
                NakedObject[] subclassInstances = getInstances(subclasses[i], true);
                if(subclassInstances != null) {
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

    private NakedObject[] getInstances(NakedObjectSpecification cls) throws ObjectStoreException {
        LOG.debug("getInstances of " + cls);
        ObjectData patternData = new ObjectData(cls, null);
        NakedObject[] instances = getInstances(patternData, null);
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
                instance.setContext(NakedObjectContext.getDefaultContext());
                loadedObjects.loaded(instance);
	            instance.setResolved();
            }
            initObject(instance, instanceData);
            
            if(title == null || instance.titleString().toLowerCase().indexOf(titlePattern) >= 0) {
                instances[count++] = instance;
            }
        }
        
        NakedObject[] array = new NakedObject[count];
        System.arraycopy(instances, 0, array, 0, count);
        return array;
    }

    public NakedObject[] getInstances(NakedObjectSpecification cls, String pattern, boolean includeSubclasses) throws ObjectStoreException, UnsupportedFindException {
        LOG.debug("getInstances like " + pattern);
        ObjectData patternData = new ObjectData(cls, null);
        NakedObject[] instances = getInstances(patternData, pattern);
        return instances;
    }

    public NakedObject[] getInstances(NakedObject pattern, boolean includeSubclasses) throws ObjectStoreException {
        LOG.debug("getInstances like " + pattern);
        ObjectData patternData = createObjectData(pattern, false);
        NakedObject[] instances = getInstances(patternData, null);
        return instances;
    }

    public LoadedObjects getLoadedObjects() {
        return loadedObjects;
    }

    public NakedClass getNakedClass(String name) throws ObjectNotFoundException, ObjectStoreException {
        NakedObject[] instances = getInstances(NAKED_CLASS_SPEC, true);
        for (int i = 0, len = instances.length; i < len; i++) {
           NakedClass cls = (NakedClass) instances[i];
           if(cls.getName().equals(name)) {
               return cls;
           }
        }

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

        object.setContext(NakedObjectContext.getDefaultContext());
        return object;
    }

    public boolean hasInstances(NakedObjectSpecification cls, boolean includeSubclasses) {
        LOG.debug("checking instance of " + cls);
        return numberOfInstances(cls, includeSubclasses) > 0;
    }

    public void init() throws ObjectStoreException {}

    private void initObject(NakedObject object, ObjectData data) throws ObjectStoreException {
        FieldSpecification[] fields = object.getSpecification().getFields();

        for (int i = 0; i < fields.length; i++) {
            FieldSpecification field = fields[i];

            if (field.isDerived()) {
                continue;
            }

            if (field.isValue()) {
                data.restoreValue(field.getName(), (NakedValue) field.get(object));
            } else if (field.isPart()) {
                /*
                 * The internal collection is already a part of the object, and therefore cannot be
                 * recreated, but its oid must be set
                 */
                ReferenceVector refs = (ReferenceVector) data.get(field.getName());

                if (refs != null) {
                    InternalCollection collection = (InternalCollection) field.get(object);
                    LOG.debug("setting collection " + field + "; assigning " + refs.getOid() + " to " + collection);
                    collection.setOid(refs.getOid());

                    for (int j = 0; j < refs.size(); j++) {
                        try {
                            if (loadedObjects.isLoaded(refs.elementAt(j))) {
                                collection.added(loadedObjects.getLoadedObject(refs.elementAt(j)));
                            } else {
                                collection.added(getObject(refs.elementAt(j), null));
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
                        ((OneToOneAssociationSpecification) field).initData(object, loadedObject);
                    } else {
                        Oid oid = reference;
                        NakedObject fieldObject;
                        Data fieldData = (Data) dataManager.loadData((SerialOid) oid);

                        if (fieldData != null) {
                            fieldObject = (NakedObject) classFor(fieldData.getClassName()).acquireInstance();
                        } else {
                            fieldObject = (NakedObject) field.getType().acquireInstance();
                        }

                        fieldObject.setOid(oid);

                        if (fieldObject instanceof InternalCollection) {
                            fieldObject.setResolved();
                        }

                        loadedObjects.loaded(fieldObject);
                        ((OneToOneAssociationSpecification) field).initData(object, fieldObject);
                    }
                }
            }
        }
    }

    public int numberOfInstances(NakedObjectSpecification cls, boolean includedSubclasses) {
        ObjectData data = new ObjectData(cls, null);
        return dataManager.numberOfInstances(data);
    }

    /*
     * The ObjectData holds all references for internal collections, so the object should haves its
     * internal collection populated by this method.
     */
    private NakedObject recreateObject(ObjectData data) throws ObjectStoreException {
        SerialOid oid = data.getOid();
        if (loadedObjects.isLoaded(oid)) { return loadedObjects.getLoadedObject(oid); }
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
        if(data == null) {
            LOG.warn("Not able to read in data - during resolve - for " + object);
        } else {
            initObject(object, data);
        }
    }

    public void save(NakedObject object) throws ObjectStoreException {
        LOG.debug("Save object " + object);

        Data data = createObjectData(object, true);

        dataManager.save(data);
    }

    public void shutdown() throws ObjectStoreException {}

    public void startTransaction() {}
}