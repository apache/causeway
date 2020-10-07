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
package org.apache.isis.testdomain.publishing;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.iactn.Interaction.Execution;
import org.apache.isis.applib.services.publish.PublishedObjects;
import org.apache.isis.applib.services.publish.PublisherService;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;

import lombok.extern.log4j.Log4j2;

@Service @Log4j2
public class PublisherServiceForTesting implements PublisherService {

    @Inject private KVStoreForTesting kvStore;
    
    @PostConstruct
    public void init() {
        log.info("about to initialize");
    }

    @Override
    public void publish(Execution<?, ?> execution) {
        _Probe.errOut("PUBLISH EXECUTION %s", execution);
        kvStore.put(this, "execution", 999);
    }

    @Override
    public void publish(PublishedObjects publishedObjects) {
        
        _Probe.errOut("PUBLISH OBJECTS %s", publishedObjects);
        
        kvStore.put(this, "uuid", publishedObjects.getUniqueId().toString());
        kvStore.put(this, "user", publishedObjects.getUsername());
        
        kvStore.put(this, "created", publishedObjects.getNumberCreated());
        kvStore.put(this, "deleted", publishedObjects.getNumberDeleted());
        kvStore.put(this, "loaded", publishedObjects.getNumberLoaded());
        kvStore.put(this, "updated", publishedObjects.getNumberUpdated());
        kvStore.put(this, "modified", publishedObjects.getNumberPropertiesModified());
        
        kvStore.put(this, "dto", publishedObjects.getDto());
        
    }

}