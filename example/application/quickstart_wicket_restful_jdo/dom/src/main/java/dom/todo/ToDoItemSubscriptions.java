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
package dom.todo;

import com.google.common.eventbus.Subscribe;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.eventbus.CollectionAddedToEvent;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.applib.services.eventbus.PropertyChangedEvent;

public class ToDoItemSubscriptions {

    //region > LOG
    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ToDoItemSubscriptions.class);
    //endregion

    @Programmatic
    @Subscribe
    public void on(ToDoItem.AbstractEvent ev) {
        LOG.info(ev.getEventDescription() + ": " + container.titleOf(ev.getToDoItem()));
    }

    @Programmatic
    @Subscribe
    public void on(PropertyChangedEvent<?,?> ev) {
        LOG.info(container.titleOf(ev.getSource()) + ", changed " + ev.getIdentifier().getMemberName() + " : " + ev.getOldValue() + " -> " + ev.getNewValue());
    }
    
    @Programmatic
    @Subscribe
    public void on(CollectionAddedToEvent<?,?> ev) {
        LOG.info(container.titleOf(ev.getSource()) + ", added to " + ev.getIdentifier().getMemberName() + " : " + ev.getValue());
    }
    
    //region > injected services
    // //////////////////////////////////////
    
    @javax.inject.Inject
    private DomainObjectContainer container;

    @SuppressWarnings("unused")
    private EventBusService eventBusService;
    public final void injectEventBusService(EventBusService eventBusService) {
        eventBusService.register(this);
    }
    //endregion

}
