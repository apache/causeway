package org.nakedobjects.system;

import org.nakedobjects.object.NakedObjectPersistor;
import org.nakedobjects.object.persistence.OidGenerator;
import org.nakedobjects.object.persistence.TwoPassPersistAlgorithm;
import org.nakedobjects.object.persistence.objectstore.ObjectStorePersistor;
import org.nakedobjects.persistence.sql.DatabaseConnectorFactory;
import org.nakedobjects.persistence.sql.DatabaseConnectorPool;
import org.nakedobjects.persistence.sql.ObjectMapperLookup;
import org.nakedobjects.persistence.sql.SqlObjectStore;
import org.nakedobjects.persistence.sql.SqlOidGenerator;
import org.nakedobjects.persistence.sql.auto.AutoMapperFactory;
import org.nakedobjects.persistence.sql.jdbc.JdbcConnectorFactory;

public abstract class AbstractSqlStoreSystem extends AbstractSystem {


    protected NakedObjectPersistor createPersistor() {
        ObjectMapperLookup mapperLookup = new ObjectMapperLookup();
        DatabaseConnectorFactory connectorFactory = new JdbcConnectorFactory();
        mapperLookup.setMapperFactory(new AutoMapperFactory());
        DatabaseConnectorPool connectionPool = new DatabaseConnectorPool(connectorFactory, 1);
        mapperLookup.setConnectionPool(connectionPool);
        
        SqlObjectStore objectStore = new SqlObjectStore();
        objectStore.setMapperLookup(mapperLookup);
        objectStore.setConnectionPool(connectionPool);
       
        OidGenerator oidGenerator = new SqlOidGenerator(connectionPool);            

        //DefaultPersistAlgorithm persistAlgorithm = new DefaultPersistAlgorithm();
        TwoPassPersistAlgorithm persistAlgorithm = new TwoPassPersistAlgorithm();
        //TopDownPersistAlgorithm persistAlgorithm = new TopDownPersistAlgorithm();
        persistAlgorithm.setOidGenerator(oidGenerator);
        
        ObjectStorePersistor persistor = new ObjectStorePersistor();
        persistor.setObjectStore(objectStore);
        persistor.setPersistAlgorithm(persistAlgorithm);
        persistor.setCheckObjectsForDirtyFlag(true);

        return persistor;
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