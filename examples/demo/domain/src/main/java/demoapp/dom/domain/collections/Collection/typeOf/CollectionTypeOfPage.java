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
package demoapp.dom.domain.collections.Collection.typeOf;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.domain.collections.Collection.typeOf.child.CollectionTypeOfChildVm;
import lombok.NoArgsConstructor;

@Named("demo.CollectionTypeOfPage")
@DomainObject(nature=Nature.VIEW_MODEL)
@DomainObjectLayout(cssClassFa="fa-shapes")
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
public class CollectionTypeOfPage implements HasAsciiDocDescription {

    @ObjectSupport
    public String title() {
        return "@Collection#typeOf";
    }

//tag::class-collections-children[]
    private List<CollectionTypeOfChildVm> children = new ArrayList<>();

    @Collection(typeOf = CollectionTypeOfChildVm.class)     // <.>
    @CollectionLayout()
    public List getChildren() {                             // <.>
        return children;
    }
//end::class-collections-children[]

//tag::class-collections-other-children[]
    private List<CollectionTypeOfChildVm> otherChildren = new ArrayList<>();

    @Collection()                                           // <.>
    @CollectionLayout()
    public List getOtherChildren() {                        // <.>
        return otherChildren;
    }
//end::class-collections-other-children[]

}
