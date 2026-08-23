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

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;

import static org.apache.causeway.applib.services.wrapper.control.SyncControl.defaults;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Named("demo.ActionSemanticsVm")
@DomainObject(nature=Nature.VIEW_MODEL)
@DomainObjectLayout(cssClassFa="fa-skull-crossbones")
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
//tag::class[]
public class ActionSemanticsPage implements HasAsciiDocDescription {
    // ...
//end::class[]
    @XmlTransient
    @Inject
    private MessageService messageService;
    @XmlTransient
    @Inject
    private WrapperFactory wrapperFactory;

    public ActionSemanticsPage(final int value) {
        this.propertyNoAnnotation = value;
        this.propertyForSafe = value;
        this.propertyForSafeAndRequestCacheable = value;
        this.propertyForIdempotent = value;
        this.propertyForIdempotentAreYouSure = value;
        this.propertyForNonIdempotent = value;
        this.propertyForNonIdempotentAreYouSure = value;
    }

    @ObjectSupport public String title() {
        return "@Action#semantics";
    }

    @Property()
    @XmlElement(required = true)
    @Getter @Setter
    private int propertyNoAnnotation;

    @Property()
    @XmlElement(required = true)
    @Getter @Setter
    private int propertyForSafe;

    @Property()
    @XmlElement(required = true)
    @Getter @Setter
    private int propertyForSafeAndRequestCacheable;

    @Property()
    @XmlElement(required = true)
    @Getter @Setter
    private int propertyForIdempotent;

    @Property()
    @XmlElement(required = true)
    @Getter @Setter
    private int propertyForIdempotentAreYouSure;

    @Property()
    @XmlElement(required = true)
    @Getter @Setter
    private int propertyForNonIdempotent;

    @Property()
    @PropertyLayout(fieldSetId = "annotated-non-idempotent", sequence = "6")
    @XmlElement(required = true)
    @Getter @Setter
    private int propertyForNonIdempotentAreYouSure;

//tag::action-no-annotation[]
    @Action()                                   // <.>
    public ActionSemanticsPage incrementByAmountNoAnnotation(final int amount) {
        setPropertyNoAnnotation(getPropertyNoAnnotation() + amount);
        return this;
    }
    @MemberSupport public int default0IncrementByAmountNoAnnotation() {
        return 1;
    }
//end::action-no-annotation[]

//tag::action-semantics-safe[]
    @Action(
        semantics = SemanticsOf.SAFE            // <.>
    )
    public ActionSemanticsPage reportPropertyForSafe() {
        messageService.informUser(String.format(
                "'PropertyForSafe' has value %d "
                , getPropertyForSafe()));
        return this;
    }
//end::action-semantics-safe[]

//tag::action-semantics-safe-and-request-cacheable-caller[]
    @Action(
        semantics = SemanticsOf.SAFE
    )
    public ActionSemanticsPage reportPropertyForSafeAndRequestCacheable() {
        int val = 0;
        for (int i=0; i<5; i++) {                                                   // <.>
            val = wrapperFactory.wrap(this, defaults().withSkipRules())              // <.>
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
    )
    @ActionLayout(hidden = Where.EVERYWHERE)                        // <.>
    public int queryPropertyForSafeAndRequestCacheable() {
        ++numberOfTimesActionSafeAndRequestCacheableWasExecuted;    // <.>
        return getPropertyForSafeAndRequestCacheable();
    }
    transient int numberOfTimesActionSafeAndRequestCacheableWasExecuted = 0;
//end::action-semantics-safe-and-request-cacheable[]

//tag::action-semantics-idempotent[]
    @Action(
        semantics = SemanticsOf.IDEMPOTENT              // <.>
    )
    public ActionSemanticsPage updatePropertyForIdempotent(final int value) {
        setPropertyForIdempotent(value);
        return this;
    }
    @MemberSupport public int default0UpdatePropertyForIdempotent() {
        return getPropertyForIdempotent();
    }
//end::action-semantics-idempotent[]

//tag::action-semantics-idempotent-are-you-sure[]
    @Action(
        semantics = SemanticsOf.IDEMPOTENT_ARE_YOU_SURE              // <.>
    )
    public ActionSemanticsPage updatePropertyForIdempotentAreYouSure(final int value) {
        setPropertyForIdempotentAreYouSure(value);
        return this;
    }
    @MemberSupport public int default0UpdatePropertyForIdempotentAreYouSure() {
        return getPropertyForIdempotentAreYouSure();
    }
//end::action-semantics-idempotent-are-you-sure[]

//tag::action-semantics-non-idempotent[]
    @Action(
        semantics = SemanticsOf.NON_IDEMPOTENT      // <.>
    )
    public ActionSemanticsPage incrementByAmountPropertyForNonIdempotent(final int amount) {
        setPropertyForNonIdempotent(getPropertyForNonIdempotent() + amount);
        return this;
    }
    @MemberSupport public int default0IncrementByAmountPropertyForNonIdempotent() {
        return 1;
    }
//end::action-semantics-non-idempotent[]

//tag::action-semantics-non-idempotent-are-you-sure-1[]
    @Action(
        semantics = SemanticsOf.NON_IDEMPOTENT_ARE_YOU_SURE     // <.>
    )
    public ActionSemanticsPage incrementPropertyForNonIdempotentAreYouSure() {
        setPropertyForNonIdempotentAreYouSure(
                getPropertyForNonIdempotentAreYouSure() + 1);
        return this;
    }
//end::action-semantics-non-idempotent-are-you-sure-1[]

//tag::action-semantics-non-idempotent-are-you-sure-2[]
    @Action(
        semantics = SemanticsOf.NON_IDEMPOTENT_ARE_YOU_SURE     // <.>
    )
    public ActionSemanticsPage incrementByAmountPropertyForNonIdempotentAreYouSure(final int amount) {
        setPropertyForNonIdempotentAreYouSure(getPropertyForNonIdempotentAreYouSure() + amount);
        return this;
    }
    @MemberSupport public int default0IncrementByAmountPropertyForNonIdempotentAreYouSure() {
        return 5;
    }
//end::action-semantics-non-idempotent-are-you-sure-2[]

//tag::class[]
}
//end::class[]
