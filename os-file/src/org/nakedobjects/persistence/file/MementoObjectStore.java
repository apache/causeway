package org.nakedobjects.persistence.file;

import org.nakedobjects.object.LoadedObjects;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedClassManager;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectStore;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.ObjectNotFoundException;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.SimpleOid;
import org.nakedobjects.object.UnsupportedFindException;
import org.nakedobjects.object.collection.InternalCollection;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.object.reflect.OneToOneAssociation;

import java.util.Vector;

import org.apache.log4j.Logger;


/**
 * This object store deals with composite collections by including their contents with the contents
 * of the parent object, i.e. the Associations and OneToManyAssociations are dealt with together
 */
public abstract class MementoObjectStore implements NakedObjectStore {
    private static final Logger LOG = Logger.getLogger(MementoObjectStore.class);
    private DataManager dataManager;
    private LoadedObjects loadedObjects;

    public MementoObjectStore(DataManager manager) {
        this.dataManager = manager;
        loadedObjects = new LoadedObjects();
    }

    public void abortTransaction() {}

    private NakedClass classFor(String type) {
        return NakedClassManager.getInstance().getNakedClass(type);
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
        data = new ObjectData(object.getNakedClass(), (SimpleOid) object.getOid());

        Field[] fields = object.getNakedClass().getFields();

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

    private Object createSkeletalObject(Object oid, String type) {
        if (loadedObjects.isLoaded(oid)) {
            return loadedObjects.getLoadedObject(oid);
        } else {
            LOG.debug("Creating skeletal object of " + type + " " + oid);
            NakedClass cls = NakedClassManager.getInstance().getNakedClass(type);
            NakedObject object = (NakedObject) cls.acquireInstance();
            object.setOid(oid);

            return object;
        }
    }

    public void destroyObject(NakedObject object) throws ObjectStoreException {
        dataManager.remove((SimpleOid) object.getOid());
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

    public Vector getInstances(NakedClass cls, boolean includeSubclasses) {
        LOG.debug("getInstances of " + cls);
        ObjectData patternData = new ObjectData(cls, null);

        ObjectDataVector data = dataManager.getInstances(patternData);
        Vector instances = new Vector(data.size());

        for (int i = 0; i < data.size(); i++) {
            ObjectData instanceData = data.element(i);
            LOG.debug("instance data " + instanceData);

            SimpleOid oid = instanceData.getOid();
            NakedObject instance;
            // TODO don't create new object if one already exists!
            if (loadedObjects.isLoaded(oid)) {
                instance = loadedObjects.getLoadedObject(oid);
            } else {
                instance = (NakedObject) createSkeletalObject(oid, instanceData.getClassName());
                loadedObjects.loaded(instance);
            }
            instance.resolve();
            instances.addElement(instance);
        }

        return instances;
    }

    public Vector getInstances(NakedClass cls, String pattern, boolean includeSubclasses) throws ObjectStoreException, UnsupportedFindException {
        throw new UnsupportedFindException();
    }

    public Vector getInstances(NakedObject pattern, boolean includeSubclasses) throws ObjectStoreException {
        LOG.debug("getInstances like " + pattern);
        ObjectData patternData = createObjectData(pattern, false);

        ObjectDataVector data = dataManager.getInstances(patternData);
        Vector instances = new Vector(data.size());

        for (int i = 0; i < data.size(); i++) {
            ObjectData instanceData = data.element(i);
            LOG.debug("instance data " + instanceData);

            SimpleOid oid = instanceData.getOid();
            NakedObject instance;
            // TODO don't create new object if one already exists!
            if (loadedObjects.isLoaded(oid)) {
                instance = loadedObjects.getLoadedObject(oid);
            } else {
                instance = (NakedObject) createSkeletalObject(oid, instanceData.getClassName());
                loadedObjects.loaded(instance);
	            //resolve(instance);
            }
            instances.addElement(instance);
        }

        return instances;
    }

    public LoadedObjects getLoadedObjects() {
        return loadedObjects;
    }

    public NakedClass getNakedClass(String name) throws ObjectNotFoundException, ObjectStoreException {
        NakedClass pattern = new NakedClass();
        pattern.makeFinder();
        pattern.getName().setValue(name);
        pattern.getReflector().clear();
       
        Vector instances = getInstances((NakedObject) pattern, false);
        if (instances.size() == 1) {
            Object oid = ((NakedClass) instances.elementAt(0)).getOid();
            return (NakedClass) getObject(oid, pattern.getNakedClass());
        } else {
            throw new ObjectNotFoundException();
        }
    }

    public NakedObject getObject(Object oid, NakedClass hint) throws ObjectNotFoundException, ObjectStoreException {
        LOG.debug("getObject " + oid);
        Data data = dataManager.loadData((SimpleOid) oid);
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

    public boolean hasInstances(NakedClass cls, boolean includeSubclasses) {
        LOG.debug("checking instance of " + cls);
        return numberOfInstances(cls, false) > 0;
    }

    public void init() throws ObjectStoreException {}

    private void initObject(NakedObject object, ObjectData data) throws ObjectStoreException {
        Field[] fields = object.getNakedClass().getFields();

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];

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
                Object reference = data.get(field.getName());
                LOG.debug("setting field " + field + " with " + reference);

                if (reference != null) {
                    if (loadedObjects.isLoaded(reference)) {
                        NakedObject loadedObject = loadedObjects.getLoadedObject(reference);
                        LOG.debug("using loaded object " + loadedObject);
                        ((OneToOneAssociation) field).initData(object, loadedObject);
                    } else {
                        Object oid = reference;
                        NakedObject fieldObject;
                        Data fieldData = (Data) dataManager.loadData((SimpleOid) oid);

                        if (fieldData != null) {
                            fieldObject = (NakedObject) classFor(fieldData.getClassName()).acquireInstance();
                        } else {
                            fieldObject = (NakedObject) classFor(field.getType().getName()).acquireInstance();
                        }

                        fieldObject.setOid(oid);

                        if (fieldObject instanceof InternalCollection) {
                            fieldObject.setResolved();
                        }

                        loadedObjects.loaded(fieldObject);
                        ((OneToOneAssociation) field).initData(object, fieldObject);
                    }
                }
            }
        }
    }

    public int numberOfInstances(NakedClass cls, boolean includedSubclasses) {
        ObjectData data = new ObjectData(cls, null);
        return dataManager.numberOfInstances(data);
    }

    /*
     * The ObjectData holds all references for internal collections, so the object should haves its
     * internal collection populated by this method.
     */
    private NakedObject recreateObject(ObjectData data) throws ObjectStoreException {
        SimpleOid oid = data.getOid();
        if (loadedObjects.isLoaded(oid)) { return loadedObjects.getLoadedObject(oid); }
        NakedClass nc = classFor(data.getClassName());
        NakedObject object = (NakedObject) nc.acquireInstance();
        LOG.debug("Recreating object " + nc.fullName() + "/" + oid);
        object.setOid(oid);
        loadedObjects.loaded(object);
        initObject(object, data);
        object.setResolved();
        return object;
    }

    public void resolve(NakedObject object) throws ObjectStoreException {
        ObjectData data = (ObjectData) dataManager.loadData((SimpleOid) object.getOid());
        initObject(object, data);
    }

    public void save(NakedObject object) throws ObjectStoreException {
        LOG.debug("Save object " + object);

        Data data = createObjectData(object, true);

        dataManager.save(data);
    }

    public void shutdown() throws ObjectStoreException {}

    public void startTransaction() {}
}