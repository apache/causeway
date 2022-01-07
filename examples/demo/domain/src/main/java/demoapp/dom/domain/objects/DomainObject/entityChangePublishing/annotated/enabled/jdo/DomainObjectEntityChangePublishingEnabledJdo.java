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
package demoapp.dom.domain.objects.DomainObject.entityChangePublishing.annotated.enabled.jdo;

import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;

import org.springframework.context.annotation.Profile;

import org.apache.isis.applib.annotations.Bounding;
import org.apache.isis.applib.annotations.DomainObject;
import org.apache.isis.applib.annotations.DomainObjectLayout;
import org.apache.isis.applib.annotations.Nature;
import org.apache.isis.applib.annotations.Publishing;
import org.apache.isis.applib.annotations.Title;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.annotated.enabled.DomainObjectEntityChangePublishingEnabledEntity;

@Profile("demo-jdo")
//tag::class[]
@PersistenceCapable(identityType = IdentityType.DATASTORE, schema = "demo")
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@DomainObject(
    nature=Nature.ENTITY
    , logicalTypeName = "demo.DomainObjectEntityChangePublishingEnabledEntity"
    , entityChangePublishing = Publishing.ENABLED            // <.>
    , bounding = Bounding.BOUNDED
)
@DomainObjectLayout(
    describedAs = "@DomainObject(entityChangePublishing=ENABLED)"
)
public class DomainObjectEntityChangePublishingEnabledJdo
                extends DomainObjectEntityChangePublishingEnabledEntity {
    // ...
//end::class[]

    public DomainObjectEntityChangePublishingEnabledJdo(String initialValue) {
        this.property = initialValue;
        this.propertyUpdatedByAction = initialValue;
    }

    @Title(sequence = "1.0")
    @Getter @Setter
    private String property;

    @Getter @Setter
    @Title(sequence = "2.0", prepend = " / ")
    private String propertyUpdatedByAction;

//tag::class[]
}
//end::class[]
