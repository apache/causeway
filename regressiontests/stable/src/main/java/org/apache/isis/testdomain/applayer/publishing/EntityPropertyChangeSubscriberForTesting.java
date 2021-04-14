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
package org.apache.isis.testdomain.applayer.publishing;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.publishing.spi.EntityPropertyChange;
import org.apache.isis.applib.services.publishing.spi.EntityPropertyChangeSubscriber;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service @Log4j2
public class EntityPropertyChangeSubscriberForTesting 
implements EntityPropertyChangeSubscriber {

    @Inject private KVStoreForTesting kvStore;
    
    @PostConstruct
    public void init() {
        log.info("about to initialize");
    }

    @Override
    public void onChanging(final EntityPropertyChange entityPropertyChange) {

        val propertyChangeEntry = String.format("%s/%s: '%s' -> '%s'", 
                entityPropertyChange.getTargetClassName(), 
                entityPropertyChange.getPropertyName(), 
                entityPropertyChange.getPreValue(), 
                entityPropertyChange.getPostValue());

        @SuppressWarnings("unchecked")
        val propertyChangeEntries = (List<String>) kvStore.get(this, "propertyChangeEntries")
            .orElseGet(ArrayList::new);
        kvStore.put(this, "propertyChangeEntries", propertyChangeEntries);
        
        propertyChangeEntries.add(propertyChangeEntry);
        
        log.debug("property changes {}", propertyChangeEntry);
    }
    
    // -- UTILITIES
    
    @SuppressWarnings("unchecked")
    public static Can<String> getPropertyChangeEntries(KVStoreForTesting kvStore) {
        return Can.ofCollection(
                (List<String>) kvStore
                .get(EntityPropertyChangeSubscriberForTesting.class, "propertyChangeEntries")
                .orElse(null));
    }
    
    public static void clearPropertyChangeEntries(KVStoreForTesting kvStore) {
        kvStore.clear(EntityPropertyChangeSubscriberForTesting.class);
    }

   

}