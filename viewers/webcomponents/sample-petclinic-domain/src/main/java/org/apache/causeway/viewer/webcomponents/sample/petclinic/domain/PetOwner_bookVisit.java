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
package org.apache.causeway.viewer.webcomponents.sample.petclinic.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    private static final LocalTime OFFICE_OPENS = LocalTime.of(8, 0);
    private static final LocalTime OFFICE_CLOSES = LocalTime.of(17, 0);

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
            final LocalDate visitDate,
            final LocalTime visitTime,
            @Parameter(maxLength = 120) final String reason) {
        final var visitAt = LocalDateTime.of(visitDate, visitTime);
        final var visit = new Visit("v-" + UUID.randomUUID(), pet, visitAt, reason);
        pet.addVisit(visit);
        visitRepository.persist(visit);
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
    public LocalDate default1Act() {
        return nextOfficeDate();
    }

    @MemberSupport
    public LocalTime default2Act() {
        return LocalTime.of(9, 0);
    }

    @MemberSupport
    public String default3Act() {
        return "Routine check-up";
    }

    @MemberSupport
    public String validate1Act(final LocalDate visitDate) {
        return visitDate != null && !visitDate.isAfter(clockService.getClock().nowAsLocalDate())
                ? "The visit date must be in the future."
                : null;
    }

    @MemberSupport
    public String validate2Act(final LocalTime visitTime) {
        return visitTime != null && (visitTime.isBefore(OFFICE_OPENS) || visitTime.isAfter(OFFICE_CLOSES))
                ? "The visit time must be between 08:00 and 17:00."
                : null;
    }

    private LocalDate nextOfficeDate() {
        return clockService.getClock().nowAsLocalDate().plusDays(1);
    }
}
