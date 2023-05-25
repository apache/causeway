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
package demoapp.dom.progmodel.actions.bulk.jdo;

import jakarta.inject.Named;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.springframework.context.annotation.Profile;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.jaxb.PersistentEntityAdapter;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom.progmodel.actions.bulk.BulkActionItemEntity;

@Profile("demo-jdo")
@PersistenceCapable(
    identityType = IdentityType.DATASTORE,
    schema = "demo",
    table = "BulkActionItemEntity"
)
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
@Named("demo.BulkActionItemEntity")
//tag::class[]
// ...
@DomainObject(nature = Nature.ENTITY)
public class BulkActionItemEntityImpl extends BulkActionItemEntity {
    // ...
//end::class[]

    public BulkActionItemEntityImpl(String value) {
        setName(value);
    }

    @Getter @Setter
    private String name;

//tag::class[]
}
//end::class[]
