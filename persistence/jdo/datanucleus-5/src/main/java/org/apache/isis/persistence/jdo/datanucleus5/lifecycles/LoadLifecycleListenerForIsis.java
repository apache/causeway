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

import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.jdo.listener.InstanceLifecycleEvent;

import org.apache.isis.core.runtime.persistence.changetracking.EntityChangeTracker;

/**
 * To be registered with each JDO PersistenceManager instance, in order to collect
 * persistence related metrics
 * 
 * @since 2.0
 *
 */
@Vetoed // managed by isis
public class LoadLifecycleListenerForIsis 
implements javax.jdo.listener.LoadLifecycleListener {
    
    @Inject private Provider<EntityChangeTracker> entityChangeTrackerProvider;

    @Override
    public void postLoad(final InstanceLifecycleEvent event) {
        entityChangeTrackerProvider.get().incrementLoaded();
    }

}
