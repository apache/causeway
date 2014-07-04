/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.runtime.services.eventbus;

import java.util.Set;

import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.core.runtime.system.context.IsisContext;

/**
 * @deprecated - because <tt>EventBusServiceJdo</tt> is annotated as the default implementation.
 */
@Deprecated
public class EventBusServiceDefault extends EventBusService {
    
    private final Set<Object> objectsToRegister = Sets.newHashSet();
    
    @Override
    protected EventBus getEventBus() {
        return IsisContext.getSession().getEventBus();
    }
    
    
    @Override
    public void register(Object domainObject) {
        // lazily registered
        // (a) there may be no session initially
        // (b) so can be unregistered at when closed
        objectsToRegister.add(domainObject);
    }
    
    @Override
    public void unregister(Object domainObject) {
        if(IsisContext.inSession()) {
            getEventBus().unregister(domainObject);
        }
        objectsToRegister.remove(domainObject);
    }

    public void open() {
        final Set<Object> objectsToRegister = this.objectsToRegister;
        for (final Object object : objectsToRegister) {
            getEventBus().register(object);
        }
    }

    public void close() {
        final Set<Object> objectsToRegister = this.objectsToRegister;
        for (final Object object : objectsToRegister) {
            getEventBus().unregister(object);
        }
    }

}

