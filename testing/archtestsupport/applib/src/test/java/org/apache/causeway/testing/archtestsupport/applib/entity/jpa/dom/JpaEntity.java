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
package org.apache.causeway.testing.archtestsupport.applib.entity.jpa.dom;

import java.util.Comparator;

import jakarta.inject.Inject;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.jaxb.PersistentEntityAdapter;
import org.apache.causeway.persistence.jpa.applib.integration.CausewayEntityListener;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Entity
@Table(
        schema = "jpa",
        uniqueConstraints = {@UniqueConstraint (name = "name", columnNames = "name")}
)
@DomainObject(nature = Nature.ENTITY)
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners({ CausewayEntityListener.class})
public class JpaEntity implements Comparable<JpaEntity> {

    @Id @Column(name = "id", nullable = false)
    private Long id;

    @Version
    private Long version;

    private String name;

    @Override public int compareTo(final JpaEntity o) {
        return Comparator.<JpaEntity,Long>comparing(x -> x.id).compare(this,o);
    }

    @Inject @Transient JpaService jpaService;
}
