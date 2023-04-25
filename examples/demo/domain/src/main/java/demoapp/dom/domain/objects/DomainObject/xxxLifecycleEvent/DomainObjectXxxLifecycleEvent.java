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
package demoapp.dom.domain.objects.DomainObject.xxxLifecycleEvent;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.events.lifecycle.ObjectCreatedEvent;
import org.apache.causeway.applib.events.lifecycle.ObjectLoadedEvent;
import org.apache.causeway.applib.events.lifecycle.ObjectPersistedEvent;
import org.apache.causeway.applib.events.lifecycle.ObjectPersistingEvent;
import org.apache.causeway.applib.events.lifecycle.ObjectRemovingEvent;
import org.apache.causeway.applib.events.lifecycle.ObjectUpdatedEvent;
import org.apache.causeway.applib.events.lifecycle.ObjectUpdatingEvent;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom._infra.values.ValueHolder;
import demoapp.dom.domain.objects.DomainObject.xxxLifecycleEvent.jpa.DomainObjectXxxLifecycleEventJpa;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
//tag::class[]
@DomainObject(
        nature = Nature.ENTITY,
        createdLifecycleEvent = DomainObjectXxxLifecycleEventJpa.CreatedEvent.class,        // <.>
        persistingLifecycleEvent = DomainObjectXxxLifecycleEventJpa.PersistingEvent.class,  // <.>
        persistedLifecycleEvent = DomainObjectXxxLifecycleEventJpa.PersistedEvent.class,    // <.>
        updatingLifecycleEvent = DomainObjectXxxLifecycleEventJpa.UpdatingEvent.class,      // <.>
        updatedLifecycleEvent = DomainObjectXxxLifecycleEventJpa.UpdatedEvent.class,        // <.>
        removingLifecycleEvent = DomainObjectXxxLifecycleEventJpa.RemovingEvent.class,      // <.>
        loadedLifecycleEvent = DomainObjectXxxLifecycleEventJpa.LoadedEvent.class           // <.>
)
public abstract class DomainObjectXxxLifecycleEvent                                         // <.>
//end::class[]
        implements
        HasAsciiDocDescription,
        ValueHolder<String>
//tag::class[]
{
    public interface LifecycleEvent {                                                       // <.>
        DomainObjectXxxLifecycleEvent getSource();
    }

    public static class CreatedEvent
            extends ObjectCreatedEvent<DomainObjectXxxLifecycleEvent>                       // <1>
            implements LifecycleEvent { }                                                   // <9>
    public static class PersistingEvent
            extends ObjectPersistingEvent<DomainObjectXxxLifecycleEvent>                    // <2>
            implements LifecycleEvent { }                                                   // <9>
    public static class PersistedEvent
            extends ObjectPersistedEvent<DomainObjectXxxLifecycleEvent>                     // <3>
            implements LifecycleEvent { }                                                   // <9>
    public static class UpdatingEvent
            extends ObjectUpdatingEvent<DomainObjectXxxLifecycleEvent>                      // <4>
            implements LifecycleEvent { }                                                   // <9>
    public static class UpdatedEvent
            extends ObjectUpdatedEvent<DomainObjectXxxLifecycleEvent>                       // <5>
            implements LifecycleEvent { }                                                   // <9>
    public static class RemovingEvent
            extends ObjectRemovingEvent<DomainObjectXxxLifecycleEvent>                      // <6>
            implements LifecycleEvent { }                                                   // <9>
    public static class LoadedEvent
            extends ObjectLoadedEvent<DomainObjectXxxLifecycleEvent>                        // <7>
            implements LifecycleEvent { }                                                   // <9>

    // ...
//end::class[]

    @ObjectSupport
    public String title() {
        return getName();
    }

    @Override
    public String value() {
        return getName();
    }

    public abstract String getName();
    public abstract void setName(String value);

//tag::class[]
}
//end::class[]
