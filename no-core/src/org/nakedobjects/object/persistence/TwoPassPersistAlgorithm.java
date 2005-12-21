package org.nakedobjects.object.persistence;

import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.OneToManyAssociation;
import org.nakedobjects.object.Persistable;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.utility.Assert;
import org.nakedobjects.utility.NakedObjectRuntimeException;
import org.nakedobjects.utility.ToString;

import org.apache.log4j.Logger;


public class TwoPassPersistAlgorithm implements PersistAlgorithm {
    private static final Logger LOG = Logger.getLogger(TwoPassPersistAlgorithm.class);
    private OidGenerator oidGenerator;

    private final synchronized Oid createOid(Naked object) {
        Oid oid = oidGenerator.next(object);
        LOG.debug("createOid " + oid);
        return oid;
    }

    public void init() {
        Assert.assertNotNull("oid generator required", oidGenerator);
        oidGenerator.init();
    }

    public void makePersistent(NakedObject object, PersistedObjectAdder persistor) {
        if (object.getResolveState().isPersistent() || object.persistable() == Persistable.TRANSIENT) {
            return;
        }

        LOG.info("persist " + object);
        NakedObjects.getObjectLoader().madePersistent(object, createOid(object));

        NakedObjectField[] fields = object.getFields();
        for (int i = 0; i < fields.length; i++) {
            NakedObjectField field = fields[i];
            if (field.isDerived()) {
                continue;
            } else if (field.isValue()) {
                continue;
            } else if (field instanceof OneToManyAssociation) {
                // skip in first pass
                continue;
            } else {
                Object fieldValue = object.getField(field);
                if (fieldValue == null) {
                    continue;
                }
                if (!(fieldValue instanceof NakedObject)) {
                    throw new NakedObjectRuntimeException();
                }
                makePersistent((NakedObject) fieldValue, persistor);
            }
        }

        for (int i = 0; i < fields.length; i++) {
            NakedObjectField field = fields[i];
            if (field.isDerived()) {
                continue;
            } else if (field.isValue()) {
                continue;
            } else if (field instanceof OneToManyAssociation) {
                InternalCollection collection = (InternalCollection) object.getField(field);
                makePersistent(collection, persistor);
//                NakedObjects.getObjectLoader().madePersistent(collection, createOid(object));
                collection.changeState(ResolveState.RESOLVED);
                for (int j = 0; j < collection.size(); j++) {
                    makePersistent(collection.elementAt(j), persistor);
                }
            } else {
                // skip in second pass
                continue;
            }
        }

        persistor.createObject(object);
    }

    public void makePersistent(InternalCollection collection, PersistedObjectAdder persistor) {
        if (collection.getResolveState().isPersistent() || collection.persistable() == Persistable.TRANSIENT) {
            return;
        }
        LOG.info("persist " + collection);
        NakedObjects.getObjectLoader().madePersistent(collection, createOid(collection));
        for (int j = 0; j < collection.size(); j++) {
            makePersistent(collection.elementAt(j), persistor);
        }
    }

    public String name() {
        return "Two pass,  bottom up persistence walker";
    }

    /**
     * Expose as a .NET property
     * 
     * @property
     */
    public void set_OidGenerator(OidGenerator oidGenerator) {
        this.oidGenerator = oidGenerator;
    }

    public void setOidGenerator(OidGenerator oidGenerator) {
        this.oidGenerator = oidGenerator;
    }

    public void shutdown() {
        oidGenerator.shutdown();
        oidGenerator = null;
    }

    public String toString() {
        ToString toString = new ToString(this);
        if (oidGenerator != null) {
            toString.append("oidGenerator", oidGenerator.name());
        }
        return toString.toString();
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */