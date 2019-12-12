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
package org.apache.isis.persistence.jdo.datanucleus5.lifecycles;

import javax.inject.Inject;
import javax.jdo.listener.InstanceLifecycleEvent;

import org.apache.isis.persistence.jdo.datanucleus5.metamodel.JdoMetamodelUtil;
import org.apache.isis.runtime.persistence.session.events.PersistenceEventService;
import org.apache.isis.runtime.persistence.session.events.PostStoreEvent;
import org.apache.isis.runtime.persistence.session.events.PreStoreEvent;

import lombok.val;

/**
 * To be registered with each JDO PersistenceManager instance, in order to publish
 * persistence related events on the framework's event bus.
 * 
 * @since 2.0
 *
 */
public class JdoStoreLifecycleListenerForIsis implements
javax.jdo.listener.StoreLifecycleListener {
    
    @Inject private PersistenceEventService persistenceEventService;

    @Override
    public void preStore(InstanceLifecycleEvent instanceEvent) {

        val persistableObject = instanceEvent.getPersistentInstance();

        if(persistableObject!=null && 
                JdoMetamodelUtil.isPersistenceEnhanced(persistableObject.getClass())) {

            val event = PreStoreEvent.of(persistableObject);
            persistenceEventService.firePreStoreEvent(event);
        }
        
    }

    @Override
    public void postStore(InstanceLifecycleEvent instanceEvent) {

        val persistableObject = instanceEvent.getPersistentInstance();

        if(persistableObject!=null && 
                JdoMetamodelUtil.isPersistenceEnhanced(persistableObject.getClass())) {

            val event = PostStoreEvent.of(persistableObject);
            persistenceEventService.firePostStoreEvent(event);
        }
        
    }
    

}
