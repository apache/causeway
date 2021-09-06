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
package demoapp.dom.domain.actions.Action.restrictTo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Title;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
    nature=Nature.VIEW_MODEL,
    logicalTypeName = "demo.ActionRestrictToVm"
)
@NoArgsConstructor
//tag::class[]
public class ActionRestrictToVm implements HasAsciiDocDescription {
    // ...
//end::class[]

    public ActionRestrictToVm(final String value) {
        this.propertyNoAnnotation = value;
        this.propertyForPrototyping = value;
        this.propertyForNoRestrictions = value;
        this.propertyForMetaAnnotations = value;
        this.propertyForMetaAnnotationsOverridden = value;
    }

    @Title
    public String title() {
        return "Action#restrictTo";
    }

    @Property()
    @PropertyLayout(fieldSetId = "not-annotated", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private String propertyNoAnnotation;

    @Property()
    @PropertyLayout(fieldSetId = "annotated", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private String propertyForPrototyping;

    @Property()
    @PropertyLayout(fieldSetId = "annotated", sequence = "2")
    @XmlElement(required = true)
    @Getter @Setter
    private String propertyForNoRestrictions;

    @Property()
    @PropertyLayout(fieldSetId = "meta-annotated", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private String propertyForMetaAnnotations;

    @Property()
    @PropertyLayout(fieldSetId = "meta-annotated-overridden", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private String propertyForMetaAnnotationsOverridden;

//tag::action-no-annotation[]
    @Action(
            semantics = SemanticsOf.IDEMPOTENT
            // no restrictTo attribute              // <.>
    )
    @ActionLayout(
        describedAs =
            "@Action()"
        , associateWith = "propertyNoAnnotation"
        , sequence = "1"
    )
    public ActionRestrictToVm updateNoAnnotation(final String text) {
        setPropertyNoAnnotation(text);
        return this;
    }
    @MemberSupport public String default0UpdateNoAnnotation() {
        return getPropertyNoAnnotation();
    }
//end::action-no-annotation[]

//tag::action-restrict-to-prototyping[]
    @Action(
        semantics = SemanticsOf.IDEMPOTENT
        , restrictTo = RestrictTo.PROTOTYPING         // <.>
    )
    @ActionLayout(
        describedAs =
            "@Action(restrictTo = RestrictTo.PROTOTYPING)"
        , associateWith = "propertyForPrototyping"
        , sequence = "1"
    )
    public ActionRestrictToVm updateRestrictToPrototyping(final String text) {
        setPropertyForPrototyping(text);
        return this;
    }
    @MemberSupport public String default0UpdateRestrictToPrototyping() {
        return getPropertyForPrototyping();
    }
//end::action-restrict-to-prototyping[]

//tag::action-restrict-to-no-restrictions[]
    @Action(
        semantics = SemanticsOf.IDEMPOTENT
        , restrictTo = RestrictTo.NO_RESTRICTIONS      // <.>
    )
    @ActionLayout(
        describedAs =
            "@Action(restrictTo = RestrictTo.NO_RESTRICTIONS)"
        , associateWith = "propertyForNoRestrictions"
        , sequence = "1"
    )
    public ActionRestrictToVm updateRestrictToNoRestrictions(final String text) {
        setPropertyForNoRestrictions(text);
        return this;
    }
    @MemberSupport public String default0UpdateRestrictToNoRestrictions() {
        return getPropertyForNoRestrictions();
    }
//end::action-restrict-to-no-restrictions[]

//tag::action-meta-annotated[]
    @ActionRestrictToPrototypingMetaAnnotation      // <.>
    @Action(
        semantics = SemanticsOf.IDEMPOTENT
    )
    @ActionLayout(
        describedAs =
            "@ActionRestrictToPrototypingMetaAnnotation"
        , associateWith = "propertyForMetaAnnotations"
        , sequence = "1"
    )
    public ActionRestrictToVm updateMetaAnnotated(final String text) {
        setPropertyForMetaAnnotations(text);
        return this;
    }
    @MemberSupport public String default0UpdateMetaAnnotated() {
        return getPropertyForMetaAnnotations();
    }
//end::action-meta-annotated[]

//tag::action-meta-annotated-overridden[]
    @ActionRestrictToNoRestrictionsMetaAnnotation   // <.>
    @Action(
        semantics = SemanticsOf.IDEMPOTENT
        , restrictTo = RestrictTo.PROTOTYPING   // <.>
    )
    @ActionLayout(
        describedAs =
            "@ActionRestrictToNoRestrictionsMetaAnnotation " +
            "@Action(restrictTo = RestrictTo.PROTOTYPING)"
        , associateWith = "propertyForMetaAnnotationsOverridden"
        , sequence = "1"
    )
    public ActionRestrictToVm updateMetaAnnotatedOverridden(final String text) {
        setPropertyForMetaAnnotationsOverridden(text);
        return this;
    }
    @MemberSupport public String default0UpdateMetaAnnotatedOverridden() {
        return getPropertyForMetaAnnotationsOverridden();
    }
//end::action-meta-annotated-overridden[]

//tag::class[]
}
//end::class[]
