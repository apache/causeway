package org.nakedobjects.object;

import org.nakedobjects.object.collection.TypedCollection;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public abstract class NakedObjectStoreTestCase extends NakedObjectTestCase {
    private static final Logger LOG = Logger.getLogger(NakedObjectStoreTestCase.class);
    protected static NakedObjectStore objectStore;
    private int next;
    protected MockObjectManager manager;

    public NakedObjectStoreTestCase(String name) {
        super(name);
    }

    protected abstract NakedObjectStore installObjectStore() throws Exception;

    protected SimpleOid nextOid() {
        return new SimpleOid(next++);
    }

    protected void restartObjectStore() throws Exception {
        shutdownObjectStore();
        setupObjectStore();
    }

    protected void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);     
        LOG.debug("test setup");

        manager = MockObjectManager.setup();
        manager.setupAddClass(TypedCollection.class);
        
        setupObjectStore();
        
        initialiseObjects();
        LOG.debug("test starting");
    }

    protected void initialiseObjects() throws Exception {}

    private void setupObjectStore() throws Exception {
        objectStore = installObjectStore();
        objectStore.init();
        LOG.debug("Object store started");
    }

    protected void tearDown() throws Exception {
        LOG.debug("test finished");
        manager.shutdown();
        try {
            shutdownObjectStore();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.tearDown();
    }

    private void shutdownObjectStore() throws Exception {
        objectStore.shutdown();
        LOG.debug("Object store shutdown complete");
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2003 Naked Objects Group
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