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
package org.apache.causeway.viewer.graphql.viewer.test.source.gqltestdomain;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

@Named("gqltestdomain.GQLTestDomainMenu")
@DomainService(
        nature=NatureOfService.VIEW)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class GQLTestDomainMenu {

    @Action(semantics = SemanticsOf.NON_IDEMPOTENT)
    public E1 createE1(final String name, @Nullable final E2 e2){
        return testEntityRepository.createE1(name, e2);
    }

    @Action(semantics = SemanticsOf.SAFE)
    public List<E1> findAllE1(){
        return testEntityRepository.findAllE1();
    }

    @Action(semantics = SemanticsOf.SAFE)
    public List<E2> findAllE2(){
        return testEntityRepository.findAllE2();
    }

    @Action(semantics = SemanticsOf.SAFE)
    public List<TestEntity> findAllTestEntities(){
        return testEntityRepository.findAllTestEntities();
    }

    @Action(semantics = SemanticsOf.SAFE)
    public E2 findE2(final String name){
        return testEntityRepository.findE2ByName(name);
    }

    @Inject
    TestEntityRepository testEntityRepository;

}
