package org.nakedobjects.persistence.cache;

import org.nakedobjects.container.configuration.ConfigurationFactory;
import org.nakedobjects.object.InstancesCriteria;
import org.nakedobjects.object.LoadedObjects;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectContext;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.defaults.LoadedObjectsHashtable;
import org.nakedobjects.object.io.BinaryTransferableWriter;
import org.nakedobjects.object.io.Memento;
import org.nakedobjects.object.io.Transferable;
import org.nakedobjects.object.persistence.NakedObjectStore;
import org.nakedobjects.object.persistence.ObjectNotFoundException;
import org.nakedobjects.object.persistence.ObjectStoreException;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.persistence.UnsupportedFindException;
import org.nakedobjects.object.reflect.FieldSpecification;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.utility.NotImplementedException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Category;


// TODO empty naked values won't work properly (i think)
public class CacheObjectStore implements NakedObjectStore {
    private final static Category LOG = Category.getInstance(CacheObjectStore.class);
    private String directoryPath = "tmp/test-cache";
    private ObjectOutputStream journal;
    private String journalFilename = "journal";
    private String PADDING = "00000000";
    private String snapshotFilename = "snapshot";
    private String suffix = ".data";
    private int version = 0;
    private LoadedObjects loadedObjects;
    private Hashtable objectSets;

    CacheObjectStore(String directory) {
        setDirectory(directory);
    }
    
    public void setDirectory(String directory) {
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        directoryPath = directory;
    }

    public CacheObjectStore() {
        String dir = ConfigurationFactory.getConfiguration().getString("cache-object-store.directory", "tmp/cache-data");
        setDirectory(dir);
    }

    public void abortTransaction() {}

    private void applyJournals() throws ObjectStoreException {
    /*
     * File file = file(journalFilename, version, false);
     * 
     * try { while (file.exists()) { LOG.info("Applying journal " + file);
     * 
     * ObjectInputStream journal = new ObjectInputStream(new BufferedInputStream( new
     * FileInputStream(file))); LOG.debug("Journal header: " + journal.readObject());
     * 
     * String action;
     * 
     * while (true) { try { action = (String) journal.readObject(); } catch (EOFException e) {
     * break; }
     * 
     * Object data = journal.readObject();
     * 
     * LOG.debug("journal entry: " + action + " " + data);
     * 
     * if ("save".equals(action)) { NakedObjectMemento memento = (NakedObjectMemento) data;
     * NakedObject object = getObject(memento.getOid(), null); memento.updateNakedObject(object,
     * objectManager);
     *  } else if ("create".equals(action)) { NakedObjectMemento memento = (NakedObjectMemento)
     * data; NakedObject object = memento.recreateObject(objectManager);
     * persistentObjectIndex.put(object.getOid(), object);
     *  } else if ("delete".equals(action)) { persistentObjectIndex.remove(data); } // } else if
     * ("save".equals(action)) { }
     * 
     * version++; file = file(journalFilename, version, false); } } catch (FileNotFoundException e) {
     * throw new ObjectStoreException("Failed to open journal file " + file, e); } catch
     * (IOException e) { throw new ObjectStoreException("Failed to process journal file " + file,
     * e); } catch (ClassNotFoundException e) { throw new ObjectStoreException( "Invalid object read
     * in from journal " + file, e); }
     *  
     */
    }

    private void closeJournal() throws ObjectStoreException {
        try {
            LOG.info("Closing journal " + journalFilename);
            journal.close();
        } catch (IOException e) {
            throw new ObjectStoreException("Failed to close journal", e);
        }
    }

    public void createObject(NakedObject object) throws ObjectStoreException {
        NakedObjectSpecification nakedClass = object.getSpecification();
        if(!nakedClass.isCollection()) {
	        writeJournal("create", new Memento(object));
	        instances(nakedClass).create(object);
        }
    }

    public void createNakedClass(NakedObject cls) throws ObjectStoreException {
        String className = cls.getName();
        if(objectSets.containsKey(className)) {
            throw new NakedObjectRuntimeException("Class already created: " + cls);
        }
        BinaryTransferableWriter data = new BinaryTransferableWriter();
        data.writeString(className);
        data.writeObject((Transferable) cls.getOid());
        writeJournal("class", data);
        Instances index = new Instances(cls.forNakedClass(), loadedObjects);
        objectSets.put(className, index);
    }
    
     private Instances instances(NakedObjectSpecification cls) {
        String className = cls.getFullName();
        
        if (objectSets.containsKey(className)) {
            return (Instances) objectSets.get(className);
        } else {
            Instances index = new Instances(cls, loadedObjects);
            objectSets.put(className, index);
            return index;
        }
     }
    
    private Instances instances(String className) throws ObjectNotFoundException {
        if (objectSets.containsKey(className)) {
            return (Instances) objectSets.get(className);
        } else {
            throw new ObjectNotFoundException();
        }
    }

    public void destroyObject(NakedObject object) {
        writeJournal("delete", new Memento(object));
        instances(object.getSpecification()).remove(object);
    }

    public void endTransaction() {}

