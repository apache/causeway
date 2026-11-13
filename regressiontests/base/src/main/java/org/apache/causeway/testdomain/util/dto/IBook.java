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
package org.apache.causeway.testdomain.util.dto;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.events.lifecycle.ObjectCreatedEvent;
import org.apache.causeway.applib.events.lifecycle.ObjectLoadedEvent;
import org.apache.causeway.applib.events.lifecycle.ObjectPersistedEvent;
import org.apache.causeway.applib.events.lifecycle.ObjectPersistingEvent;
import org.apache.causeway.applib.events.lifecycle.ObjectRemovingEvent;
import org.apache.causeway.applib.events.lifecycle.ObjectUpdatedEvent;
import org.apache.causeway.applib.events.lifecycle.ObjectUpdatingEvent;

public interface IBook {
    
    // -- DOMAIN EVENTS
    public static class ActionDomainEvent extends CausewayModuleApplib.ActionDomainEvent<IBook> {};
    public static class PropertyDomainEvent extends CausewayModuleApplib.PropertyDomainEvent<IBook, Object> {};
    public static class CollectionDomainEvent extends CausewayModuleApplib.CollectionDomainEvent<IBook, Object> {};

    // -- LIFE CYCLE EVENTS
    public static class CreatedLifecycleEvent extends ObjectCreatedEvent<IBook> {};
    public static class LoadedLifecycleEvent extends ObjectLoadedEvent<IBook> {};
    public static class PersistingLifecycleEvent extends ObjectPersistingEvent<IBook> {};
    public static class PersistedLifecycleEvent extends ObjectPersistedEvent<IBook> {};
    public static class UpdatingLifecycleEvent extends ObjectUpdatingEvent<IBook> {};
    public static class UpdatedLifecycleEvent extends ObjectUpdatedEvent<IBook> {};
    public static class RemovingLifecycleEvent extends ObjectRemovingEvent<IBook> {};

    default String title() {
        return String.format("%s [%s]", getName(), getIsbn());
    }

    String getAuthor();
    String getDescription();
    String getIsbn();
    String getName();
    double getPrice();
    String getPublisher();

}
