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
package demoapp.dom.domain.objects.DomainObjectLayout.bookmarking.jpa;

import java.util.Set;
import java.util.TreeSet;

import jakarta.inject.Named;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.springframework.context.annotation.Profile;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.persistence.jpa.applib.integration.CausewayEntityListener;

import demoapp.dom.domain.objects.DomainObjectLayout.bookmarking.DomainObjectLayoutBookmarkingEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Profile("demo-jpa")
@Entity
@Table(
    schema = "demo",
    name = "DomainObjectLayoutBookmarkingEntity"
)
@EntityListeners(CausewayEntityListener.class)
@Named("demo.DomainObjectLayoutBookmarkingEntity")
@NoArgsConstructor
//tag::class[]
// ...
@DomainObject(nature = Nature.ENTITY)
public class DomainObjectLayoutBookmarkingEntityImpl extends DomainObjectLayoutBookmarkingEntity {
    // ...
//end::class[]

    public DomainObjectLayoutBookmarkingEntityImpl(String value) {
        setName(value);
    }

    @Id
    @GeneratedValue
    private Long id;

    @Getter @Setter
    private String name;

    @Collection
    @OneToMany(
            mappedBy = "parent",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Getter @Setter
    private Set<DomainObjectLayoutBookmarkingChildEntityImpl> children = new TreeSet<>();

    @Override
    public void addChild(String value) {
        getChildren().add(new DomainObjectLayoutBookmarkingChildEntityImpl(this, value));
    }

//tag::class[]
}
//end::class[]
