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
package demoapp.dom.domain.collections.Collection.domainEvent;

import java.util.List;
import java.util.Objects;

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.ActionLayout;
import org.apache.isis.applib.annotations.MemberSupport;
import org.apache.isis.applib.annotations.SemanticsOf;

import lombok.RequiredArgsConstructor;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.domain.collections.Collection.domainEvent.child.CollectionDomainEventChildVm;

//tag::class[]
@Action(
    semantics = SemanticsOf.IDEMPOTENT,
    choicesFrom = "children"
)
@ActionLayout(sequence = "2")
@RequiredArgsConstructor
public class CollectionDomainEventVm_removeChild implements HasAsciiDocDescription {

    private final CollectionDomainEventVm collectionDomainEventVm;

    private List<CollectionDomainEventChildVm> getChildren() {
        return collectionDomainEventVm.getChildren();
    }

    @MemberSupport public CollectionDomainEventVm_removeChild act(final CollectionDomainEventChildVm child) {
        getChildren().removeIf(
                x -> Objects.equals(x.getValue(), child.getValue()));
        return this;
    }

    @MemberSupport public CollectionDomainEventChildVm default0Act() {
        return getChildren().isEmpty() ? null : getChildren().get(0);
    }
    @MemberSupport public List<CollectionDomainEventChildVm> choices0Act() {
        return getChildren();
    }
    @MemberSupport public String disableAct() {
        return getChildren().isEmpty() ? "No children to remove": null;
    }

}
//end::class[]
