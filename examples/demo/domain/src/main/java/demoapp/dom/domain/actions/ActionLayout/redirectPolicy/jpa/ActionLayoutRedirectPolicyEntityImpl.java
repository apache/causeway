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
package demoapp.dom.domain.actions.ActionLayout.redirectPolicy.jpa;

import demoapp.dom.domain.actions.ActionLayout.redirectPolicy.ActionLayoutRedirectPolicyEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import lombok.SneakyThrows;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Repainting;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.persistence.jpa.applib.integration.CausewayEntityListener;

import org.springframework.context.annotation.Profile;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.*;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;

@Profile("demo-jpa")
@Entity
@Table(
    schema = "demo",
    name = "ActionLayoutRedirectPolicyEntity"
)
@EntityListeners(CausewayEntityListener.class)
@Named("demo.ActionLayoutRedirectPolicyEntity")
@NoArgsConstructor
//tag::class[]
// ...
@DomainObject(nature = Nature.ENTITY)
public class ActionLayoutRedirectPolicyEntityImpl extends ActionLayoutRedirectPolicyEntity {
    // ...
//end::class[]

    public ActionLayoutRedirectPolicyEntityImpl(String value) {
        setName(value);
    }

    @Id
    @GeneratedValue
    private Long id;

    @Getter @Setter
    private String name;

    @Property(optionality = Optionality.OPTIONAL)
    @XmlElement(required = false)
    @Getter @Setter
    private Integer count;

//tag::class[]
}
//end::class[]
