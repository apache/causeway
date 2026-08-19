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

import java.time.LocalDate;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.xactn.TransactionService;

@Configuration
public class PetClinicDataConfiguration {

    @Bean
    ApplicationRunner loadPetClinicData(
            final TransactionService transactionService,
            final ClockService clockService,
            final PetOwnerRepository ownerRepository,
            final VisitRepository visitRepository) {
        return args -> transactionService.runTransactional(Propagation.REQUIRED, () -> {
            if (ownerRepository.findById(PetOwner.MARY_ID) != null) {
                return;
            }
            final LocalDate today = clockService.getClock().nowAsLocalDate();

            final var mary = owner("owner-mary", "Mary Smith", "Mary", "020 7946 0312", "mary@example.com", "Prefers morning appointments.", today.minusDays(18));
            final var basil = pet("pet-basil", mary, "Basil", PetSpecies.DOG, "Friendly terrier");
            final var samantha = pet("pet-samantha", mary, "Samantha", PetSpecies.CAT, "Indoor cat");
            mary.addSeedPet(basil);
            mary.addSeedPet(samantha);
            ownerRepository.persist(mary);

            final var james = owner("owner-james", "James Carter", "Jim", "020 7946 0544", "james@example.com", "Call before booking.", today.minusDays(41));
            final var leo = pet("pet-leo", james, "Leo", PetSpecies.HAMSTER, "Very small carrier");
            james.addSeedPet(leo);
            ownerRepository.persist(james);

            final var helen = owner("owner-helen", "Helen Leary", null, "020 7946 0780", "helen@example.com", null, today.minusDays(7));
            final var max = pet("pet-max", helen, "Max", PetSpecies.DOG, "Allergic to chicken");
            helen.addSeedPet(max);
            ownerRepository.persist(helen);

            final var peter = owner("owner-peter", "Peter McTavish", "Pete", "020 7946 0911", "peter@example.com", "New client", null);
            final var tweety = pet("pet-tweety", peter, "Tweety", PetSpecies.BIRD, null);
            peter.addSeedPet(tweety);
            ownerRepository.persist(peter);

            visitRepository.persist(new Visit("visit-basil-checkup", basil, today.plusDays(1).atTime(9, 30), "Annual check-up"));
            visitRepository.persist(new Visit("visit-samantha-vaccine", samantha, today.plusDays(3).atTime(11, 0), "Vaccination"));
            visitRepository.persist(new Visit("visit-max-followup", max, today.plusDays(7).atTime(14, 15), "Diet follow-up"));
        });
    }

    private static PetOwner owner(
            final String id,
            final String name,
            final String knownAs,
            final String phone,
            final String email,
            final String notes,
            final LocalDate lastVisit) {
        final var owner = new PetOwner(id, name);
        owner.setKnownAs(knownAs);
        owner.setTelephoneNumber(phone);
        owner.setEmailAddress(email);
        owner.setNotes(notes);
        owner.setLastVisit(lastVisit);
        return owner;
    }

    private static Pet pet(
            final String id,
            final PetOwner owner,
            final String name,
            final PetSpecies species,
            final String notes) {
        final var pet = new Pet(id, owner, name, species);
        pet.setNotes(notes);
        return pet;
    }
}
