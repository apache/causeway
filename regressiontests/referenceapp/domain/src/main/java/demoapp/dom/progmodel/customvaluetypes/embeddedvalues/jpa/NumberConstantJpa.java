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
package demoapp.dom.progmodel.customvaluetypes.embeddedvalues.jpa;

import jakarta.inject.Named;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.springframework.context.annotation.Profile;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.persistence.jpa.applib.integration.CausewayEntityListener;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import demoapp.dom.progmodel.customvaluetypes.embeddedvalues.ComplexNumber;
import demoapp.dom.progmodel.customvaluetypes.embeddedvalues.NumberConstantEntity;

@Profile("demo-jpa")
//tag::class[]
@Entity
@Table(
      schema = "demo",
      name = "NumberConstantJpa"
)
@EntityListeners(CausewayEntityListener.class)
@Named("demo.NumberConstantEntityJpa")
@DomainObject
@NoArgsConstructor
public class NumberConstantJpa
//end::class[]
        extends NumberConstantEntity
//tag::class[]
{
    // ...
//end::class[]
    @ObjectSupport public String title() {
        return getName();
    }

    @Override
    public ComplexNumber value() {
        return getNumber();
    }

    @Id
    @GeneratedValue
    private Long id;

//tag::class[]
    @Column(nullable = false)
    @Property
    @Getter @Setter
    private String name;

    @jakarta.persistence.Embedded                 // <.>
    @Property(editing = Editing.ENABLED)
    @Getter @Setter
    private ComplexNumberJpa number;
}
//end::class[]
