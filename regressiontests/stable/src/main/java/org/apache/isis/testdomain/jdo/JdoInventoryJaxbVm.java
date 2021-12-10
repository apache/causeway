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
package org.apache.isis.testdomain.jdo;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.ObjectSupport;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.testdomain.jdo.entities.JdoBook;
import org.apache.isis.testdomain.jdo.entities.JdoProduct;

import lombok.Getter;
import lombok.Setter;

@XmlRootElement(name = "root")
@XmlType(
        propOrder = {"name", "favoriteBook", "books"}
)
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
        nature=Nature.VIEW_MODEL
        , logicalTypeName = "testdomain.jdo.JdoInventoryJaxbVm"
)
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
    //@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
    private JdoBook favoriteBook = null;

    @MemberSupport public List<JdoBook> choicesFavoriteBook() {
        return listBooks();
    }

    @Getter @Setter
    @Collection
    @XmlElement(name = "book")
    //@XmlJavaTypeAdapter(PersistentEntitiesAdapter.class)
    private java.util.Collection<JdoBook> books = new ArrayList<>();

}