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
package demoapp.dom.domain.properties.Property.executionPublishing.jpa;

import javax.inject.Named;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;

import org.springframework.context.annotation.Profile;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.persistence.jpa.applib.integration.CausewayEntityListener;

import demoapp.dom.domain.properties.Property.executionPublishing.PropertyExecutionPublishingDisabledMetaAnnotation;
import demoapp.dom.domain.properties.Property.executionPublishing.PropertyExecutionPublishingEnabledMetaAnnotation;
import demoapp.dom.domain.properties.Property.executionPublishing.PropertyExecutionPublishingEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Profile("demo-jpa")
//tag::class[]
@Entity
@Table(
      schema = "demo",
      name = "PropertyExecutionPublishingJpa"
)
@EntityListeners(CausewayEntityListener.class)
@Named("demo.PropertyExecutionPublishingEntity")
@DomainObject(
        editing = Editing.ENABLED)
@NoArgsConstructor
public class PropertyExecutionPublishingJpa
        extends PropertyExecutionPublishingEntity {
    // ...
//end::class[]

    public PropertyExecutionPublishingJpa(final String initialValue) {
        this.property = initialValue;
        this.propertyMetaAnnotated = initialValue;
        this.propertyMetaAnnotatedOverridden = initialValue;
    }

    @ObjectSupport public String title() {
        return "Property#executionPublishing (JDO)";
    }

    @Id
    @GeneratedValue
    private Long id;

//tag::annotation[]
    @Property(
            executionPublishing = Publishing.ENABLED             // <.>
    )
    @PropertyLayout(
        describedAs =
            "@Property(executionPublishing = ENABLED)",
        fieldSetId = "annotation", sequence = "1")
    @Getter @Setter
    private String property;
//end::annotation[]

//tag::meta-annotated[]
    @PropertyExecutionPublishingEnabledMetaAnnotation                // <.>
    @Property()
    @PropertyLayout(
        describedAs = "@PropertyPublishingEnabledMetaAnnotation",
        fieldSetId = "meta-annotated", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private String propertyMetaAnnotated;
//end::meta-annotated[]

//tag::meta-annotated-overridden[]
    @PropertyExecutionPublishingDisabledMetaAnnotation   // <.>
    @Property(
            executionPublishing = Publishing.ENABLED   // <.>
    )
    @PropertyLayout(
        describedAs =
            "@PropertyPublishingDisabledMetaAnnotation " +
            "@Property(executionPublishing = ENABLED)",
        fieldSetId = "meta-annotated-overridden", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyMetaAnnotatedOverridden;
//end::meta-annotated-overridden[]

//tag::class[]
}
//end::class[]
