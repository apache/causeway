/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.runtime.system.persistence;

import java.util.Map;
import java.util.function.Function;

import org.datanucleus.PersistenceNucleusContext;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.store.StoreManager;
import org.datanucleus.store.federation.FederatedStoreManager;
import org.datanucleus.store.schema.SchemaAwareStoreManager;

/**
 * Given only config properties, tries to find, which kind of store manager
 * Datanucleus is going to utilize. We do this prior to bootstrapping Datanucleus, 
 * to allow for programmatic override of given config properties. 
 * (eg. proper schema creation setup)  
 * 
 * @since 2.0.0-M2
 */
enum DNStoreManagerType {

    SchemaAware,
    Federated, // [ahuber] not used by now
    Other
    ;

    public static DNStoreManagerType typeOf(Map<String,String> datanucleusProps) {

        if(hasSecondaryDataStore(datanucleusProps)) {
            return Federated; 
        } 
        
        if(isKnownSchemaAwareStoreManagerIfNotFederated(datanucleusProps)) {
            return SchemaAware;
        }
        
        return probe(datanucleusProps, storeManager->{
            
            if(storeManager instanceof SchemaAwareStoreManager) {
                return SchemaAware;
            }
            
            if(storeManager instanceof FederatedStoreManager) {
                return Federated;
            }
            
            return Other;
            
        });

    }
    
    public boolean isSchemaAware() {
        return this == SchemaAware;
    }

    // -- HELPER
    
    /* not necessarily complete, just for speed up */
    private final static String[] knownSchemaAwareIfNotFederated = {
            "jdbc:hsqldb:",
            "jdbc:sqlserver:",
            "jdbc:h2:",
            "jdbc:mysql:",
            "jdbc:mariadb:",
            "jdbc:postgresql:",
            "jdbc:db2:",
            };
    
    private static boolean hasSecondaryDataStore(Map<String,String> datanucleusProps) {
        final boolean hasSecondaryDataStore = datanucleusProps.keySet().stream()
            .anyMatch(key->key.startsWith("datanucleus.datastore."));
        return hasSecondaryDataStore;
    }

    private static boolean isKnownSchemaAwareStoreManagerIfNotFederated(Map<String,String> datanucleusProps) {

        // this saves some time, but also avoids the (still undiagnosed) issue that instantiating the
        // PMF can cause the ClassMetadata for the entity classes to be loaded in and cached prior to
        // registering the CreateSchemaObjectFromClassData (to invoke 'create schema' first)
        final String connectionUrl = datanucleusProps.get("javax.jdo.option.ConnectionURL");
        if(connectionUrl != null) {
            for(String magic : knownSchemaAwareIfNotFederated) {
                if (connectionUrl.startsWith(magic)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private static DNStoreManagerType probe(
            Map<String,String> datanucleusProps, 
            Function<StoreManager, DNStoreManagerType> categorizer) {
        
        // we create a throw-away instance of PMF so that we can probe whether DN has
        // been configured with a schema-aware store manager or not.
        final JDOPersistenceManagerFactory probePmf = (JDOPersistenceManagerFactory) 
                DataNucleusApplicationComponents5.newPersistenceManagerFactory(datanucleusProps);

        try {
            final PersistenceNucleusContext nucleusContext = probePmf.getNucleusContext();
            final StoreManager storeManager = nucleusContext.getStoreManager();
            
            return categorizer.apply(storeManager);
        } finally {
            probePmf.close();
        }
    }

}