    private File file(String filenameBase, int version, boolean temp) {
        File directory = new File(directoryPath);
        String number = PADDING + version;
        String filepath = filenameBase + number.substring(number.length() - PADDING.length()) + (temp ? ".tmp" : suffix);

        return new File(directory, filepath);
    }

    public String getDebugData() {
        return null;
    }

    public String getDebugTitle() {
        return null;
    }

    public NakedObject[] getInstances(NakedObjectSpecification specification, boolean includeSubclasses) {
        Vector instances = new Vector();
        Enumeration objects = instances(specification).instances();
        while (objects.hasMoreElements()) {
            NakedObject instance = (NakedObject) objects.nextElement();
            instances.addElement(instance);
        }
        
        return toArray(instances);
    }

    public NakedObject[] getInstances(InstancesCriteria criteria, boolean includeSubclasses) throws ObjectStoreException,
            UnsupportedFindException {
        Vector instances = new Vector();
        Enumeration objects = instances(criteria.getSpecification()).instances();
        while (objects.hasMoreElements()) {
            NakedObject instance = (NakedObject) objects.nextElement();
            if(criteria.matches(instance)) {
                instances.addElement(instance);
            }
        }
        
        return toArray(instances);
    }
    
    private NakedObject[] toArray(Vector instances) {
        NakedObject[] instanceArray = new NakedObject[instances.size()];
        instances.copyInto(instanceArray);
        return instanceArray;
    }

    public NakedObject[] getInstances(NakedObject pattern, boolean includeSubclasses) {
        NakedObjectSpecification requiredType = pattern.getSpecification();
        Vector instances = new Vector();
        Enumeration objects = instances(requiredType).instances();
        while (objects.hasMoreElements()) {
            NakedObject data = (NakedObject) objects.nextElement();

            if (matchesPattern(pattern, data)) {
                instances.addElement(data);
            }
        }

        return toArray(instances);
    }
    
    public NakedObject[] getInstances(NakedObjectSpecification cls, String pattern, boolean includeSubclasses) throws ObjectStoreException, UnsupportedFindException {
        Vector instances = new Vector();
        Enumeration objects = instances(cls).instances();
        String match = pattern.toLowerCase();
        while (objects.hasMoreElements()) {
            NakedObject data = (NakedObject) objects.nextElement();

            if (data.titleString().toLowerCase().indexOf(match) >= 0) {
                instances.addElement(data);
            }
        }

        return toArray(instances);
    }
    
    public LoadedObjects getLoadedObjects() {
        return loadedObjects;
    }

    public NakedObject getObject(Oid oid, NakedObjectSpecification hint) throws ObjectNotFoundException, ObjectStoreException {
            NakedObject object = (NakedObject) instances(hint).read(oid);
            if (object == null) { throw new ObjectNotFoundException(oid); }
            return object;
    }
    
    public NakedClass getNakedClass(String name) throws ObjectNotFoundException, ObjectStoreException {
        Enumeration instances = instances(NakedClass.class.getName()).instances();
        while(instances.hasMoreElements()) {
            NakedClass instance = (NakedClass) instances.nextElement();
            if(instance.getFullName().equals(name)) {
                return instance;
            }
        }
        return null;
    }

    public boolean hasInstances(NakedObjectSpecification cls, boolean includeSubclasses) {
       return numberOfInstances(cls, false) > 0;
    }

    public void init() throws ObjectStoreException {
        loadSnapshot();
        applyJournals();
        openJounal();
    }

    private void loadSnapshot() throws ObjectStoreException {
        loadedObjects = new LoadedObjectsHashtable();
        objectSets = new Hashtable();
        
        File directory = new File(directoryPath);
        String filepath = latestSnapshot(directory);
        File file = new File(directory, filepath);

        if (file.exists()) {
            ObjectInputStream oos = null;

            try {
                LOG.info("Loading objects from " + file + "...");
                oos = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
                loadInstances(oos);
                int size = loadData(oos, NakedObjectContext.getDefaultContext());
                LOG.info(size + " objects loaded from " + file);
            } catch (FileNotFoundException e) {
                throw new ObjectStoreException("Failed to find file " + filepath, e);
            } catch (IOException e) {
                throw new ObjectStoreException("Failed to read file " + filepath, e);
            } catch (ClassNotFoundException e) {
                throw new ObjectStoreException("Failed to read file " + filepath, e);
            } finally {
                if (oos != null) {
                    try {
                        oos.close();
                    } catch (IOException e) {
                        LOG.error("Failed to close file " + filepath, e);
                    }
                }
            }
        } else {
            LOG.info("No snapshot to load: " + filepath);
        }

    }

