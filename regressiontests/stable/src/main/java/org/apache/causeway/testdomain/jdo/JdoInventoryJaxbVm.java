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
package org.apache.causeway.testdomain.jdo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.testdomain.jdo.entities.JdoBook;
import org.apache.causeway.testdomain.jdo.entities.JdoInventory;
import org.apache.causeway.testdomain.jdo.entities.JdoProduct;
import org.apache.causeway.testdomain.util.dto.IBook;

import lombok.Getter;
import lombok.Setter;

@XmlRootElement(name = "root")
@XmlType(
        propOrder = {
                "name", "favoriteBook", "bookForTab1", "bookForTab2", "inventory",
                "books", "booksForTab1", "booksForTab2"}
)
@XmlAccessorType(XmlAccessType.FIELD)
@Named("testdomain.jdo.JdoInventoryJaxbVm")
@DomainObject(
        nature=Nature.VIEW_MODEL,
        aliased={
            "testdomain.jdo.JdoInventoryJaxbVmAlias"
        })
public class JdoInventoryJaxbVm {

    @XmlTransient @Inject
    private RepositoryService repository;

    @ObjectSupport public String title() {
        return String.format("%s; %s; %d products",
                this.getClass().getSimpleName(), getName(), listProducts().size());
    }

    @Property(editing = Editing.ENABLED)
    @Getter @Setter
    @XmlElement
    private String name;

    @Action
    public List<JdoProduct> listProducts() {
        return repository.allInstances(JdoProduct.class);
    }

    @Action
    public List<JdoBook> listBooks() {
        return repository.allInstances(JdoBook.class);
    }

    @Getter @Setter
    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)
    @XmlElement(required = false)
    private JdoBook favoriteBook = null;

    @MemberSupport public List<JdoBook> choicesFavoriteBook() {
        return listBooks();
    }

    @Getter @Setter
    @Collection
    @XmlElement(name = "book")
    private java.util.Collection<JdoBook> books = new ArrayList<>();

    @Getter @Setter
    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)
    @XmlElement(required = false)
    private JdoInventory inventory;

    @MemberSupport public List<JdoInventory> choicesInventory() {
        return repository.allInstances(JdoInventory.class);
    }

    // -- TAB TEST - TAB 1

    @Getter @Setter
    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)
    @XmlElement(required = false)
    private JdoBook bookForTab1 = null;

    @MemberSupport public List<JdoBook> choicesBookForTab1() {
        return listBooks();
    }

    @Property
    @XmlTransient
    public String getBookNameForTab1() {
        return Optional.ofNullable(getBookForTab1())
                .map(IBook::getName)
                .orElse("none selected");
    }

    @Getter @Setter
    @Collection
    @XmlElement(name = "book1")
    private java.util.Collection<JdoBook> booksForTab1 = new ArrayList<>();

    @Collection
    @XmlTransient
    public java.util.Collection<JdoProduct> getProductsForTab1() {
        return Optional.ofNullable(inventory)
                .map(JdoInventory::getProducts)
                .orElseGet(Collections::emptySet);
    }

    // -- TAB TEST - TAB 2

    @Getter @Setter
    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)
    @XmlElement(required = false)
    private JdoBook bookForTab2 = null;

    @MemberSupport public List<JdoBook> choicesBookForTab2() {
        return listBooks();
    }

    @Property
    @XmlTransient
    public String getBookNameForTab2() {
        return Optional.ofNullable(getBookForTab2())
                .map(IBook::getName)
                .orElse("none selected");
    }

    @Getter @Setter
    @Collection
    @XmlElement(name = "book2")
    private java.util.Collection<JdoBook> booksForTab2 = new ArrayList<>();

    @Collection
    @XmlTransient
    public java.util.Collection<JdoProduct> getProductsForTab2() {
        return Optional.ofNullable(inventory)
                .map(JdoInventory::getProducts)
                .orElseGet(Collections::emptySet);
    }

}