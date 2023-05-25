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
package demoapp.dom.domain.objects.DomainObject.entityChangePublishing;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.repository.RepositoryService;

import demoapp.dom._infra.samples.NameSamples;
import demoapp.dom._infra.values.ValueHolderRepository;

import lombok.RequiredArgsConstructor;

//tag::class[]
@Action(semantics = SemanticsOf.NON_IDEMPOTENT)
@RequiredArgsConstructor
public class DomainObjectEntityChangePublishingPage_modify {

    private final DomainObjectEntityChangePublishingPage page;

//tag::act[]
    @MemberSupport public DomainObjectEntityChangePublishingPage act(
            final Integer howManyToCreate,
            final List<? extends DomainObjectEntityChangePublishingEntity> toUpdate,
            final List<? extends DomainObjectEntityChangePublishingEntity> toDelete
    ) {
        for(int i = 0; i < howManyToCreate; i++) {
            publishingEntities.create(nameSamples.random());
        }
        toUpdate.forEach(x -> x.setPropertyUpdatedByAction("Object #" + renumber.incrementAndGet()));
        toDelete.forEach(x -> repositoryService.remove(x));
        return page;
    }
//end::act[]
    @MemberSupport public Integer default0Act() {
        return 1;
    }
    @MemberSupport public List<Integer> choices0Act() {
        return Arrays.asList(1, 2, 3, 4, 5);
    }

    @MemberSupport public List<? extends DomainObjectEntityChangePublishingEntity> default1Act() {
        return publishingEntities.firstAsList();
    }
    @MemberSupport public List<? extends DomainObjectEntityChangePublishingEntity> choices1Act() {
        return publishingEntities.all();
    }

    @MemberSupport public List<? extends DomainObjectEntityChangePublishingEntity> default2Act() {
        return publishingEntities.firstAsList();
    }
    @MemberSupport public List<? extends DomainObjectEntityChangePublishingEntity> choices2Act() {
        return publishingEntities.all();
    }


    final static AtomicInteger renumber = new AtomicInteger(0);

    @Inject ValueHolderRepository<String, ? extends DomainObjectEntityChangePublishingEntity> publishingEntities;
    @Inject NameSamples nameSamples;
    @Inject RepositoryService repositoryService;
}
//end::class[]