    private String latestSnapshot(File directory) {
        String[] snapshots = directory.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(snapshotFilename) && name.endsWith(suffix);
            }
        });

        String max = snapshotFilename + PADDING + suffix;

        for (int i = 0; i < snapshots.length; i++) {
            if (max.compareTo(snapshots[i]) < 0) {
                max = snapshots[i];
            }
        }

        String number = max.substring(snapshotFilename.length(), max.length() - suffix.length());
        version = Integer.valueOf(number).intValue() + 1;

        String filepath = snapshotFilename + number + suffix;
        return filepath;
    }

    private int loadData(ObjectInputStream oos, NakedObjectContext context) throws ObjectNotFoundException, IOException, ClassNotFoundException {
        int size = 0;
        int noClasses = oos.readInt();
        for (int k = 0; k < noClasses; k++) {                   
            String className = (String) oos.readObject();
            size += instances(className).loadData(oos, context);
        }
        return size;
    }

    private int loadInstances(ObjectInputStream oos) throws IOException, ClassNotFoundException, ObjectNotFoundException {
        int noClasses = oos.readInt();
        for (int k = 0; k < noClasses; k++) {                   
            String className = (String) oos.readObject();
            Instances instances = instances(className);
            instances.loadIdentities(oos);            
        }
        return noClasses;
    }

    private boolean matchesPattern(NakedObject pattern, NakedObject instance) {
        NakedObject object = instance;
        NakedObjectSpecification nc = object.getSpecification();
        NakedObjectField[] fields = nc.getFields();

        for (int f = 0; f < fields.length; f++) {
            NakedObjectField fld = fields[f];

            // are ignoring internal collections - these probably should be considered
            // ignore derived fields - there is no way to set up these fields
            if (fld.isDerived()) {
                continue;
            }

            if (fld.isValue()) {
                // find the objects
                NakedValue reqd = (NakedValue) fld.getPojo(pattern);
                NakedValue search = (NakedValue) fld.getPojo(object);

                // if pattern contains empty value then it matches anything
                if (reqd.isEmpty()) {
                    continue;
                }

                // compare the titles
                String r = reqd.titleString().toLowerCase();
                String s = search.titleString().toLowerCase();

                // if the pattern occurs in the object
                if (s.indexOf(r) == -1) { return false; }
            } else {
                // find the objects
                NakedObject reqd = (NakedObject) fld.getPojo(pattern);
                NakedObject search = (NakedObject) fld.getPojo(object);

                // if pattern contains null reference then it matches anything
                if (reqd == null) {
                    continue;
                }

                // otherwise there must be a reference, else they can never match
                if (search == null) { return false; }

                if (!reqd.getOid().equals(search.getOid())) { return false; }
            }
        }

        return true;
    }

    public String name() {
        return "Cache Object Store";
    }

    public int numberOfInstances(NakedObjectSpecification cls, boolean includedSubclasses) {
	    return instances(cls).numberInstances();
    }

    private void openJounal() throws ObjectStoreException {
        File file = file(journalFilename, version, false);

        try {
            LOG.info("Creating journal " + file);
            journal = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            journal.writeObject("Journal opened " + new Date());
            journal.flush();
        } catch (FileNotFoundException e) {
            throw new ObjectStoreException("Failed to open jounal file " + file, e);
        } catch (IOException e) {
            throw new ObjectStoreException("Failed to write jounal file " + file, e);
        }
    }

    public void resolveImmediately(NakedObject object) {}

    public void save(NakedObject object) throws ObjectStoreException {
        writeJournal("save", new Memento(object));
    }

    private void saveSnapshot() throws ObjectStoreException {
        File tempFile = file(snapshotFilename, version, true);
        LOG.info("Saving objects in " + tempFile + "...");
        
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)));
            saveInstances(oos);
            saveData(oos);
        } catch (FileNotFoundException e) {
            throw new ObjectStoreException("Failed to find file " + tempFile, e);
        } catch (IOException e) {
            throw new ObjectStoreException("Failed to write to file " + tempFile, e);
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    LOG.error("Failed to close file " + tempFile, e);
                }
            }
        }
        File file = file(snapshotFilename, version, false);
        tempFile.renameTo(file);
        LOG.info("File renamed as " + file);
    }

     private void saveData(ObjectOutputStream oos) throws IOException {
        long size = 0;
        oos.writeInt(objectSets.size());
        Enumeration e1 = objectSets.keys();
        while (e1.hasMoreElements()) {
            String className = (String) e1.nextElement();
            oos.writeObject(className);
            Instances instances = (Instances) objectSets.get(className); 
            size += instances.saveData(oos);
        }
        LOG.info(size + " objects saved");
    }

    private void saveInstances(ObjectOutputStream oos) throws ObjectNotFoundException, IOException {
        oos.writeInt(objectSets.size());
        Enumeration e1 = objectSets.keys();
        while (e1.hasMoreElements()) {
            String className = (String) e1.nextElement();
            oos.writeObject(className);
            instances(className).saveIdentities(oos);
        }
    }

    public void shutdown() throws ObjectStoreException {
        saveSnapshot();
        closeJournal();
    }

    public void startTransaction() {}

    private void writeJournal(String action, Memento object) {
	    BinaryTransferableWriter writer = new BinaryTransferableWriter();
	    object.writeData(writer);
	    writeJournal(action, writer);
    }
    
    private void writeJournal(String action, BinaryTransferableWriter writer) {
        LOG.debug("Journal " + action + " - " + writer);
        try {
            journal.writeUTF(action);
            journal.write(writer.getBinaryData());
            journal.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
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