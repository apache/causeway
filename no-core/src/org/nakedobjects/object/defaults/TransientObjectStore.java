package org.nakedobjects.object.defaults;

import org.nakedobjects.object.InstancesCriteria;
import org.nakedobjects.object.LoadedObjects;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectStore;
import org.nakedobjects.object.ObjectNotFoundException;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.UnsupportedFindException;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.utility.Debug;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Category;


public class TransientObjectStore implements NakedObjectStore {
    private final static Category LOG = Category.getInstance(TransientObjectStore.class);
    private final Hashtable classes;
    private LoadedObjects loaded;
    /*
     * The objects need to store in a repeatable sequence so the elements and instances method
     * return the same data for any repeated call, and so that one subset of instances follows on
     * the previous. This is done by keeping the objects in the order that they where created.
     */
    private final Vector persistentObjectVector;

    public TransientObjectStore() {
//        loaded = new LoadedObjectsHashtable();
        persistentObjectVector = new Vector();
        classes = new Hashtable(30);
    }

    public void abortTransaction() {}

    public void createObject(NakedObject object) throws ObjectStoreException {
        LOG.debug("createObject " + object);
        save(object);
    }

    public void createNakedClass(NakedObject cls) throws ObjectStoreException {
        LOG.debug("createClass " + cls);
 //       cls.setResolved();
        classes.put(((NakedClass) cls.getObject()).getName(), cls);
        persistentObjectVector.addElement(cls);
    }
    
    public void destroyObject(NakedObject object) throws ObjectStoreException {
        LOG.info("Delete requested on '" + object + "'");
        persistentObjectVector.removeElement(object);
    }

    private Enumeration elements() {
        return persistentObjectVector.elements();
    }

    public void endTransaction() {
        LOG.debug("end transaction");
    }

    public String getDebugData() {
        StringBuffer data = new StringBuffer();
        data.append("Objects\n");
        data.append("----------\n");
        Enumeration e = persistentObjectVector.elements();
        while (e.hasMoreElements()) {
            data.append(e.nextElement());
            data.append('\n');
        }
        
        data.append("\n\nObjects\n");
        data.append("----------\n");
        Vector dump = new Vector();
        e = persistentObjectVector.elements();
        while (e.hasMoreElements()) {
            NakedObject object = (NakedObject) e.nextElement();
            data.append(object);
            data.append(debugGraph(object, "name???", 0, dump));
            data.append('\n');
        }

        return data.toString();
    }

    
    public String getDebugTitle() {
        return name();
    }

    public NakedObject[] getInstances(NakedObjectSpecification cls, boolean includeSubclasses) {
        Vector instances = instances(cls);
        return toInstancesArray(instances);
    }

	private Vector instances(NakedObjectSpecification cls) {
        Vector instances = new Vector();
        Enumeration objects = elements();

        while (objects.hasMoreElements()) {
            NakedObject object = (NakedObject) objects.nextElement();

            if (cls.equals(object.getSpecification())) {
                instances.addElement(object);
            }
        }

        LOG.debug("getInstances of " + cls + " => " + instances);
        return instances;
    }

    private NakedObject[] toInstancesArray(Vector instances) {
        NakedObject[] array = new NakedObject[instances.size()];
        instances.copyInto(array);
        return array;
    }

    public NakedObject[] getInstances(NakedObjectSpecification cls, String pattern, boolean includeSubclasses) throws ObjectStoreException, UnsupportedFindException {
	   	Vector instances = instances(cls);
	   	int i = 0;
	   	String match = pattern.toLowerCase();
	   	while(i < instances.size()) {
	   	    NakedObject element = (NakedObject) instances.elementAt(i);
            if(element.titleString().toString().toLowerCase().indexOf(match) == -1) {
                instances.removeElementAt(i);
            } else {
                i++;
            }
	   	}
	   	return  toInstancesArray(instances);
	}
    
    public NakedObject[] getInstances(InstancesCriteria criteria, boolean includeSubclasses) throws ObjectStoreException,
            UnsupportedFindException {
        if (criteria == null) { throw new NullPointerException(); }

        Vector instances = new Vector();
        Enumeration objects = elements();

        NakedObjectSpecification requiredType = criteria.getSpecification();
        while (objects.hasMoreElements()) {
            NakedObject object = (NakedObject) objects.nextElement();

            if (requiredType.equals(object.getSpecification()) && criteria.matches(object)) {
                instances.addElement(object);
            }
        }

        LOG.debug("getInstances for " + criteria + " => " + instances);
        return toInstancesArray(instances);
    }
    
    
	public NakedObject[] getInstances(NakedObject pattern, boolean includeSubclasses) {
        if (pattern == null) { throw new NullPointerException(); }

        Vector instances = new Vector();
        if (pattern instanceof NakedClass) {
            //            NakedClass requiredType = pattern.getNakedClass();
            Enumeration objects = elements();

            String name = ((NakedClass) pattern).getName();

            while (objects.hasMoreElements()) {
                NakedObject object = (NakedObject) objects.nextElement();

                if (object.getObject() instanceof NakedClass && ((NakedClass) object.getObject()).getName().equals(name)) {
                    instances.addElement(object);
                }
            }
        } else {
            NakedObjectSpecification requiredType = pattern.getSpecification();
            Enumeration objects = elements();

            while (objects.hasMoreElements()) {
                NakedObject object = (NakedObject) objects.nextElement();

                if (requiredType.equals(object.getSpecification()) && matchesPattern(pattern, object)) {
                    instances.addElement(object);
                }
            }
        }
        LOG.debug("getInstances like " + pattern + " => " + instances);
        return  toInstancesArray(instances);
    }

