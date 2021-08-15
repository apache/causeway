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
package org.apache.isis.testing.archtestsupport.applib.entity.jpa.dom;

import java.util.Comparator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.jaxb.PersistentEntityAdapter;
import org.apache.isis.persistence.jpa.applib.integration.JpaEntityInjectionPointResolver;

@Entity
@Table(
        schema = "jpa",
        uniqueConstraints = {@UniqueConstraint (name = "name", columnNames = "name")}
)
@DomainObject(nature = Nature.ENTITY)
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
@EntityListeners({ JpaEntityInjectionPointResolver.class})
public abstract class JpaEntity2 implements Comparable<JpaEntity2> {

    @Id @Column(name = "id", nullable = false)
    private Long id;

    @Version
    private Long version;

    private final String name;

    // abstract classes do not need to have no-arg constructor
    public JpaEntity2(final String name) {
        this.name = name;
    }

    @Override public int compareTo(final JpaEntity2 o) {
        return Comparator.<JpaEntity2,Long>comparing(x -> x.id).compare(this,o);
    }
}
