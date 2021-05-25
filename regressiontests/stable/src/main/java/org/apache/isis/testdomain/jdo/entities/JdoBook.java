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

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.testdomain.model.stereotypes.MyService;
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
        logicalTypeName = "testdomain.jdo.Book",
        entityChangePublishing = Publishing.ENABLED)
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


@NoArgsConstructor(access = AccessLevel.PUBLIC)
@ToString(callSuper = true)
@Log4j2
public class JdoBook extends JdoProduct {

    @Inject private KVStoreForTesting kvStore;

    // -- ENTITY SERVICE INJECTION TEST
    private MyService myService;
    @Inject
    public void setMyService(MyService myService) {
        val count = kvStore.incrementCounter(JdoBook.class, "injection-count");
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

    public static JdoBook of(
            String name,
            String description,
            double price,
            String author,
            String isbn,
            String publisher) {

        return new JdoBook(name, description, price, author, isbn, publisher);
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
            String name,
            String description,
            double price,
            String author,
            String isbn,
            String publisher) {

        super(name, description, price, /*comments*/null);
        this.author = author;
        this.isbn = isbn;
        this.publisher = publisher;
    }
}