    public LoadedObjects getLoadedObjects() {
        return loaded;
    }

    public NakedObject getObject(Oid oid, NakedObjectSpecification hint) throws ObjectNotFoundException, ObjectStoreException {
        LOG.debug("getObject " + oid);
        Enumeration e = elements();
        while (e.hasMoreElements()) {
            NakedObject instance = (NakedObject) e.nextElement();
            if (instance.getOid().equals(oid)) {
                LOG.debug("  got " + instance);
                return instance;
            }
        }
        throw new ObjectNotFoundException(oid);
    }

    public NakedClass getNakedClass(String name) throws ObjectNotFoundException, ObjectStoreException {
        NakedObject object = (NakedObject) classes.get(name);
        if(object == null) {
            throw new ObjectNotFoundException();
        } else {
	        NakedClass nc = (NakedClass) object.getObject();
            return nc;
        }
    }
    
    public boolean hasInstances(NakedObjectSpecification cls, boolean includeSubclasses) {
        Enumeration e = elements();

        while (e.hasMoreElements()) {
            NakedObject data = (NakedObject) e.nextElement();
            if (cls.equals(data.getSpecification())) {
                LOG.debug("hasInstances of " + cls);
                return true;
            }
        }
        LOG.debug("Failed - hasInstances of " + cls);
        return false;
    }

    public void init() throws ObjectStoreException {}

    private boolean matchesPattern(NakedObject pattern, NakedObject instance) {
        NakedObject object = instance;
        NakedObjectSpecification nc = object.getSpecification();
        NakedObjectField[] fields = nc.getFields();

        for (int f = 0; f < fields.length; f++) {
            NakedObjectField fld = fields[f];

            // are ignoring internal collections - these probably should be considered
            // ignore derived fields - there is no way to set up these fields
            if (fld.isPart() || fld.isDerived()) {
                continue;
            }

            if (fld.isValue()) {
                // find the objects
                NakedObject reqd = pattern.getField(fld);
                NakedObject search = object.getField(fld);

                // if pattern contains empty value then it matches anything
                if (reqd.isEmpty(fld)) {
                    continue;
                }

                // compare the titles
                String r = reqd.titleString().toString().toLowerCase();
                String s = search.titleString().toString().toLowerCase();

                // if the pattern occurs in the object
                if (s.indexOf(r) == -1) { return false; }
            } else {
                // find the objects
                NakedObject reqd = pattern.getField(fld);
                NakedObject search = object.getField(fld);

                // if pattern contains null reference then it matches anything
                if (reqd == null) {
                    continue;
                }

                // otherwise there must be a reference, else they can never match
                if (search == null) { return false; }

                if (reqd != search) { return false; }
            }
        }

        return true;
    }

    public String name() {
        return "Transient Object Store";
    }

    public int numberOfInstances(NakedObjectSpecification cls, boolean includedSubclasses) {
        int noInstances = 0;
        Enumeration e = elements();

        while (e.hasMoreElements()) {
            NakedObject data = (NakedObject) e.nextElement();
            if (cls.equals(data.getSpecification())) {
                noInstances++;
            }
        }

        LOG.debug("numberOfInstances of " + cls + " = " + noInstances);
        return noInstances;
    }

    public void resolve(NakedObject object) throws ObjectStoreException {
        LOG.debug("resolve " + object);

        NakedObject o = getObject(object.getOid(), null);
        object.copyObject(o);
    }

    public void save(NakedObject object) throws ObjectStoreException {
        LOG.debug("save " + object);
        if (object.getObject() instanceof NakedClass) { 
            throw new ObjectStoreException("Can't make changes to a NakedClass object"); 
        }
        if (!persistentObjectVector.contains(object)) {
            persistentObjectVector.addElement(object);
        }
    }

