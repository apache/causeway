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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.ActionLayout;
import org.apache.isis.applib.annotations.Collection;
import org.apache.isis.applib.annotations.CollectionLayout;
import org.apache.isis.applib.annotations.DomainObject;
import org.apache.isis.applib.annotations.Nature;
import org.apache.isis.applib.annotations.ObjectSupport;
import org.apache.isis.applib.annotations.SemanticsOf;

import lombok.Getter;
import lombok.NoArgsConstructor;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.domain.actions.Action.typeOf.child.ActionTypeOfChildVm;

@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
    nature=Nature.VIEW_MODEL,
    logicalTypeName = "demo.ActionTypeOfVm"
)
@NoArgsConstructor
//tag::class-collections-children[]
public class ActionTypeOfVm implements HasAsciiDocDescription {

//end::class-collections-children[]

    @ObjectSupport public String title() {
        return "Action#typeOf";
    }

//tag::class-collections-children[]
    @Collection()
    @CollectionLayout()
    @Getter
    private List<ActionTypeOfChildVm> children = new ArrayList<>();

    // ...
//end::class-collections-children[]

//tag::action[]
    @Action(
            semantics = SemanticsOf.SAFE
            , typeOf = ActionTypeOfChildVm.class     // <.>
            , choicesFrom = "children"
    )
    @ActionLayout(
        describedAs =
            "@Action(typeOf = ActionTypeOfChildVm.class)"
        , sequence = "1"
    )
    public List find(final String value) {          // <.>
        return getChildren().stream()
                .filter(x -> x.getValue().contains(value))
                .collect(Collectors.toList());
    }
//end::action[]

//tag::action-no-annotation[]
    @Action(
            semantics = SemanticsOf.SAFE
            , choicesFrom = "children"
    )
    @ActionLayout(
        describedAs =
            "@Action()"
        , sequence = "2"
    )
    public List findButNoTypeOfAnnotation(final String value) {     // <.>
        return find(value);
    }
//end::action-no-annotation[]

//tag::class-collections-children[]

}
//end::class-collections-children[]
