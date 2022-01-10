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
package org.apache.isis.testdomain.jpa.entities;

import javax.inject.Inject;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Transient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.events.lifecycle.ObjectCreatedEvent;
import org.apache.isis.applib.events.lifecycle.ObjectLoadedEvent;
import org.apache.isis.applib.events.lifecycle.ObjectPersistedEvent;
import org.apache.isis.applib.events.lifecycle.ObjectPersistingEvent;
import org.apache.isis.applib.events.lifecycle.ObjectRemovingEvent;
import org.apache.isis.applib.events.lifecycle.ObjectUpdatedEvent;
import org.apache.isis.applib.events.lifecycle.ObjectUpdatingEvent;
import org.apache.isis.applib.jaxb.PersistentEntityAdapter;
import org.apache.isis.persistence.jpa.applib.integration.IsisEntityListener;
import org.apache.isis.testdomain.model.stereotypes.MyService;
import org.apache.isis.testdomain.util.dto.BookDto;
import org.apache.isis.testdomain.util.dto.IBook;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Entity
@EntityListeners(IsisEntityListener.class)
@DiscriminatorValue("Book")
@DomainObject(
        logicalTypeName = "testdomain.jpa.Book"
        , entityChangePublishing = Publishing.ENABLED

        , actionDomainEvent = JpaBook.ActionDomainEvent.class
        , propertyDomainEvent = JpaBook.PropertyDomainEvent.class
        , collectionDomainEvent = JpaBook.CollectionDomainEvent.class

        , createdLifecycleEvent = JpaBook.CreatedLifecycleEvent.class
        , persistingLifecycleEvent = JpaBook.PersistingLifecycleEvent.class
        , persistedLifecycleEvent = JpaBook.PersistedLifecycleEvent.class
        , loadedLifecycleEvent = JpaBook.LoadedLifecycleEvent.class
        , updatingLifecycleEvent = JpaBook.UpdatingLifecycleEvent.class
        , updatedLifecycleEvent = JpaBook.UpdatedLifecycleEvent.class
        , removingLifecycleEvent = JpaBook.RemovingLifecycleEvent.class)
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@ToString(callSuper = true)
@Log4j2
public class JpaBook
extends JpaProduct
implements IBook {

    // -- DOMAIN EVENTS
    public static class ActionDomainEvent extends IsisModuleApplib.ActionDomainEvent<JpaBook> {};
    public static class PropertyDomainEvent extends IsisModuleApplib.PropertyDomainEvent<JpaBook, Object> {};
    public static class CollectionDomainEvent extends IsisModuleApplib.CollectionDomainEvent<JpaBook, Object> {};

    // -- LIFE CYCLE EVENTS
    public static class CreatedLifecycleEvent extends ObjectCreatedEvent<JpaBook> {};
    public static class LoadedLifecycleEvent extends ObjectLoadedEvent<JpaBook> {};
    public static class PersistingLifecycleEvent extends ObjectPersistingEvent<JpaBook> {};
    public static class PersistedLifecycleEvent extends ObjectPersistedEvent<JpaBook> {};
    public static class UpdatingLifecycleEvent extends ObjectUpdatingEvent<JpaBook> {};
    public static class UpdatedLifecycleEvent extends ObjectUpdatedEvent<JpaBook> {};
    public static class RemovingLifecycleEvent extends ObjectRemovingEvent<JpaBook> {};

    @Inject @Transient private KVStoreForTesting kvStore;

    // -- ENTITY SERVICE INJECTION TEST
    @Transient private MyService myService;
    @Inject
    public void setMyService(final MyService myService) {
        val count = kvStore.incrementCounter(JpaBook.class, "injection-count");
        log.debug("INJECTION " + count);
        this.myService = myService;
    }
    @Programmatic
    public boolean hasInjectionPointsResolved() {
        getAuthor(); // seems to have the required side-effect to actually trigger injection
        return myService != null;
    }
    // --

    public static JpaBook of(
            final String name,
            final String description,
            final double price,
            final String author,
            final String isbn,
            final String publisher) {

        return new JpaBook(name, description, price, author, isbn, publisher);
    }

    public static JpaBook fromDto(final BookDto dto) {
        return JpaBook.of(dto.getName(), dto.getDescription(), dto.getPrice(),
                dto.getAuthor(), dto.getIsbn(), dto.getPublisher());
    }

    @Override
    public String title() {
        return IBook.super.title();
    }

    @Property
    @Getter @Setter @Column(nullable = true)
    private String author;

    @Property
    @Getter @Setter @Column(nullable = false, unique = true)
    private String isbn;

    @Property
    @Getter @Setter @Column(nullable = true)
    private String publisher;

    // -- CONSTRUCTOR

    private JpaBook(
            final String name,
            final String description,
            final double price,
            final String author,
            final String isbn,
            final String publisher) {

        super(/*id*/ null, name, description, price, /*comments*/null);
        this.author = author;
        this.isbn = isbn;
        this.publisher = publisher;
    }

}
