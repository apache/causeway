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

import java.util.List;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;

@Named(PetClinicDomainModule.NAMESPACE + ".PetOwners")
@DomainService
@Priority(PriorityPrecedence.EARLY)
public class PetOwners {

    private final PetOwnerRepository repository;

    @Inject
    public PetOwners(final PetOwnerRepository repository) {
        this.repository = repository;
    }

    @Action(semantics = SemanticsOf.NON_IDEMPOTENT)
    @ActionLayout(describedAs = "Registers a new pet owner.", cssClassFa = "user-plus")
    public PetOwner create(
            @Parameter(maxLength = 80) final String name,
            @Parameter(maxLength = 40, optionality = Optionality.OPTIONAL) final String knownAs,
            @Parameter(maxLength = 40, optionality = Optionality.OPTIONAL) final String telephoneNumber,
            @Parameter(maxLength = 120, optionality = Optionality.OPTIONAL) final String emailAddress) {
        final var owner = new PetOwner("owner-" + (repository.count() + 1), name);
        owner.setKnownAs(knownAs);
        owner.setTelephoneNumber(telephoneNumber);
        owner.setEmailAddress(emailAddress);
        return repository.persist(owner);
    }

    @Action(semantics = SemanticsOf.SAFE, typeOf = PetOwner.class)
    @ActionLayout(describedAs = "Finds owners whose names contain the search text.", cssClassFa = "magnifying-glass")
    public List<PetOwner> findByName(@Parameter(maxLength = 80) final String name) {
        return repository.findByNameContaining(name);
    }

    @Action(semantics = SemanticsOf.SAFE, typeOf = PetOwner.class)
    @ActionLayout(describedAs = "Finds owners using the demonstration name search.", cssClassFa = "magnifying-glass")
    public List<PetOwner> findByNameLike(@Parameter(maxLength = 80) final String name) {
        return repository.findByNameContaining(name);
    }

    @Action(semantics = SemanticsOf.SAFE, typeOf = PetOwner.class)
    @ActionLayout(describedAs = "Lists every registered pet owner.", cssClassFa = "users")
    public List<PetOwner> listAll() {
        return repository.findAll();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(describedAs = "Counts the registered pet owners.", cssClassFa = "calculator")
    public long count() {
        return repository.count();
    }
}
