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
package org.apache.causeway.testdomain.jpa.entities;

import java.util.concurrent.atomic.LongAdder;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Transient;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.jaxb.PersistentEntityAdapter;
import org.apache.causeway.persistence.jpa.applib.integration.CausewayEntityListener;
import org.apache.causeway.testdomain.model.stereotypes.MyService;
import org.apache.causeway.testdomain.util.dto.BookDto;
import org.apache.causeway.testdomain.util.dto.IBook;
import org.apache.causeway.testdomain.util.kv.KVStoreForTesting;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Entity
@EntityListeners(CausewayEntityListener.class)
@DiscriminatorValue("Book")
@Named("testdomain.jpa.Book")
@DomainObject(
        entityChangePublishing = Publishing.ENABLED

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
@DomainObjectLayout(cssClassFa = "book")
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@ToString(callSuper = true)
@Slf4j
public class JpaBook
extends JpaProduct
implements IBook {

    @Inject @Transient private KVStoreForTesting kvStore;

    // -- ENTITY SERVICE INJECTION TEST
    @Transient private MyService myService;
    @Inject
    public void setMyService(final MyService myService) {
        var count = kvStore.incrementCounter(JpaBook.class, "injection-count");
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

    @Property(editing = Editing.ENABLED)
    @Column(nullable = false, unique = true)
    private String isbn;
    @Override
    public String getIsbn() {
        System.err.printf("[%d]getIsbn()->%s%n", this.hashCode(), isbn);
        if("ISBN-XXXX".equals(isbn)) {
            System.err.printf("%s%n", "bingo");
        }
        return isbn;
    }
    public void setIsbn(final String isbn) {
        System.err.printf("[%d]setIsbn():%s->%s%n", this.hashCode(), this.isbn, isbn);
        this.isbn = isbn;
    }

    private static final LongAdder idGen = new LongAdder();
    private int oid=-1;

    @Override
    public int hashCode() {
        synchronized(idGen) {
            if(oid==-1) {
                idGen.increment();
                oid = idGen.intValue();
            }
        }
        return oid;
    }

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
        System.err.printf("[%d]con Isbn():%s%n", this.hashCode(), isbn);
        this.isbn = isbn;
        this.publisher = publisher;
    }

}
