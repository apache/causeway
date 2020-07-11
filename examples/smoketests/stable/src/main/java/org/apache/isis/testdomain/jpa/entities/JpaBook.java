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

import javax.persistence.Column;
import javax.persistence.Entity;

import org.apache.isis.applib.annotation.Auditing;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.Publishing;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
//@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
//@Discriminator(value="Book")
@DomainObject(
        objectType = "testdomain.jpa.Book",
        publishing=Publishing.ENABLED, 
        auditing = Auditing.ENABLED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(callSuper = true)
public class JpaBook extends JpaProduct {

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
