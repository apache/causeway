/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.viewer.webcomponents.sample.htmx.petclinic.domain;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.HomePage;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.services.clock.ClockService;

@Named(PetClinicDomainModule.NAMESPACE + ".HomePage")
@DomainObject(nature = Nature.VIEW_MODEL)
@DomainObjectLayout
@HomePage
public class PetClinicHomePage {

    @Inject
    private PetOwners petOwners;

    @Inject
    private VisitRepository visitRepository;

    @Inject
    private ClockService clockService;

    @ObjectSupport
    public String title() {
        return getPetOwners().size() + " pet owners · " + getFutureVisits().size() + " upcoming visits";
    }

    @Collection
    @CollectionLayout(paged = 10)
    public List<PetOwner> getPetOwners() {
        return petOwners.listAll();
    }

    @Collection
    @CollectionLayout(paged = 10)
    public List<Visit> getFutureVisits() {
        final LocalDateTime now = clockService.getClock().nowAsLocalDateTime();
        return visitRepository.findByVisitAtAfter(now);
    }
}
