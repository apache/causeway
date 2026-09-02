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
import java.time.LocalDateTime;

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

            final var george = owner("owner-george", "George Franklin", "George", "020 7946 1020", "george@example.com", "Prefers email reminders.", today.minusDays(62));
            final var ruby = pet("pet-ruby", george, "Ruby", PetSpecies.CAT, "Nervous around dogs");
            george.addSeedPet(ruby);
            ownerRepository.persist(george);

            final var betty = owner("owner-betty", "Betty Davis", null, "020 7946 1131", "betty@example.com", null, today.minusDays(25));
            final var archie = pet("pet-archie", betty, "Archie", PetSpecies.DOG, "Enjoys long walks");
            betty.addSeedPet(archie);
            ownerRepository.persist(betty);

            final var eduardo = owner("owner-eduardo", "Eduardo Rodriguez", "Eddie", "020 7946 1242", "eduardo@example.com", "Multi-pet household", today.minusDays(14));
            final var rosy = pet("pet-rosy", eduardo, "Rosy", PetSpecies.DOG, "Needs a quiet waiting area");
            final var jewel = pet("pet-jewel", eduardo, "Jewel", PetSpecies.CAT, "Indoor cat");
            final var iggy = pet("pet-iggy", eduardo, "Iggy", PetSpecies.LIZARD, "Requires a heated carrier");
            final var chester = pet("pet-chester", eduardo, "Chester", PetSpecies.HAMSTER, null);
            final var sly = pet("pet-sly", eduardo, "Sly", PetSpecies.SNAKE, "Experienced handler only");
            final var tico = pet("pet-tico", eduardo, "Tico", PetSpecies.BIRD, "Mimics telephone rings");
            eduardo.addSeedPet(rosy);
            eduardo.addSeedPet(jewel);
            eduardo.addSeedPet(iggy);
            eduardo.addSeedPet(chester);
            eduardo.addSeedPet(sly);
            eduardo.addSeedPet(tico);
            ownerRepository.persist(eduardo);

            final var jean = owner("owner-jean", "Jean Coleman", "Jean", "020 7946 1353", "jean@example.com", "Weekend appointments preferred.", today.minusDays(33));
            final var shadow = pet("pet-shadow", jean, "Shadow", PetSpecies.CAT, null);
            jean.addSeedPet(shadow);
            ownerRepository.persist(jean);

            final var jeff = owner("owner-jeff", "Jeff Black", null, "020 7946 1464", "jeff@example.com", null, today.minusDays(12));
            final var lucky = pet("pet-lucky", jeff, "Lucky", PetSpecies.BIRD, "Recovering flight feathers");
            jeff.addSeedPet(lucky);
            ownerRepository.persist(jeff);

            final var maria = owner("owner-maria", "Maria Escobito", "Maria", "020 7946 1575", "maria@example.com", "Requires step-free access.", today.minusDays(50));
            final var mulligan = pet("pet-mulligan", maria, "Mulligan", PetSpecies.DOG, "Senior dog");
            maria.addSeedPet(mulligan);
            ownerRepository.persist(maria);

            visit(visitRepository, "visit-basil-checkup", basil, today.plusDays(1).atTime(9, 30), "Annual check-up", null);
            visit(visitRepository, "visit-samantha-vaccine", samantha, today.plusDays(3).atTime(11, 0), "Vaccination", "Bring vaccination record");
            visit(visitRepository, "visit-max-followup", max, today.plusDays(7).atTime(14, 15), "Diet follow-up", null);

            visit(visitRepository, "visit-rosy-dental-history", rosy, today.minusDays(60).atTime(10, 0), "Dental treatment", "Routine recovery");
            visit(visitRepository, "visit-jewel-checkup-history", jewel, today.minusDays(45).atTime(13, 30), "Annual check-up", null);
            visit(visitRepository, "visit-iggy-skin-history", iggy, today.minusDays(28).atTime(15, 0), "Skin examination", "Humidity reviewed");
            visit(visitRepository, "visit-chester-nail-history", chester, today.minusDays(14).atTime(9, 15), "Nail trim", null);
            visit(visitRepository, "visit-rosy-followup", rosy, today.plusDays(2).atTime(10, 15), "Dental follow-up", null);
            visit(visitRepository, "visit-jewel-dental", jewel, today.plusDays(4).atTime(9, 0), "Dental examination", null);
            visit(visitRepository, "visit-iggy-wellness", iggy, today.plusDays(5).atTime(15, 30), "Wellness review", "Check enclosure temperature");
            visit(visitRepository, "visit-chester-checkup", chester, today.plusDays(8).atTime(11, 15), "Annual check-up", null);
            visit(visitRepository, "visit-sly-nutrition", sly, today.plusDays(10).atTime(13, 0), "Nutrition review", null);
            visit(visitRepository, "visit-tico-beak", tico, today.plusDays(12).atTime(16, 0), "Beak examination", null);
            visit(visitRepository, "visit-rosy-vaccine", rosy, today.plusDays(15).atTime(8, 45), "Vaccination", null);

            visit(visitRepository, "visit-ruby-wellness", ruby, today.plusDays(6).atTime(10, 45), "Wellness review", null);
            visit(visitRepository, "visit-archie-physio", archie, today.plusDays(9).atTime(14, 0), "Mobility assessment", "Observe gait after exercise");
            visit(visitRepository, "visit-shadow-checkup", shadow, today.plusDays(11).atTime(9, 45), "Annual check-up", null);
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

    private static void visit(
            final VisitRepository repository,
            final String id,
            final Pet pet,
            final LocalDateTime visitAt,
            final String reason,
            final String notes) {
        final var visit = new Visit(id, pet, visitAt, reason);
        visit.setNotes(notes);
        repository.persist(visit);
    }
}
