package org.nakedobjects.object.persistence.defaults;

import org.nakedobjects.object.InstancesCriteria;
import org.nakedobjects.object.LoadedObjects;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.persistence.CreateObjectCommand;
import org.nakedobjects.object.persistence.DestroyObjectCommand;
import org.nakedobjects.object.persistence.NakedObjectStore;
import org.nakedobjects.object.persistence.ObjectNotFoundException;
import org.nakedobjects.object.persistence.ObjectStoreException;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.persistence.PersistenceCommand;
import org.nakedobjects.object.persistence.SaveObjectCommand;
import org.nakedobjects.object.persistence.UnsupportedFindException;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.utility.Debug;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Category;


public class TransientObjectStore implements NakedObjectStore {
    private final static Category LOG = Category.getInstance(TransientObjectStore.class);
    private final Hashtable classes;
    private final Hashtable instances;
    private LoadedObjects loaded;

    public TransientObjectStore() {
        LOG.info("new object store");
        instances = new Hashtable();
        classes = new Hashtable(30);
    }

    public void abortTransaction() {
        LOG.debug("transaction aborted");
    }

    public CreateObjectCommand createCreateObjectCommand(final NakedObject object) {
        return new CreateObjectCommand() {
            public void execute() throws ObjectStoreException {
                LOG.debug("  createObject " + object);
                save(object);
            }
        };
    }

    public DestroyObjectCommand createDestroyObjectCommand(final NakedObject object) {
        return new DestroyObjectCommand() {
            public void execute() throws ObjectStoreException {
                LOG.info("  delete requested on '" + object + "'");
                 TransientObjectStoreInstances ins = instancesFor(object.getSpecification());
                ins.remove(object.getOid());
            }
        };
    }

    public SaveObjectCommand createSaveObjectCommand(final NakedObject object) {
        return new SaveObjectCommand() {
            public void execute() throws ObjectStoreException {
                save(object);
            }
        };
    }

    private String debugCollectionGraph(NakedCollection collection, String name, int level, Vector recursiveElements) {
        StringBuffer s = new StringBuffer();

        if (recursiveElements.contains(collection)) {
            s.append("*\n");
        } else {
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
            return "\n" + debugCollectionGraph((NakedCollection) object, name, level, recursiveElements);
        } else {
            return "\n" + debugObjectGraph(object, name, level, recursiveElements);
        }
    }

