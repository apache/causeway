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
package demoapp.dom.domain.properties.Property.executionPublishing;

import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.xml.bind.annotation.XmlElement;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Publishing;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.domain._interactions.ExposeCapturedInteractions;
import lombok.Getter;
import lombok.Setter;

//tag::class[]
@PersistenceCapable(identityType = IdentityType.DATASTORE, schema = "demo")
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@DomainObject(
        nature=Nature.ENTITY
        , objectType = "demo.PropertyPublishingJdo"
        , editing = Editing.ENABLED
)
public class PropertyExecutionPublishingJdo
        implements HasAsciiDocDescription, ExposeCapturedInteractions {
    // ...
//end::class[]

    public PropertyExecutionPublishingJdo(String initialValue) {
        this.propertyUsingAnnotation = initialValue;
        this.propertyUsingMetaAnnotation = initialValue;
        this.propertyUsingMetaAnnotationButOverridden = initialValue;
    }

    public String title() {
        return "Property#publishing";
    }

//tag::annotation[]
    @Property(
            executionPublishing = Publishing.ENABLED             // <.>
    )
    @PropertyLayout(
        describedAs =
            "@Property(publishing = ENABLED)",
        group = "annotation", sequence = "1")
    @Getter @Setter
    private String propertyUsingAnnotation;
//end::annotation[]

//tag::meta-annotated[]
    @PropertyExecutionPublishingEnabledMetaAnnotation                // <.>
    @Property()
    @PropertyLayout(
        describedAs = "@PropertyPublishingEnabledMetaAnnotation",
        group = "meta-annotated", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private String propertyUsingMetaAnnotation;
//end::meta-annotated[]

//tag::meta-annotated-overridden[]
    @PropertyExecutionPublishingDisabledMetaAnnotation   // <.>
    @Property(
            executionPublishing = Publishing.ENABLED   // <.>
    )
    @PropertyLayout(
        describedAs =
            "@PropertyPublishingDisabledMetaAnnotation " +
            "@Property(publishing = ENABLED)",
        group = "meta-annotated-overridden", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyUsingMetaAnnotationButOverridden;
//end::meta-annotated-overridden[]

//tag::class[]
}
//end::class[]
