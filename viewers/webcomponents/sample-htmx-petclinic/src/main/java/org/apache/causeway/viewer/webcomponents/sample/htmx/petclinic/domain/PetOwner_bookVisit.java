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
import java.util.Set;
import java.util.UUID;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.services.clock.ClockService;

@Action
@ActionLayout(associateWith = "visits")
public class PetOwner_bookVisit {

    private final PetOwner petOwner;

    @Inject
    private ClockService clockService;

    @Inject
    private VisitRepository visitRepository;

    public PetOwner_bookVisit(final PetOwner petOwner) {
        this.petOwner = petOwner;
    }

    @MemberSupport
    public PetOwner act(
            final Pet pet,
            final LocalDateTime visitAt,
            @Parameter(maxLength = 120) final String reason) {
        visitRepository.persist(new Visit("visit-" + UUID.randomUUID(), pet, visitAt, reason));
        return petOwner;
    }

    @MemberSupport
    public Set<Pet> choices0Act() {
        return petOwner.getPets();
    }

    @MemberSupport
    public Pet default0Act() {
        return petOwner.getPets().size() == 1 ? petOwner.getPets().iterator().next() : null;
    }

    @MemberSupport
    public LocalDateTime default1Act() {
        return nextOfficeOpening();
    }

    @MemberSupport
    public String default2Act() {
        return "Routine check-up";
    }

    @MemberSupport
    public String validate1Act(final LocalDateTime visitAt) {
        return visitAt != null && visitAt.isBefore(nextOfficeOpening())
                ? "The visit must be booked in the future."
                : null;
    }

    private LocalDateTime nextOfficeOpening() {
        return clockService.getClock().nowAsLocalDate().plusDays(1).atTime(9, 0);
    }
}
