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
package demoapp.dom.annotDomain.Action.restrictTo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
    nature=Nature.VIEW_MODEL,
    objectType = "demo.ActionRestrictToVm"
)
@NoArgsConstructor
//tag::class[]
public class ActionRestrictToVm implements HasAsciiDocDescription {
    // ...
//end::class[]

    public ActionRestrictToVm(String value) {
        this.propertyNoAnnotation = value;
        this.propertyForPrototyping = value;
        this.propertyForNoRestrictions = value;
        this.propertyForMetaAnnotations = value;
        this.propertyForMetaAnnotationsOverridden = value;
    }

    public String title() {
        return "Action#restrictTo";
    }

    @Property()
    @MemberOrder(name = "not-annotated", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private String propertyNoAnnotation;

    @Property()
    @MemberOrder(name = "annotated", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private String propertyForPrototyping;

    @Property()
    @MemberOrder(name = "annotated", sequence = "2")
    @XmlElement(required = true)
    @Getter @Setter
    private String propertyForNoRestrictions;

    @Property()
    @MemberOrder(name = "meta-annotated", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private String propertyForMetaAnnotations;

    @Property()
    @MemberOrder(name = "meta-annotated-overridden", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private String propertyForMetaAnnotationsOverridden;

//tag::action-no-annotation[]
    @Action(
            semantics = SemanticsOf.IDEMPOTENT
            , associateWith = "propertyNoAnnotation"
            , associateWithSequence = "1"
            // no restrictTo attribute              // <.>
    )
    @ActionLayout(
        describedAs =
            "@Action()"
    )
    public ActionRestrictToVm updateNoAnnotation(final String text) {
        setPropertyNoAnnotation(text);
        return this;
    }
    public String default0UpdateNoAnnotation() {
        return getPropertyNoAnnotation();
    }
//end::action-no-annotation[]

//tag::action-restrict-to-prototyping[]
    @Action(
            semantics = SemanticsOf.IDEMPOTENT
            , associateWith = "propertyForPrototyping"
            , associateWithSequence = "1"
            , restrictTo = RestrictTo.PROTOTYPING // <.>
    )
    @ActionLayout(
        describedAs =
            "@Action(restrictTo = RestrictTo.PROTOTYPING)"
    )
    public ActionRestrictToVm updateRestrictToPrototyping(final String text) {
        setPropertyForPrototyping(text);
        return this;
    }
    public String default0UpdateRestrictToPrototyping() {
        return getPropertyForPrototyping();
    }
//end::action-restrict-to-prototyping[]

//tag::action-restrict-to-no-restrictions[]
    @Action(
            semantics = SemanticsOf.IDEMPOTENT
            , associateWith = "propertyForNoRestrictions"
            , associateWithSequence = "1"
            , restrictTo = RestrictTo.NO_RESTRICTIONS      // <.>
    )
    @ActionLayout(
        describedAs =
            "@Action(restrictTo = RestrictTo.NO_RESTRICTIONS)"
    )
    public ActionRestrictToVm updateRestrictToNoRestrictions(final String text) {
        setPropertyForNoRestrictions(text);
        return this;
    }
    public String default0UpdateRestrictToNoRestrictions() {
        return getPropertyForNoRestrictions();
    }
//end::action-restrict-to-no-restrictions[]

//tag::action-meta-annotated[]
    @ActionRestrictToPrototypingMetaAnnotation      // <.>
    @Action(
            semantics = SemanticsOf.IDEMPOTENT
            , associateWith = "propertyForMetaAnnotations"
            , associateWithSequence = "1"
    )
    @ActionLayout(
        describedAs =
            "@ActionRestrictToPrototypingMetaAnnotation"
    )
    public ActionRestrictToVm updateMetaAnnotated(final String text) {
        setPropertyForMetaAnnotations(text);
        return this;
    }
    public String default0UpdateMetaAnnotated() {
        return getPropertyForMetaAnnotations();
    }
//end::action-meta-annotated[]

//tag::action-meta-annotated-overridden[]
    @ActionRestrictToNoRestrictionsMetaAnnotation   // <.>
    @Action(
            semantics = SemanticsOf.IDEMPOTENT
            , associateWith = "propertyForMetaAnnotationsOverridden"
            , associateWithSequence = "1"
            , restrictTo = RestrictTo.PROTOTYPING   // <.>
    )
    @ActionLayout(
        describedAs =
            "@ActionRestrictToNoRestrictionsMetaAnnotation " +
                    "@Action(restrictTo = RestrictTo.PROTOTYPING"
    )
    public ActionRestrictToVm updateMetaAnnotatedOverridden(final String text) {
        setPropertyForMetaAnnotationsOverridden(text);
        return this;
    }
    public String default0UpdateMetaAnnotatedOverridden() {
        return getPropertyForMetaAnnotationsOverridden();
    }
//end::action-meta-annotated-overridden[]

//tag::class[]
}
//end::class[]
