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

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.ActionLayout;
import org.apache.isis.applib.annotations.SemanticsOf;

import lombok.RequiredArgsConstructor;
import lombok.val;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.domain.collections.Collection.domainEvent.child.CollectionDomainEventChildVm;

//tag::class[]
@Action(
    semantics = SemanticsOf.NON_IDEMPOTENT,
    choicesFrom = "children"
)
@ActionLayout(sequence = "1")
@RequiredArgsConstructor
public class CollectionDomainEventVm_addChild implements HasAsciiDocDescription {

    private final CollectionDomainEventVm collectionDomainEventVm;

    public CollectionDomainEventVm_addChild act() {
        val child = new CollectionDomainEventChildVm("Child #" + ++collectionDomainEventVm.lastChildNumberAdded);
        collectionDomainEventVm.getChildren().add(child);
        return this;
    }


}
//end::class[]
