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
package demoapp.dom.annotDomain.Property.publishing;

import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.xml.bind.annotation.XmlElement;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Publishing;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.annotDomain._interactions.ExposeCapturedInteractions;

//tag::class[]
@PersistenceCapable(identityType = IdentityType.DATASTORE, schema = "demo")
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@DomainObject(
        nature=Nature.JDO_ENTITY
        , objectType = "demo.PropertyPublishingJdo"
        , editing = Editing.ENABLED
)
public class PropertyPublishingJdo
        implements HasAsciiDocDescription, ExposeCapturedInteractions {
    // ...
//end::class[]

    public PropertyPublishingJdo(String initialValue) {
        this.propertyUsingAnnotation = initialValue;
        this.propertyUsingMetaAnnotation = initialValue;
        this.propertyUsingMetaAnnotationButOverridden = initialValue;
    }

    public String title() {
        return "Property#publishing";
    }

//tag::annotation[]
    @Property(
        publishing = Publishing.ENABLED             // <.>
    )
    @PropertyLayout(
        describedAs =
            "@Property(publishing = ENABLED)"
    )
    @MemberOrder(name = "annotation", sequence = "1")
    @Getter @Setter
    private String propertyUsingAnnotation;
//end::annotation[]

//tag::meta-annotated[]
    @PropertyPublishingEnabledMetaAnnotation                // <.>
    @Property()
    @PropertyLayout(
        describedAs = "@PropertyPublishingEnabledMetaAnnotation"
    )
    @MemberOrder(name = "meta-annotated", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private String propertyUsingMetaAnnotation;
//end::meta-annotated[]

//tag::meta-annotated-overridden[]
    @PropertyPublishingDisabledMetaAnnotation   // <.>
    @Property(
        publishing = Publishing.ENABLED         // <.>
    )
    @PropertyLayout(
        describedAs =
            "@PropertyPublishingDisabledMetaAnnotation " +
            "@Property(publishing = ENABLED)"
    )
    @MemberOrder(name = "meta-annotated-overridden", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyUsingMetaAnnotationButOverridden;
//end::meta-annotated-overridden[]

//tag::class[]
}
//end::class[]
