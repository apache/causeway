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
package demoapp.dom.domain.actions.Action.executionPublishing;

import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.annotation.SemanticsOf;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.domain._interactions.ExposeCapturedInteractions;
import lombok.Getter;
import lombok.Setter;

//tag::class[]
@PersistenceCapable(identityType = IdentityType.DATASTORE, schema = "demo")
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@DomainObject(
        nature=Nature.ENTITY
        , objectType = "demo.ActionPublishingJdo"
        , editing = Editing.DISABLED
)
public class ActionExecutionPublishingJdo
        implements HasAsciiDocDescription, ExposeCapturedInteractions {
    // ...
//end::class[]

    public ActionExecutionPublishingJdo(String initialValue) {
        this.property = initialValue;
        this.propertyMetaAnnotated = initialValue;
        this.propertyMetaAnnotatedOverridden = initialValue;
    }

    public String title() {
        return "Action#publishing";
    }

//tag::property[]
    @Property()
    @PropertyLayout(fieldSet = "annotation", sequence = "1")
    @Getter @Setter
    private String property;

    @Property()
    @PropertyLayout(fieldSet = "meta-annotated", sequence = "1")
    @Getter @Setter
    private String propertyMetaAnnotated;

    @Property()
    @PropertyLayout(fieldSet = "meta-annotated-overridden", sequence = "1")
    @Getter @Setter
    private String propertyMetaAnnotatedOverridden;
//end::property[]

//tag::annotation[]
    @Action(
        executionPublishing = Publishing.ENABLED       // <.>
        , semantics = SemanticsOf.IDEMPOTENT
        , associateWith = "property"
    )
    @ActionLayout(
        named = "Update Property"
        , describedAs = "@Action(publishing = ENABLED)"
        , sequence = "1"
    )
    public ActionExecutionPublishingJdo updatePropertyUsingAnnotation(final String value) {
        setProperty(value);
        return this;
    }
    public String default0UpdatePropertyUsingAnnotation() {
        return getProperty();
    }

//end::annotation[]

//tag::meta-annotation[]
    @ActionExecutionPublishingEnabledMetaAnnotation      // <.>
    @Action(
        semantics = SemanticsOf.IDEMPOTENT
        , associateWith = "propertyMetaAnnotated"
    )
    @ActionLayout(
        named = "Update Property"
        , describedAs = "@ActionPublishingEnabledMetaAnnotation"
        , sequence = "1"
    )
    public ActionExecutionPublishingJdo updatePropertyUsingMetaAnnotation(final String value) {
        setPropertyMetaAnnotated(value);
        return this;
    }
    public String default0UpdatePropertyUsingMetaAnnotation() {
        return getPropertyMetaAnnotated();
    }
//end::meta-annotation[]

//tag::meta-annotation-overridden[]
    @ActionExecutionPublishingDisabledMetaAnnotation     // <.>
    @Action(
        executionPublishing = Publishing.ENABLED       // <.>
        , semantics = SemanticsOf.IDEMPOTENT
        , associateWith = "propertyMetaAnnotatedOverridden"
    )
    @ActionLayout(
        named = "Update Property"
        , describedAs =
            "@ActionPublishingDisabledMetaAnnotation " +
            "@Action(publishing = ENABLED)"
        , sequence = "1"
    )
    public ActionExecutionPublishingJdo updatePropertyUsingMetaAnnotationButOverridden(final String value) {
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
