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
package demoapp.dom.domain.objects.DomainObject.entityChangePublishing.annotated.disabled.jpa;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.context.annotation.Profile;

import org.apache.isis.applib.annotation.Bounding;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.persistence.jpa.applib.integration.JpaEntityInjectionPointResolver;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.annotated.disabled.DomainObjectEntityChangePublishingDisabledEntity;

@Profile("demo-jpa")
//tag::class[]
@Entity
@Table(
    schema = "demo",
    name = "DomainObjectEntityChangePublishingDisabledJpa"
)
@EntityListeners(JpaEntityInjectionPointResolver.class)
@DomainObject(
        nature=Nature.ENTITY
        , logicalTypeName = "demo.DomainObjectEntityChangePublishingDisabledEntity"
        , entityChangePublishing = Publishing.DISABLED           // <.>
        , bounding = Bounding.BOUNDED
    )
    @DomainObjectLayout(
        describedAs = "@DomainObject(entityChangePublishing=DISABLED)"
    )
@NoArgsConstructor
public class DomainObjectEntityChangePublishingDisabledJpa
                extends DomainObjectEntityChangePublishingDisabledEntity {
    // ...
//end::class[]

    public DomainObjectEntityChangePublishingDisabledJpa(String initialValue) {
        this.property = initialValue;
        this.propertyUpdatedByAction = initialValue;
    }

    @Id
    @GeneratedValue
    private Long id;

    @Title(sequence = "1.0")
    @Getter @Setter
    private String property;

    @Getter @Setter
    @Title(sequence = "2.0", prepend = " / ")
    private String propertyUpdatedByAction;

//tag::class[]
}
//end::class[]
