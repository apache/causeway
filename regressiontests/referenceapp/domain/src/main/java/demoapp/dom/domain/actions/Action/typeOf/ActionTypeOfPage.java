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
package demoapp.dom.domain.actions.Action.typeOf;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.inject.Named;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.SemanticsOf;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.domain.actions.Action.typeOf.child.ActionTypeOfChildVm;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Named("demo.ActionTypeOfPage")
@DomainObject(nature=Nature.VIEW_MODEL)
@DomainObjectLayout(cssClassFa="fa-shapes")
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
//tag::class-collections-children[]
public class ActionTypeOfPage implements HasAsciiDocDescription {

//end::class-collections-children[]

    @ObjectSupport public String title() {
        return "@Action#typeOf";
    }

//tag::class-collections-children[]
    @Collection()
    @CollectionLayout()
    @Getter
    private List<ActionTypeOfChildVm> children = new ArrayList<>();

    // ...
//end::class-collections-children[]

//tag::action-returning-generic-list[]
    @Action(semantics = SemanticsOf.SAFE)
    public List<ActionTypeOfChildVm> find(final String value) {         // <.>
        return getChildren().stream()
                .filter(x -> x.getValue().contains(value))
                .collect(Collectors.toList());
    }
//end::action-returning-generic-list[]

//tag::action-returning-raw-list[]
    @Action(semantics = SemanticsOf.SAFE)
    public List findReturningRawList(final String value) {              // <.>
        return find(value);
    }
//end::action-returning-raw-list[]

//tag::action-returning-raw-list-but-annotated[]
    @Action(
            semantics = SemanticsOf.SAFE,
            typeOf = ActionTypeOfChildVm.class                          // <.>
    )
    public List findReturningRawListButAnnotated(final String value) {  // <.>
        return find(value);
    }
//end::action-returning-raw-list-but-annotated[]

//tag::class-collections-children[]

}
//end::class-collections-children[]
