package org.nakedobjects.system;

import org.nakedobjects.object.NakedObjectPersistor;
import org.nakedobjects.object.persistence.DefaultPersistAlgorithm;
import org.nakedobjects.object.persistence.SimpleOidGenerator;
import org.nakedobjects.object.persistence.objectstore.ObjectStorePersistor;
import org.nakedobjects.object.persistence.objectstore.inmemory.TransientObjectStore;



/**
 * Utility class to start a server, using the default configuration file: client.properties.
 */
public final class Exploration extends AbstractSystem {
    public static void main(String[] args) {
        new Exploration().init();        
    }

    protected NakedObjectPersistor createPersistor() {
        DefaultPersistAlgorithm persistAlgorithm = new DefaultPersistAlgorithm();
        persistAlgorithm.setOidGenerator(new SimpleOidGenerator());
        ObjectStorePersistor persistor = new ObjectStorePersistor();
        persistor.setObjectStore(new TransientObjectStore());
        persistor.setPersistAlgorithm(persistAlgorithm);
        persistor.setCheckObjectsForDirtyFlag(true);
        return persistor;
    }
    
    protected void setupFixtures() {
        installFixtures("nakedobjects.exploration.fixtures");
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