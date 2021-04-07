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
package demoapp.dom.domain.actions.Action.commandPublishing;

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
import demoapp.dom.domain._commands.ExposePersistedCommands;
import lombok.Getter;
import lombok.Setter;

//tag::class[]
@PersistenceCapable(identityType = IdentityType.DATASTORE, schema = "demo")
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@DomainObject(
        nature=Nature.ENTITY
        , objectType = "demo.ActionCommandJdo"
        , editing = Editing.DISABLED
)
public class ActionCommandPublishingJdo
        implements HasAsciiDocDescription, ExposePersistedCommands {
    // ...
//end::class[]

    public ActionCommandPublishingJdo(String initialValue) {
        this.property = initialValue;
        this.propertyCommandDisabled = initialValue;
        this.propertyMetaAnnotated = initialValue;
        this.propertyMetaAnnotatedOverridden = initialValue;
    }

    public String title() {
        return "Action#command";
    }

//tag::property[]
    @Property()
    @PropertyLayout(group = "annotation", sequence = "1")
    @Getter @Setter
    private String property;

    @Property()
    @PropertyLayout(group = "annotation", sequence = "2")
    @Getter @Setter
    private String propertyCommandDisabled;

    @Property()
    @PropertyLayout(group = "meta-annotated", sequence = "1")
    @Getter @Setter
    private String propertyMetaAnnotated;

    @Property()
    @PropertyLayout(group = "meta-annotated-overridden", sequence = "1")
    @Getter @Setter
    private String propertyMetaAnnotatedOverridden;
//end::property[]

//tag::annotation[]
    @Action(
        commandPublishing = Publishing.ENABLED                  // <.>
        , semantics = SemanticsOf.IDEMPOTENT
        , associateWith = "property"
    )
    @ActionLayout(
        named = "Update Property"
        , describedAs = "@Action(command = ENABLED)"
        , sequence = "1"
    )
    public ActionCommandPublishingJdo updatePropertyUsingAnnotation(final String value) {
        // ...
//end::annotation[]
        setProperty(value);
        return this;
    }
    public String default0UpdatePropertyUsingAnnotation() {
        return getProperty();
//tag::annotation[]
    }
//end::annotation[]

//tag::annotation-2[]
    @Action(
        commandPublishing = Publishing.DISABLED                 // <.>
        , semantics = SemanticsOf.IDEMPOTENT
        , associateWith = "propertyCommandDisabled"
    )
    @ActionLayout(
        named = "Update Property"
        , describedAs = "@Action(command = ENABLED)"
        , sequence = "1"
    )
    public ActionCommandPublishingJdo updatePropertyCommandDisabledUsingAnnotation(final String value) {
        // ...
//end::annotation-2[]
        setPropertyCommandDisabled(value);
        return this;
    }
    public String default0UpdatePropertyCommandDisabledUsingAnnotation() {
        return getPropertyCommandDisabled();
//tag::annotation-2[]
    }
//end::annotation-2[]

//tag::meta-annotation[]
    @ActionCommandPublishingEnabledMetaAnnotation                 // <.>
    @Action(
        semantics = SemanticsOf.IDEMPOTENT
        , associateWith = "propertyMetaAnnotated"
    )
    @ActionLayout(
        named = "Update Property"
        , describedAs = "@ActionCommandEnabledMetaAnnotation"
        , sequence = "1"
    )
    public ActionCommandPublishingJdo updatePropertyUsingMetaAnnotation(final String value) {
        // ...
//end::meta-annotation[]
        setPropertyMetaAnnotated(value);
        return this;
    }
    public String default0UpdatePropertyUsingMetaAnnotation() {
        return getPropertyMetaAnnotated();
//tag::meta-annotation[]
    }
//end::meta-annotation[]

//tag::meta-annotation-overridden[]
    @ActionCommandPublishingDisabledMetaAnnotation                // <.>
    @Action(
        semantics = SemanticsOf.IDEMPOTENT
        , commandPublishing = Publishing.ENABLED                // <.>
        , associateWith = "propertyMetaAnnotatedOverridden"
    )
    @ActionLayout(
        named = "Update Property"
        , describedAs =
            "@ActionCommandDisabledMetaAnnotation @Action(command = ENABLED)"
        , sequence = "1"
    )
    public ActionCommandPublishingJdo updatePropertyUsingMetaAnnotationButOverridden(final String value) {
        // ...
//end::meta-annotation-overridden[]
        setPropertyMetaAnnotatedOverridden(value);
        return this;
    }
    public String default0UpdatePropertyUsingMetaAnnotationButOverridden() {
        return getPropertyMetaAnnotatedOverridden();
//tag::meta-annotation-overridden[]
    }
//end::meta-annotation-overridden[]


//tag::class[]

}
//end::class[]