    private String debugObjectGraph(NakedObject object, String name, int level, Vector recursiveElements) {
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

    private Enumeration elements(NakedObjectSpecification spec) {
        TransientObjectStoreInstances ins = instancesFor(spec);
        return ins.elements();
    }

    public void endTransaction() {
        LOG.debug("end transaction");
    }


    protected void finalize() throws Throwable {
        super.finalize();
        LOG.info("finalizing object store");
    }

    public String getDebugData() {
        StringBuffer data = new StringBuffer();
        data.append("Objects\n");
        data.append("----------\n");
        Enumeration e = instances.keys();
        while (e.hasMoreElements()) {
            NakedObjectSpecification spec = (NakedObjectSpecification) e.nextElement();
            TransientObjectStoreInstances instances = instancesFor(spec);
            Enumeration f = instances.elements();
            while (f.hasMoreElements()) {
                data.append(f.nextElement());
                data.append('\n');
            }            
        }
        
        data.append("\n\nObjects\n");
        data.append("----------\n");
        Vector dump = new Vector();
        e = instances.keys();
        while (e.hasMoreElements()) {
            NakedObjectSpecification spec = (NakedObjectSpecification) e.nextElement();
            TransientObjectStoreInstances instances = instancesFor(spec);
            Enumeration f = instances.elements();
            while (f.hasMoreElements()) {
                NakedObject object = (NakedObject) f.nextElement();
                data.append(object);
                data.append(debugGraph(object, "name???", 0, dump));
                data.append('\n');
            }
        }
        return data.toString();
    }

    public String getDebugTitle() {
        return name();
    }

    public NakedObject[] getInstances(InstancesCriteria criteria, boolean includeSubclasses) throws ObjectStoreException,
            UnsupportedFindException {
        if (criteria == null) {
            throw new NullPointerException();
        }

        Vector instances = new Vector();
        Enumeration objects = elements(criteria.getSpecification());

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
        if (pattern == null) {
            throw new NullPointerException();
        }

        Vector instances = new Vector();
        if (pattern instanceof NakedClass) {
            Enumeration objects = elements(pattern.getSpecification());

            String name = ((NakedClass) pattern).getName();

            while (objects.hasMoreElements()) {
                NakedObject object = (NakedObject) objects.nextElement();

                if (object.getObject() instanceof NakedClass && ((NakedClass) object.getObject()).getName().equals(name)) {
                    instances.addElement(object);
                }
            }
        } else {
            NakedObjectSpecification requiredType = pattern.getSpecification();
            Enumeration objects = elements(pattern.getSpecification());

            while (objects.hasMoreElements()) {
                NakedObject object = (NakedObject) objects.nextElement();

                if (requiredType.equals(object.getSpecification()) && matchesPattern(pattern, object)) {
                    instances.addElement(object);
                }
            }
        }
        LOG.debug("getInstances like " + pattern + " => " + instances);
        return toInstancesArray(instances);
    }

    public NakedObject[] getInstances(NakedObjectSpecification spec, boolean includeSubclasses) {
        LOG.debug("get instances" + (includeSubclasses ? " (included subclasses)" : "") + ": " + spec.getFullName());
        return instancesFor(spec).instances();
    }

    public NakedObject[] getInstances(NakedObjectSpecification spec, String pattern, boolean includeSubclasses)
            throws ObjectStoreException, UnsupportedFindException {
        
        String p = pattern.toLowerCase();
        NakedObject match = instancesFor(spec).instanceMatching(p);
        if(match == null) {
            Vector instances = new Vector();
            Enumeration e = instancesFor(spec).elements();
            while (e.hasMoreElements()) {
                NakedObject element = (NakedObject) e.nextElement();
                String elementTitle = element.titleString().toLowerCase();
                if (elementTitle == p || elementTitle.indexOf(p) >= 0) {
                    instances.addElement(element);
                } 
            }
            return toInstancesArray(instances);     
        } else {
            return new NakedObject[] {match};
        }
    }

    public LoadedObjects getLoadedObjects() {
        return loaded;
    }

    public NakedClass getNakedClass(String name) throws ObjectNotFoundException, ObjectStoreException {
        LOG.debug("getNakedClass " + name);
        NakedObject object = (NakedObject) classes.get(name);
        //NakedObject object = intancesfor().getNakedClass(); // (NakedObject) classes.get(name);
        if (object == null) {
            throw new ObjectNotFoundException();
        } else {
            NakedClass nc = (NakedClass) object.getObject();
            return nc;
        }
    }

    public NakedObject getObject(Oid oid, NakedObjectSpecification hint) throws ObjectNotFoundException, ObjectStoreException {
        LOG.debug("getObject " + oid);
        TransientObjectStoreInstances ins = instancesFor(hint);
        return ins.getObject(oid);
    }

    public boolean hasInstances(NakedObjectSpecification spec, boolean includeSubclasses) {
        return instancesFor(spec).hasInstances();
    }

    private void indent(StringBuffer s, int level) {
        for (int indent = 0; indent < level; indent++) {
            s.append(Debug.indentString(4) + "|");
        }

        s.append(Debug.indentString(4) + "+--");
    }

    public void init() throws ObjectStoreException {
        LOG.info("init");
    }

    private TransientObjectStoreInstances instancesFor(NakedObjectSpecification spec) {
        if (instances.containsKey(spec)) {
            return (TransientObjectStoreInstances) instances.get(spec);
        } else {
            TransientObjectStoreInstances ins = new TransientObjectStoreInstances();
            instances.put(spec, ins);
            return ins;
        }
    }

    private boolean matchesPattern(NakedObject pattern, NakedObject instance) {
        NakedObject object = instance;
        NakedObjectSpecification nc = object.getSpecification();
        NakedObjectField[] fields = nc.getFields();

        for (int f = 0; f < fields.length; f++) {
            NakedObjectField fld = fields[f];

            // are ignoring internal collections - these probably should be
            // considered
            // ignore derived fields - there is no way to set up these fields
            if (fld.isDerived()) {
                continue;
            }

            if (fld.isValue()) {
                // find the objects
                NakedObject reqd = (NakedObject) pattern.getField(fld);
                NakedObject search = (NakedObject) object.getField(fld);

                // if pattern contains empty value then it matches anything
                if (reqd.isEmpty(fld)) {
                    continue;
                }

                // compare the titles
                String r = reqd.titleString().toString().toLowerCase();
                String s = search.titleString().toString().toLowerCase();

                // if the pattern occurs in the object
                if (s.indexOf(r) == -1) {
                    return false;
                }
            } else {
                // find the objects
                NakedObject reqd = (NakedObject) pattern.getField(fld);
                NakedObject search = (NakedObject) object.getField(fld);

                // if pattern contains null reference then it matches anything
                if (reqd == null) {
                    continue;
                }

                // otherwise there must be a reference, else they can never
                // match
                if (search == null) {
                    return false;
                }

                if (reqd != search) {
                    return false;
                }
            }
        }

        return true;
    }

    public String name() {
        return "Transient Object Store";
    }

    public int numberOfInstances(NakedObjectSpecification spec, boolean includedSubclasses) {
        return instancesFor(spec).numberOfInstances();
    }

    public void resolveImmediately(NakedObject object) throws ObjectStoreException {
        LOG.debug("resolve " + object);
        NakedObject o = getObject(object.getOid(), null);
        object.copyObject(o);
    }

    public void resolveEagerly(NakedObject object, NakedObjectField field) throws ObjectStoreException {}

    public void runTransaction(PersistenceCommand[] commands) throws ObjectStoreException {
        LOG.info("start execution of transaction");
        for (int i = 0; i < commands.length; i++) {
            commands[i].execute();
        }
        LOG.info("end execution");
    }

    private void save(NakedObject object) throws ObjectStoreException {
        LOG.debug("   saving object " + object);
        if (object.getObject() instanceof NakedClass) {
            throw new ObjectStoreException("Can't make changes to a NakedClass object");
        }
        TransientObjectStoreInstances ins = instancesFor(object.getSpecification());
        ins.save(object);
    }

    /**
     * Expose as a .NET property
     * 
     * @property
     */
    public void set_LoadedObjects(LoadedObjects loaded) {
        this.loaded = loaded;
    }

    public void setLoadedObjects(LoadedObjects loaded) {
        this.loaded = loaded;
    }

    public void shutdown() throws ObjectStoreException {
        loaded = null;
        instances.clear();
        LOG.info("shutdown");
    }

    public void startTransaction() {
        LOG.debug("start transaction");
    }

    private NakedObject[] toInstancesArray(Vector instances) {
        NakedObject[] array = new NakedObject[instances.size()];
        instances.copyInto(array);
        return array;
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

