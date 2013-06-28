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
package org.apache.isis.objectstore.jdo.datanucleus.service.eventbus;

import java.util.Collection;

import com.google.common.eventbus.EventBus;

import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.objectstore.jdo.applib.service.support.IsisJdoSupport;
import org.apache.isis.objectstore.jdo.datanucleus.JDOStateManagerForIsis;
import org.apache.isis.objectstore.jdo.datanucleus.JDOStateManagerForIsis.Hint;

public class EventBusServiceJdo extends EventBusService {

    
    @Override
    protected EventBus getEventBus() {
        return IsisContext.getSession().getEventBus();
    }

    // //////////////////////////////////////

    @Override
    protected void ensureLoaded(final Collection<?> collection) {
        isisJdoSupport.ensureLoaded(collection);
    }

    /**
     * skip if called in any way by way of the {@link JDOStateManagerForIsis}.
     */
    @Override
    protected boolean skip(Object event) {
        return JDOStateManagerForIsis.hint.get() != Hint.NONE;
    }
    
    // //////////////////////////////////////

    private IsisJdoSupport isisJdoSupport;
    public void setIsisJdoSupport(IsisJdoSupport isisJdoSupport) {
        this.isisJdoSupport = isisJdoSupport;
    }

}

