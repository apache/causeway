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
package demoapp.dom.services.extensions.secman.apptenancy.jpa;

import javax.inject.Named;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.context.annotation.Profile;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.annotation.Title;
import org.apache.causeway.persistence.jpa.applib.integration.CausewayEntityListener;

import demoapp.dom.services.extensions.secman.apptenancy.persistence.TenantedEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Profile("demo-jpa")
//tag::class[]
@Entity
@Table(
    schema = "demo",
    name = "TenantedJpa"
)
@EntityListeners(CausewayEntityListener.class)
@Named("demo.TenantedEntity")
@DomainObject
@NoArgsConstructor
public class TenantedJpa
        extends TenantedEntity {

    public TenantedJpa(final String name) {
        this.name = name;
    }

    @Id
    @GeneratedValue
    private Long id;

    @Title
    @Property(editing = Editing.ENABLED)
    @PropertyLayout(fieldSetId = "General", sequence = "1")
    @Getter @Setter
    private String name;

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(associateWith = "name", sequence = "1")
    public TenantedJpa updateName(final String name) {
        this.name = name;
        return this;
    }
    @MemberSupport public String default0UpdateName() {
        return this.name;
    }

}
//end::class[]
