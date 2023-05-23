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
package demoapp.dom.domain.objects.DomainObjectLayout.bookmarking.jdo;

import java.util.Set;
import java.util.TreeSet;

import javax.inject.Named;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.springframework.context.annotation.Profile;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom.domain.objects.DomainObjectLayout.bookmarking.DomainObjectLayoutBookmarkingEntity;

@Profile("demo-jdo")
@PersistenceCapable(
    identityType = IdentityType.DATASTORE,
    schema = "demo",
    table = "DomainObjectLayoutBookmarkingEntity"
)
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@Named("demo.DomainObjectLayoutBookmarkingEntity")
//tag::class[]
// ...
@DomainObject(nature = Nature.ENTITY)
public class DomainObjectLayoutBookmarkingEntityImpl extends DomainObjectLayoutBookmarkingEntity {
    // ...
//end::class[]

    public DomainObjectLayoutBookmarkingEntityImpl(String value) {
        setName(value);
    }

    @Getter @Setter
    private String name;

    @Collection
    @Persistent(
            mappedBy = "parent",
            defaultFetchGroup = "true",
            dependentElement = "true"
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
