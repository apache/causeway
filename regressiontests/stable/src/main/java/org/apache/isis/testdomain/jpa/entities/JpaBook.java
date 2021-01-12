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

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.persistence.jpa.applib.integration.JpaEntityInjectionPointResolver;
import org.apache.isis.testdomain.model.stereotypes.MyService;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Entity
@EntityListeners(JpaEntityInjectionPointResolver.class)
@DiscriminatorValue("Book")
@DomainObject(
        objectType = "testdomain.jpa.Book",
        nature = Nature.JPA_ENTITY, //TODO[ISIS-2332] should not be required, when using JPA quick classify SPI 
        entityChangePublishing = Publishing.ENABLED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(callSuper = true)
@Log4j2
public class JpaBook extends JpaProduct {

    @Inject @Transient private KVStoreForTesting kvStore;
    
    // -- ENTITY SERVICE INJECTION TEST
    @Transient private MyService myService;
    @Inject 
    public void setMyService(MyService myService) {
        val count = kvStore.incrementCounter(JpaBook.class, "injection-count");
        log.debug("INJECTION " + count);
        this.myService = myService;
    }
    public boolean hasInjectionPointsResolved() {
        getAuthor(); // seems to have the required side-effect to actually trigger injection
        return myService != null;
    }
    // --
    
    @Override
    public String title() {
        return toString();
    }

    public static JpaBook of(
            String name, 
            String description, 
            double price, 
            String author, 
            String isbn, 
            String publisher) {

        return new JpaBook(name, description, price, author, isbn, publisher);
    }

    @Property
    @Getter @Setter @Column(nullable = true)
    private String author;

    @Property
    @Getter @Setter @Column(nullable = true)
    private String isbn;

    @Property
    @Getter @Setter @Column(nullable = true)
    private String publisher;

    // -- CONSTRUCTOR

    private JpaBook(
            String name, 
            String description, 
            double price, 
            String author, 
            String isbn, 
            String publisher) {

        super(/*id*/ null, name, description, price, /*comments*/null);
        this.author = author;
        this.isbn = isbn;
        this.publisher = publisher;
    }

}
