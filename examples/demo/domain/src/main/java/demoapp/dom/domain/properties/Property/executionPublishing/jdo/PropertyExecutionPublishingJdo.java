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
package demoapp.dom.domain.properties.Property.executionPublishing.jdo;

import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.xml.bind.annotation.XmlElement;

import org.springframework.context.annotation.Profile;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.ObjectSupport;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Publishing;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom.domain.properties.Property.executionPublishing.PropertyExecutionPublishingDisabledMetaAnnotation;
import demoapp.dom.domain.properties.Property.executionPublishing.PropertyExecutionPublishingEnabledMetaAnnotation;
import demoapp.dom.domain.properties.Property.executionPublishing.PropertyExecutionPublishingEntity;

@Profile("demo-jdo")
//tag::class[]
@PersistenceCapable(identityType = IdentityType.DATASTORE, schema = "demo")
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@DomainObject(
        nature=Nature.ENTITY
        , logicalTypeName = "demo.PropertyExecutionPublishingEntity"
        , editing = Editing.ENABLED
)
public class PropertyExecutionPublishingJdo
        extends PropertyExecutionPublishingEntity {
    // ...
//end::class[]

    public PropertyExecutionPublishingJdo(final String initialValue) {
        this.property = initialValue;
        this.propertyMetaAnnotated = initialValue;
        this.propertyMetaAnnotatedOverridden = initialValue;
    }

    @ObjectSupport public String title() {
        return "Property#executionPublishing (JDO)";
    }

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
