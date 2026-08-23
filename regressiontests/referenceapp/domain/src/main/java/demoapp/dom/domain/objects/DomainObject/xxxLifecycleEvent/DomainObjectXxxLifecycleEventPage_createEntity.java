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
package demoapp.dom.domain.objects.DomainObject.xxxLifecycleEvent;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.SemanticsOf;

import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.types.Samples;
import lombok.RequiredArgsConstructor;

//tag::class[]
@Action(semantics = SemanticsOf.NON_IDEMPOTENT)
@RequiredArgsConstructor
public class DomainObjectXxxLifecycleEventPage_createEntity {

    private final DomainObjectXxxLifecycleEventPage page;

    @MemberSupport public DomainObjectXxxLifecycleEventPage act(final String text) {
        var entity = objectRepository.create(text);
        page.setEntity(entity);
        return page;
    }
    @MemberSupport public String default0Act() {
        return samples.single();
    }
    @MemberSupport public String disableAct() {
        return page.getEntity() != null ? "Entity already created" : null;
    }

    @Inject ValueHolderRepository<String, ? extends DomainObjectXxxLifecycleEventEntity> objectRepository;
    @Inject Samples<String> samples;
}
//end::class[]
