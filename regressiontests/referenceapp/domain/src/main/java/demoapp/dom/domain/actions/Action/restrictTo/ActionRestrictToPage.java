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

import jakarta.inject.Named;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Named("demo.ActionRestrictToPage")
@DomainObject(nature=Nature.VIEW_MODEL)
@DomainObjectLayout(cssClassFa="fa-paper-plane")
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
//tag::class[]
public class ActionRestrictToPage implements HasAsciiDocDescription {
    // ...
//end::class[]

    public ActionRestrictToPage(final String value) {
        this.value = value;
    }

    @ObjectSupport public String title() {
        return "@Action#restrictTo";
    }

    @Property()
    @XmlElement(required = true)
    @Getter @Setter
    private String value;

//tag::action-no-annotation[]
    @Action(
            semantics = SemanticsOf.IDEMPOTENT
            // no restrictTo attribute                  // <.>
    )
    public ActionRestrictToPage updateNoAnnotation(final String text) {
        setValue(text);
        return this;
    }
    @MemberSupport public String default0UpdateNoAnnotation() {
        return getValue();
    }
//end::action-no-annotation[]

//tag::action-restrict-to-prototyping[]
    @Action(
        semantics = SemanticsOf.IDEMPOTENT,
        restrictTo = RestrictTo.PROTOTYPING             // <.>
    )
    public ActionRestrictToPage updateRestrictToPrototyping(final String text) {
        setValue(text);
        return this;
    }
    @MemberSupport public String default0UpdateRestrictToPrototyping() {
        return getValue();
    }
//end::action-restrict-to-prototyping[]

//tag::action-restrict-to-no-restrictions[]
    @Action(
        semantics = SemanticsOf.IDEMPOTENT,
        restrictTo = RestrictTo.NO_RESTRICTIONS         // <.>
    )
    public ActionRestrictToPage updateRestrictToNoRestrictions(final String text) {
        setValue(text);
        return this;
    }
    @MemberSupport public String default0UpdateRestrictToNoRestrictions() {
        return getValue();
    }
//end::action-restrict-to-no-restrictions[]

//tag::class[]
}
//end::class[]