    /*
     * public void save(NakedObject object) throws NonPersistentObjectException,
     * ObjectStoreException { LOG.debug("save " + object); if(object instanceof NakedCollection) {
     * NakedCollection collection = (NakedCollection) object; saveCollection(collection); } else {
     * saveObject(object); } }
     * 
     * private void saveCollection(NakedCollection collection) throws ObjectStoreException {
     * LOG.debug("Save collectiton "+collection);
     * 
     * if (!collection.isPersistent()) { collection.setOid(newOid()); LOG.debug("Non persistent,
     * assigned oid "+ collection.getOid()); collection.setResolved();
     * persistentObjectVector.addElement(collection); }
     * 
     * Enumeration e = collection.elements();
     * 
     * while (e.hasMoreElements()) { NakedObject element = (NakedObject) e.nextElement();
     * LOG.debug("adding element " + element); if(element.getOid() == null) { save(element); } } }
     * 
     * 
     * private void saveObject(NakedObject object) throws ObjectStoreException { LOG.debug("Save
     * object "+ object);
     * 
     * if (!object.isPersistent()) { object.setOid(newOid()); LOG.debug("Non persistent, assigned
     * oid "+ object.getOid()); object.setResolved(); persistentObjectVector.addElement(object); }
     * 
     * loaded(object);
     * 
     * Field[] fields = object.getNakedClass().getFields();
     * 
     * for (int i = 0; i < fields.length; i++) { if (fields[i].isDerived()) { // don't persist
     * derived fields continue; }
     * 
     * Naked fieldContent = (Naked) fields[i].get(object); String fieldName = fields[i].getName();
     * 
     * if (fieldContent instanceof InternalCollection) { InternalCollection collection =
     * (InternalCollection) fieldContent; if (!((InternalCollection) fieldContent).isPersistent()) {
     * ((InternalCollection) fieldContent).setOid(newOid()); LOG.debug("Non persistent internal
     * collection, assigned oid "+ object.getOid()); ((InternalCollection)
     * fieldContent).setResolved(); }
     * 
     * Enumeration e = collection.elements();
     * 
     * while (e.hasMoreElements()) { NakedObject element = (NakedObject) e.nextElement();
     * LOG.debug("adding element to internal collection field " + fieldName +" " + element); Object
     * oid = element.getOid(); if (oid == null) { save(element); } } } else if (fieldContent
     * instanceof NakedValue) { // ignore } else { LOG.debug("adding reference field " + fieldName +" " +
     * fieldContent); if(fieldContent != null) { Object oid = ((NakedObject) fieldContent).getOid();
     * if (oid == null) { save((NakedObject) fieldContent); } } } } }
     */

    public void setLoadedObjects(LoadedObjects loaded) {
        this.loaded = loaded;
    }
    
    /**
	 * Expose as a .NET property
	 * @property
	 */
    public void set_LoadedObjects(LoadedObjects loaded) {
        this.loaded = loaded;
    }
    
    public void shutdown() throws ObjectStoreException {}

    public void startTransaction() {
        LOG.debug("start transaction");
    }

    private String debugCollectionGraph(NakedCollection collection, String name, int level,
        Vector recursiveElements) {
        StringBuffer s = new StringBuffer();
    
        //	indent(s, level - 1);
        if (recursiveElements.contains(collection)) {
            s.append("*\n");
        } else {
            //		s.append("\n");
            recursiveElements.addElement(collection);
    
            Enumeration e = ((NakedCollection) collection).elements();
    
            while (e.hasMoreElements()) {
                indent(s, level);
    
                NakedObject element = ((NakedObject) e.nextElement());
    
                s.append(element);
                s.append(debugGraph(element, name, level + 1, recursiveElements));
            }
        }
    
        return s.toString();
    }

    public String debugGraph(NakedObject object, String name, int level, Vector recursiveElements) {
        if (level > 3) {
            return "...\n"; // only go 3 levels?
        }
    
        if (recursiveElements == null) {
            recursiveElements = new Vector(25, 10);
        }
    
        if (object instanceof NakedCollection) {
            return "\n" +
            debugCollectionGraph((NakedCollection) object, name, level, recursiveElements);
        } else {
            return "\n" + debugObjectGraph(object, name, level, recursiveElements);
        }
    }

    private String debugObjectGraph(NakedObject object, String name, int level,
        Vector recursiveElements) {
        StringBuffer s = new StringBuffer();
    
        recursiveElements.addElement(object);
    
        // work through all its fields
        NakedObjectField[] fields;
        
        fields = object.getSpecification().getFields();
    
        for (int i = 0; i < fields.length; i++) {
            NakedObjectField field = fields[i];
            Object obj = object.getField(field);
    
            name = field.getName();
            indent(s, level);
    
             if (obj instanceof NakedObject) {
                if (recursiveElements.contains(obj)) {
                    s.append(name + ": " + obj + "*\n");
                } else {
                    s.append(name + ": " + obj);
                    s.append(debugGraph((NakedObject) obj, name, level + 1, recursiveElements));
                }
            } else {
                s.append(name + ": " + obj);
                s.append("\n");
            }
        }
    
        return s.toString();
    }
    

    private void indent(StringBuffer s, int level) {
        for (int indent = 0; indent < level; indent++) {
            s.append(Debug.indentString(4) + "|");
        }

        s.append(Debug.indentString(4) + "+--");
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

