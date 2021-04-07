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
package demoapp.dom.domain.actions.Action.semantics;

import static org.apache.isis.applib.services.wrapper.control.SyncControl.control;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
    nature=Nature.VIEW_MODEL,
    objectType = "demo.ActionSemanticsVm"
)
@NoArgsConstructor
//tag::class[]
public class ActionSemanticsVm implements HasAsciiDocDescription {
    // ...
//end::class[]
    @XmlTransient
    @Inject
    private MessageService messageService;
    @XmlTransient
    @Inject
    private WrapperFactory wrapperFactory;

    public ActionSemanticsVm(final int value) {
        this.propertyNoAnnotation = value;
        this.propertyForSafe = value;
        this.propertyForSafeAndRequestCacheable = value;
        this.propertyForIdempotent = value;
        this.propertyForIdempotentAreYouSure = value;
        this.propertyForNonIdempotent = value;
        this.propertyForNonIdempotentAreYouSure = value;
        this.propertyForMetaAnnotations = value;
        this.propertyForMetaAnnotationsOverridden = value;
    }

    public String title() {
        return "Action#semantics";
    }

    @Property()
    @PropertyLayout(group = "not-annotated", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private int propertyNoAnnotation;

    @Property()
    @PropertyLayout(group = "annotated-safe", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private int propertyForSafe;

    @Property()
    @PropertyLayout(group = "annotated-safe", sequence = "2")
    @XmlElement(required = true)
    @Getter @Setter
    private int propertyForSafeAndRequestCacheable;


    @Property()
    @PropertyLayout(group = "annotated-idempotent", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private int propertyForIdempotent;

    @Property()
    @PropertyLayout(group = "annotated-idempotent", sequence = "2")
    @XmlElement(required = true)
    @Getter @Setter
    private int propertyForIdempotentAreYouSure;

    @Property()
    @PropertyLayout(group = "annotated-non-idempotent", sequence = "5")
    @XmlElement(required = true)
    @Getter @Setter
    private int propertyForNonIdempotent;

    @Property()
    @PropertyLayout(group = "annotated-non-idempotent", sequence = "6")
    @XmlElement(required = true)
    @Getter @Setter
    private int propertyForNonIdempotentAreYouSure;

    @Property()
    @PropertyLayout(group = "meta-annotated", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private int propertyForMetaAnnotations;

    @Property()
    @PropertyLayout(group = "meta-annotated-overridden", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private int propertyForMetaAnnotationsOverridden;

//tag::action-no-annotation[]
    @Action(
        associateWith = "propertyNoAnnotation"
        // no semantics attribute              // <.>
    )
    @ActionLayout(
        named = "Increment by Amount",
        describedAs = "@Action()"
        , sequence = "1"
    )
    public ActionSemanticsVm incrementByAmountNoAnnotation(final int amount) {
        setPropertyNoAnnotation(getPropertyNoAnnotation() + amount);
        return this;
    }
    public int default0IncrementNoAnnotation() {
        return 1;
    }
//end::action-no-annotation[]

//tag::action-semantics-safe[]
    @Action(
        semantics = SemanticsOf.SAFE            // <.>
        , associateWith = "propertyForSafe"
    )
    @ActionLayout(
        named = "Report",
        describedAs =
            "@Action(semantics = SemanticsOf.SAFE)"
        , sequence = "1"
    )
    public ActionSemanticsVm reportPropertyForSafe() {
        messageService.informUser(String.format(
                "'PropertyForSafe' has value %d "
                , getPropertyForSafe()));
        return this;
    }
//end::action-semantics-safe[]

//tag::action-semantics-safe-and-request-cacheable-caller[]
    @Action(
        semantics = SemanticsOf.SAFE
        , associateWith = "propertyForSafeAndRequestCacheable"
    )
    @ActionLayout(
        named = "Report"
        , describedAs = "@Action(semantics = SemanticsOf.SAFE)"
        , sequence = "1"
    )
    public ActionSemanticsVm reportPropertyForSafeAndRequestCacheable() {
        int val = 0;
        for (int i=0; i<5; i++) {                                                   // <.>
            val = wrapperFactory.wrap(this, control().withSkipRules())              // <.>
                    .queryPropertyForSafeAndRequestCacheable();
        }
        messageService.informUser(String.format(
                "Action 'queryPropertyForSafeAndRequestCacheable' returns %d " +
                "and was invoked 5 times in this action but was executed %d times." // <.>
                , val, numberOfTimesActionSafeAndRequestCacheableWasExecuted));
        return this;
    }
//end::action-semantics-safe-and-request-cacheable-caller[]

//tag::action-semantics-safe-and-request-cacheable[]
    @Action(
        semantics = SemanticsOf.SAFE_AND_REQUEST_CACHEABLE          // <.>
        , hidden = Where.EVERYWHERE                                 // <.>
    )
    public int queryPropertyForSafeAndRequestCacheable() {
        ++numberOfTimesActionSafeAndRequestCacheableWasExecuted;    // <.>
        return getPropertyForSafeAndRequestCacheable();
    }
    transient int numberOfTimesActionSafeAndRequestCacheableWasExecuted = 0;
//end::action-semantics-safe-and-request-cacheable[]

//tag::action-semantics-idempotent[]
    @Action(
        semantics = SemanticsOf.IDEMPOTENT              // <.>
        , associateWith = "propertyForIdempotent"
    )
    @ActionLayout(
        named = "Set to Value"
        , describedAs = "@Action(semantics = SemanticsOf.IDEMPOTENT)"
        , sequence = "1"
    )
    public ActionSemanticsVm setToValuePropertyForIdempotent(final int value) {
        setPropertyForIdempotent(value);
        return this;
    }
    public int default0SetToValuePropertyForIdempotent() {
        return getPropertyForIdempotent();
    }
//end::action-semantics-idempotent[]

//tag::action-semantics-idempotent-are-you-sure[]
    @Action(
        semantics = SemanticsOf.IDEMPOTENT_ARE_YOU_SURE              // <.>
        , associateWith = "propertyForIdempotent"
    )
    @ActionLayout(
        named = "Set to Value",
        describedAs = "@Action(semantics = SemanticsOf.IDEMPOTENT_ARE_YOU_SURE)"
        , sequence = "1"
    )
    public ActionSemanticsVm setToValuePropertyForIdempotentAreYouSure(final int value) {
        setPropertyForIdempotentAreYouSure(value);
        return this;
    }
    public int default0SetToValuePropertyForIdempotentAreYouSure() {
        return getPropertyForIdempotentAreYouSure();
    }
//end::action-semantics-idempotent-are-you-sure[]

//tag::action-semantics-non-idempotent[]
    @Action(
        semantics = SemanticsOf.NON_IDEMPOTENT      // <.>
        , associateWith = "propertyForNonIdempotent"
    )
    @ActionLayout(
        named = "Increment by Amount"
        , describedAs =
            "@Action(semantics = SemanticsOf.NON_IDEMPOTENT)"
        , sequence = "1"
    )
    public ActionSemanticsVm incrementByAmountPropertyForNonIdempotent(final int amount) {
        setPropertyForNonIdempotent(getPropertyForNonIdempotent() + amount);
        return this;
    }
    public int default0IncrementByAmountPropertyForNonIdempotent() {
        return 1;
    }
//end::action-semantics-non-idempotent[]

//tag::action-semantics-non-idempotent-are-you-sure[]
    @Action(
        semantics = SemanticsOf.NON_IDEMPOTENT_ARE_YOU_SURE     // <.>
        , associateWith = "propertyForNonIdempotentAreYouSure"
    )
    @ActionLayout(
        named = "Increment"
        , describedAs =
            "@Action(semantics = SemanticsOf.NON_IDEMPOTENT_ARE_YOU_SURE)"
        , sequence = "1"
    )
    public ActionSemanticsVm incrementPropertyForNonIdempotentAreYouSure() {
        setPropertyForNonIdempotentAreYouSure(
                getPropertyForNonIdempotentAreYouSure() + 1);
        return this;
    }

    @Action(
        semantics = SemanticsOf.NON_IDEMPOTENT_ARE_YOU_SURE     // <.>
        , associateWith = "propertyForNonIdempotentAreYouSure"
    )
    @ActionLayout(
        named = "Increment by Amount"
        , describedAs =
            "@Action(semantics = SemanticsOf.NON_IDEMPOTENT_ARE_YOU_SURE)"
        , sequence = "2"
    )
    public ActionSemanticsVm incrementByAmountPropertyForNonIdempotentAreYouSure(final int amount) {
        setPropertyForNonIdempotentAreYouSure(getPropertyForNonIdempotentAreYouSure() + amount);
        return this;
    }
    public int default0IncrementByAmountPropertyForNonIdempotentAreYouSure() {
        return 1;
    }
//end::action-semantics-non-idempotent-are-you-sure[]

//tag::action-meta-annotated[]
    @ActionSemanticsIdempotentMetaAnnotation      // <.>
    @Action(
        semantics = SemanticsOf.IDEMPOTENT
        , associateWith = "propertyForMetaAnnotations"
    )
    @ActionLayout(
        named = "Set to Value"
        , describedAs =
            "@ActionSemanticsIdempotentMetaAnnotation"
        , sequence = "1"
    )
    public ActionSemanticsVm setToValueMetaAnnotated(final int value) {
        setPropertyForMetaAnnotations(value);
        return this;
    }
    public int default0UpdateMetaAnnotated() {
        return getPropertyForMetaAnnotations();
    }
//end::action-meta-annotated[]

//tag::action-meta-annotated-overridden[]
    @ActionSemanticsSafeMetaAnnotation              // <.>
    @Action(
        semantics = SemanticsOf.IDEMPOTENT      // <.>
        , associateWith = "propertyForMetaAnnotationsOverridden"
    )
    @ActionLayout(
        named = "Set to Value"
        , describedAs =
            "@ActionSemanticsSafeMetaAnnotation " +
            "@Action(semantics = SemanticsOf.IDEMPOTENT)"
        , sequence = "1"
    )
    public ActionSemanticsVm setToValueMetaAnnotatedOverridden(final int val) {
        setPropertyForMetaAnnotationsOverridden(val);
        return this;
    }
    public int default0UpdateMetaAnnotatedOverridden() {
        return getPropertyForMetaAnnotationsOverridden();
    }
//end::action-meta-annotated-overridden[]

//tag::class[]
}
//end::class[]
