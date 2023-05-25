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
package demoapp.dom.progmodel.actions.bulk.jpa;

import javax.inject.Named;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.springframework.context.annotation.Profile;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.jaxb.PersistentEntityAdapter;
import org.apache.causeway.persistence.jpa.applib.integration.CausewayEntityListener;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import demoapp.dom.progmodel.actions.bulk.BulkActionItemEntity;

@Profile("demo-jpa")
@Entity
@Table(
    schema = "demo",
    name = "BulkActionItemEntity"
)
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
@EntityListeners(CausewayEntityListener.class)
@Named("demo.BulkActionItemEntity")
@NoArgsConstructor
//tag::class[]
// ...
@DomainObject(nature = Nature.ENTITY)
public class BulkActionItemEntityImpl extends BulkActionItemEntity {
    // ...
//end::class[]

    public BulkActionItemEntityImpl(String value) {
        setName(value);
    }

    @Id
    @GeneratedValue
    private Long id;

    @Getter @Setter
    private String name;

//tag::class[]
}
//end::class[]
