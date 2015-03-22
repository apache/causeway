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
package org.apache.isis.applib;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.eventbus.EventBusService;

/**
 * Convenience class for services that act as subscribers.
 */
public abstract class AbstractSubscriber {

    //region > postConstruct, preDestroy

    @Programmatic
    @PostConstruct
    public void postConstruct() {
        eventBusService.register(this);
    }

    @Programmatic
    @PreDestroy
    public void preDestroy() {
        eventBusService.unregister(this);
    }
    //endregion

    //region > injected services
    @javax.inject.Inject
    protected DomainObjectContainer container;

    @javax.inject.Inject
    protected EventBusService eventBusService;
    //endregion

}
