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
package demoapp.dom.annotDomain.Action.publishing;

import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.annotDomain._interactions.ExposeCapturedInteractions;

//tag::class[]
@PersistenceCapable(identityType = IdentityType.DATASTORE, schema = "demo")
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@DomainObject(
        nature=Nature.JDO_ENTITY
        , objectType = "demo.ActionPublishingJdo"
        , editing = Editing.DISABLED
)
public class ActionPublishingJdo
        implements HasAsciiDocDescription, ExposeCapturedInteractions {
    // ...
//end::class[]

    public ActionPublishingJdo(String initialValue) {
        this.property = initialValue;
        this.propertyMetaAnnotated = initialValue;
        this.propertyMetaAnnotatedOverridden = initialValue;
    }

    public String title() {
        return "Action#publishing";
    }

//tag::property[]
    @Property()
    @MemberOrder(name = "annotation", sequence = "1")
    @Getter @Setter
    private String property;

    @Property()
    @MemberOrder(name = "meta-annotated", sequence = "1")
    @Getter @Setter
    private String propertyMetaAnnotated;

    @Property()
    @MemberOrder(name = "meta-annotated-overridden", sequence = "1")
    @Getter @Setter
    private String propertyMetaAnnotatedOverridden;
//end::property[]

//tag::annotation[]
    @Action(
        publishing = Publishing.ENABLED         // <.>
        , semantics = SemanticsOf.IDEMPOTENT
        , associateWith = "property"
        , associateWithSequence = "1"
    )
    @ActionLayout(
        named = "Update Property"
        , describedAs = "@Action(publishing = ENABLED)"
    )
    public ActionPublishingJdo updatePropertyUsingAnnotation(final String value) {
        setProperty(value);
        return this;
    }
    public String default0UpdatePropertyUsingAnnotation() {
        return getProperty();
    }

//end::annotation[]

//tag::meta-annotation[]
    @ActionPublishingEnabledMetaAnnotation      // <.>
    @Action(
        semantics = SemanticsOf.IDEMPOTENT
        , associateWith = "propertyMetaAnnotated"
        , associateWithSequence = "1"
    )
    @ActionLayout(
        named = "Update Property"
        , describedAs = "@ActionPublishingEnabledMetaAnnotation"
    )
    public ActionPublishingJdo updatePropertyUsingMetaAnnotation(final String value) {
        setPropertyMetaAnnotated(value);
        return this;
    }
    public String default0UpdatePropertyUsingMetaAnnotation() {
        return getPropertyMetaAnnotated();
    }
//end::meta-annotation[]

//tag::meta-annotation-overridden[]
    @ActionPublishingDisabledMetaAnnotation     // <.>
    @Action(
        publishing = Publishing.ENABLED         // <.>
        , semantics = SemanticsOf.IDEMPOTENT
        , associateWith = "propertyMetaAnnotatedOverridden"
        , associateWithSequence = "1"
    )
    @ActionLayout(
        named = "Update Property"
        , describedAs =
            "@ActionPublishingDisabledMetaAnnotation " +
            "@Action(publishing = ENABLED)"
    )
    public ActionPublishingJdo updatePropertyUsingMetaAnnotationButOverridden(final String value) {
        setPropertyMetaAnnotatedOverridden(value);
        return this;
    }
    public String default0UpdatePropertyUsingMetaAnnotationButOverridden() {
        return getPropertyMetaAnnotatedOverridden();
    }
//end::meta-annotation-overridden[]

//tag::class[]
}
//end::class[]
