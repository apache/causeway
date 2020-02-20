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

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.apache.isis.applib.annotation.Auditing;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.Publishing;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@PersistenceCapable
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
@Discriminator(value="Book")
@DomainObject(
        objectType = "testdomain.jdo.Book",
        publishing=Publishing.ENABLED, 
        auditing = Auditing.ENABLED)
@ToString(callSuper = true)
public class JdoBook extends JdoProduct {

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
