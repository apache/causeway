package org.nakedobjects.object.persistence.defaults;

import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.persistence.CreateObjectCommand;
import org.nakedobjects.object.persistence.DestroyObjectCommand;
import org.nakedobjects.object.persistence.InstancesCriteria;
import org.nakedobjects.object.persistence.NakedObjectStore;
import org.nakedobjects.object.persistence.ObjectNotFoundException;
import org.nakedobjects.object.persistence.ObjectStoreException;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.persistence.PersistenceCommand;
import org.nakedobjects.object.persistence.SaveObjectCommand;
import org.nakedobjects.object.persistence.UnsupportedFindException;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.utility.Debug;
import org.nakedobjects.utility.DebugString;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Category;


public class TransientObjectStore implements NakedObjectStore {
    private final static Category LOG = Category.getInstance(TransientObjectStore.class);
    protected Hashtable instances;

    public TransientObjectStore() {
        LOG.info("Creating object store");
        instances = new Hashtable();
    }

    public void abortTransaction() {
        LOG.debug("transaction aborted");
    }

    public CreateObjectCommand createCreateObjectCommand(final NakedObject object) {
        return new CreateObjectCommand() {
            public void execute() throws ObjectStoreException {
                LOG.debug("  create object " + object);
                save(object);
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
            public void execute() throws ObjectStoreException {
                LOG.info("  delete object '" + object + "'");
                destroy(object);
            }

            public NakedObject onObject() {
                return object;
            }

            public String toString() {
                return "DestroyObjectCommand [object=" + object + "]";
            }
        };
    }

    public SaveObjectCommand createSaveObjectCommand(final NakedObject object) {
        return new SaveObjectCommand() {
            public void execute() throws ObjectStoreException {
                save(object);
            }

            public NakedObject onObject() {
                return object;
            }

            public String toString() {
                return "SaveObjectCommand [object=" + object + "]";
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

                NakedObject element;
                try {
                    element = ((NakedObject) e.nextElement());
                } catch (ClassCastException ex) {
                    LOG.error(ex);
                    return s.toString();
                }

                s.append(element);
                s.append(debugGraph(element, name, level + 1, recursiveElements));
            }
        }

        return s.toString();
    }

    private String debugGraph(NakedObject object, String name, int level, Vector recursiveElements) {
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

            if (field.isCollection()) {
                s.append(name + ": \n" + debugCollectionGraph((NakedCollection) obj, "nnn", level + 1, recursiveElements));
            } else {
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
        }

        return s.toString();
    }

    private void destroy(NakedObject object) {
        NakedObjectSpecification specification = object.getSpecification();
        LOG.debug("   destroy object " + object + " as instance of " + specification.getShortName());
        TransientObjectStoreInstances ins = instancesFor(specification);
        ins.remove(object.getOid());
    }

    public void endTransaction() {
        LOG.debug("end transaction");
    }

    protected void finalize() throws Throwable {
        super.finalize();
        LOG.info("finalizing object store");
    }

    public String getDebugData() {
        DebugString debug = new DebugString();
        debug.appendTitle("Business Objects");
        Enumeration e = instances.keys();
        while (e.hasMoreElements()) {
            NakedObjectSpecification spec = (NakedObjectSpecification) e.nextElement();
            debug.appendln(0, spec.getFullName());
            TransientObjectStoreInstances instances = instancesFor(spec);
            Enumeration f = instances.elements();
            if (!f.hasMoreElements()) {
                debug.appendln(8, "no instances");
            }
            while (f.hasMoreElements()) {
                debug.appendln(8, f.nextElement().toString());
            }
        }
        debug.appendln();

        debug.appendTitle("Object graphs");
        Vector dump = new Vector();
        e = instances.keys();
        while (e.hasMoreElements()) {
            NakedObjectSpecification spec = (NakedObjectSpecification) e.nextElement();
            TransientObjectStoreInstances instances = instancesFor(spec);
            Enumeration f = instances.elements();
            while (f.hasMoreElements()) {
                NakedObject object = (NakedObject) f.nextElement();
                debug.append(spec.getFullName());
                debug.append(": ");
                debug.append(object);
                debug.appendln(debugGraph(object, "name???", 0, dump));
            }
        }
        return debug.toString();
    }

    public String getDebugTitle() {
        return name();
    }

    public NakedObject[] getInstances(InstancesCriteria criteria) throws ObjectStoreException, UnsupportedFindException {
        Vector instances = new Vector();
        getInstances(criteria, instances);
        return toInstancesArray(instances);
    }

    private void getInstances(InstancesCriteria criteria, Vector instances) {
        NakedObjectSpecification spec = criteria.getSpecification();
        instancesFor(spec).instances(criteria, instances);
        if (criteria.includeSubclasses()) {
            NakedObjectSpecification[] subclasses = spec.subclasses();
            for (int i = 0; i < subclasses.length; i++) {
                getInstances(subclasses[i], instances, true);
            }
        }
    }

    public NakedObject[] getInstances(NakedObjectSpecification spec, boolean includeSubclasses) {
        LOG.debug("get instances" + (includeSubclasses ? " (included subclasses)" : "") + ": " + spec.getFullName());
        Vector instances = new Vector();
        getInstances(spec, instances, includeSubclasses);
        NakedObject[] ins = toInstancesArray(instances);
        return ins;
    }

    private void getInstances(NakedObjectSpecification spec, Vector instances, boolean includeSubclasses) {
        instancesFor(spec).instances(instances);
        if (includeSubclasses) {
            NakedObjectSpecification[] subclasses = spec.subclasses();
            for (int i = 0; i < subclasses.length; i++) {
                getInstances(subclasses[i], instances, true);
            }
        }
    }

    public NakedClass getNakedClass(String name) throws ObjectNotFoundException, ObjectStoreException {
        throw new ObjectNotFoundException();
    }

    public NakedObject getObject(Oid oid, NakedObjectSpecification hint) throws ObjectNotFoundException, ObjectStoreException {
        LOG.debug("getObject " + oid);
        TransientObjectStoreInstances ins = instancesFor(hint);
        NakedObject object = ins.getObject(oid);
        if (object == null) {
            throw new ObjectNotFoundException(oid);
        } else {
            setupReferencedObjects(object);
            return object;
        }
    }

    public boolean hasInstances(NakedObjectSpecification spec, boolean includeSubclasses) {
        if (instancesFor(spec).hasInstances()) {
            return true;
        }
        if (includeSubclasses) {
            NakedObjectSpecification[] subclasses = spec.subclasses();
            for (int i = 0; i < subclasses.length; i++) {
                if (hasInstances(subclasses[i], includeSubclasses)) {
                    return true;
                }
            }
        }
        return false;
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

    public String name() {
        return "Transient Object Store";
    }

    public int numberOfInstances(NakedObjectSpecification spec, boolean includeSubclasses) {
        int numberOfInstances = instancesFor(spec).numberOfInstances();
        if (includeSubclasses) {
            NakedObjectSpecification[] subclasses = spec.subclasses();
            for (int i = 0; i < subclasses.length; i++) {
                numberOfInstances += numberOfInstances(subclasses[i], true);
            }
        }
        return numberOfInstances;
    }

    public void resolveEagerly(NakedObject object, NakedObjectField field) throws ObjectStoreException {}

    public void resolveImmediately(NakedObject object) throws ObjectStoreException {
        LOG.debug("resolve " + object);
        setupReferencedObjects(object);
    }

    public void runTransaction(PersistenceCommand[] commands) throws ObjectStoreException {
        LOG.info("start execution of transaction");
        for (int i = 0; i < commands.length; i++) {
            commands[i].execute();
        }
        LOG.info("end execution");
    }

    private void save(NakedObject object) throws ObjectStoreException {
        if (object.getObject() instanceof NakedClass) {
            throw new ObjectStoreException("Can't make changes to a NakedClass object");
        }
        NakedObjectSpecification specification = object.getSpecification();
        LOG.debug("   saving object " + object + " as instance of " + specification.getShortName());
        TransientObjectStoreInstances ins = instancesFor(specification);
        ins.save(object);
    }

    private void setupReference(NakedObject fieldContent) {
        if (fieldContent != null && fieldContent.getOid() == null) {
            Oid fieldOid = instancesFor(fieldContent.getSpecification()).getOidFor(fieldContent.getObject());
            fieldContent.setOid(fieldOid);
        }
    }

    private void setupReferencedObjects(NakedObject object) {
        NakedObjectField[] fields = object.getFields();
        for (int i = 0; i < fields.length; i++) {
            NakedObjectField field = fields[i];
            if (field.isCollection()) {
                NakedCollection col = (NakedCollection) object.getField(field);
                for (Enumeration e = col.elements(); e.hasMoreElements();) {
                    NakedObject element = (NakedObject) e.nextElement();
                    setupReference(element);
                }
            } else if (field.isObject()) {
                NakedObject fieldContent = (NakedObject) object.getField(field);
                setupReference(fieldContent);
            }
        }
    }

    public void shutdown() throws ObjectStoreException {
        for (Enumeration e = instances.elements(); e.hasMoreElements();) {
            TransientObjectStoreInstances inst = (TransientObjectStoreInstances) e.nextElement();
            inst.shutdown();
        }
        instances.clear();
        LOG.info("shutdown");
    }

    public void startTransaction() {
        LOG.debug("start transaction");
    }

    private NakedObject[] toInstancesArray(Vector instances) {
        NakedObject[] ins = new NakedObject[instances.size()];
        for (int i = 0; i < ins.length; i++) {
            NakedObject object = (NakedObject) instances.elementAt(i);
            setupReferencedObjects(object);
            ins[i] = object;
        }
        return ins;
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

