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
package org.apache.isis.testdomain.jdo.entities;

import javax.inject.Inject;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Query;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.DomainObject;
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

@PersistenceCapable
//@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
@Discriminator(value="Book")
@DomainObject(
        logicalTypeName = "testdomain.jdo.Book"
        , entityChangePublishing = Publishing.ENABLED

        , actionDomainEvent = JdoBook.ActionDomainEvent.class
        , propertyDomainEvent = JdoBook.PropertyDomainEvent.class
        , collectionDomainEvent = JdoBook.CollectionDomainEvent.class

        , createdLifecycleEvent = JdoBook.CreatedLifecycleEvent.class
        , persistingLifecycleEvent = JdoBook.PersistingLifecycleEvent.class
        , persistedLifecycleEvent = JdoBook.PersistedLifecycleEvent.class
        , loadedLifecycleEvent = JdoBook.LoadedLifecycleEvent.class
        , updatingLifecycleEvent = JdoBook.UpdatingLifecycleEvent.class
        , updatedLifecycleEvent = JdoBook.UpdatedLifecycleEvent.class
        , removingLifecycleEvent = JdoBook.RemovingLifecycleEvent.class)
@javax.jdo.annotations.Uniques({
    @javax.jdo.annotations.Unique(
            name = "JdoBook_isbn_UNQ", members = { "isbn" })
})
//@NamedQuery(
//name = "JdoInventory.findAffordableProducts",
//query = "SELECT p FROM JdoInventory i, IN(i.products) p WHERE p.price <= :priceUpperBound")
@Query(
      name = "findAffordableBooks",
      language = "JDOQL",
      value = "SELECT "
              + "FROM org.apache.isis.testdomain.jdo.entities.JdoBook "
              + "WHERE price <= :priceUpperBound")

@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@ToString(callSuper = true)
@Log4j2
public class JdoBook
extends JdoProduct
implements IBook {

    // -- DOMAIN EVENTS
    public static class ActionDomainEvent extends IsisModuleApplib.ActionDomainEvent<JdoBook> {};
    public static class PropertyDomainEvent extends IsisModuleApplib.PropertyDomainEvent<JdoBook, Object> {};
    public static class CollectionDomainEvent extends IsisModuleApplib.CollectionDomainEvent<JdoBook, Object> {};

    // -- LIFE CYCLE EVENTS
    public static class CreatedLifecycleEvent extends ObjectCreatedEvent<JdoBook> {};
    public static class LoadedLifecycleEvent extends ObjectLoadedEvent<JdoBook> {};
    public static class PersistingLifecycleEvent extends ObjectPersistingEvent<JdoBook> {};
    public static class PersistedLifecycleEvent extends ObjectPersistedEvent<JdoBook> {};
    public static class UpdatingLifecycleEvent extends ObjectUpdatingEvent<JdoBook> {};
    public static class UpdatedLifecycleEvent extends ObjectUpdatedEvent<JdoBook> {};
    public static class RemovingLifecycleEvent extends ObjectRemovingEvent<JdoBook> {};

    @Inject private KVStoreForTesting kvStore;

    // -- ENTITY SERVICE INJECTION TEST
    private MyService myService;
    @Inject
    public void setMyService(final MyService myService) {
        val count = kvStore.incrementCounter(JdoBook.class, "injection-count");
        log.debug("INJECTION " + count);
        this.myService = myService;
    }
    public boolean hasInjectionPointsResolved() {
        getAuthor(); // seems to have the required side-effect to actually trigger injection
        return myService != null;
    }
    // --

    public static JdoBook of(
            final String name,
            final String description,
            final double price,
            final String author,
            final String isbn,
            final String publisher) {

        return new JdoBook(name, description, price, author, isbn, publisher);
    }

    public static JdoBook fromDto(final BookDto dto) {
       return JdoBook.of(dto.getName(), dto.getDescription(), dto.getPrice(),
               dto.getAuthor(), dto.getIsbn(), dto.getPublisher());
    }

    @Override
    public String title() {
        return IBook.super.title();
    }

    @Property
    @Getter @Setter @Column(allowsNull = "true")
    private String author;

    @Property
    @Getter @Setter @Column(allowsNull = "true")
    private String isbn;

    @Property
    @Getter @Setter @Column(allowsNull = "true")
    private String publisher;

    // -- CONSTRUCTOR

    private JdoBook(
            final String name,
            final String description,
            final double price,
            final String author,
            final String isbn,
            final String publisher) {

        super(name, description, price, /*comments*/null);
        this.author = author;
        this.isbn = isbn;
        this.publisher = publisher;
    }

}
