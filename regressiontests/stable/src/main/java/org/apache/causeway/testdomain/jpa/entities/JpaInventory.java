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

import java.util.Set;

import javax.inject.Named;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.jaxb.PersistentEntityAdapter;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Named("testdomain.jpa.Inventory")
@DomainObject(
        entityChangePublishing = Publishing.ENABLED)
@NamedQuery(
        name = "JpaInventory.findAffordableProducts",
        query = "SELECT p FROM JpaInventory i, IN(i.products) p WHERE p.price <= :priceUpperBound")
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class JpaInventory {

    public static JpaInventory of(final String name, final Set<JpaProduct> products) {
        return new JpaInventory(name, products);
    }

    public JpaInventory(final String name, final Set<JpaProduct> products) {
        super();
        this.name = name;
        this.products = products;
    }

    public String title() {
        return toString();
    }

    @Id
    @GeneratedValue
    private Long id;

    @Property
    @Column(nullable = true)
    private @Getter @Setter String name;

    // 1:n relation
    @Collection
    @OneToMany(cascade = CascadeType.PERSIST) @JoinColumn(nullable = true)
    private @Getter @Setter java.util.Collection<JpaProduct> products;


}

