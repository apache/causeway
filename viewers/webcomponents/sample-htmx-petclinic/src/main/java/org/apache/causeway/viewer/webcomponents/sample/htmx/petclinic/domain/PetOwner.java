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
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.BookmarkPolicy;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.LabelPosition;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.applib.services.title.TitleService;

@Entity
@Table(name = "petclinic_pet_owner")
@Named(PetClinicDomainModule.NAMESPACE + ".PetOwner")
@DomainObject
@DomainObjectLayout(bookmarking = BookmarkPolicy.AS_ROOT)
public class PetOwner implements Comparable<PetOwner> {

    public static final String MARY_ID = "owner-mary";

    @Id
    @Column(nullable = false, length = 40)
    private String id;

    @Version
    @Column(nullable = false)
    @PropertyLayout(fieldSetId = "metadata", sequence = "999")
    private long version;

    @Column(nullable = false, unique = true, length = 80)
    @PropertyLayout(fieldSetId = "identity", sequence = "1")
    private String name;

    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)
    @Column(length = 40)
    @PropertyLayout(fieldSetId = "identity", sequence = "2")
    private String knownAs;

    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)
    @Column(length = 40)
    @PropertyLayout(fieldSetId = "contact", sequence = "1",
            describedAs = "Primary telephone number for appointment contact.")
    private String telephoneNumber;

    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)
    @Column(length = 120)
    @PropertyLayout(fieldSetId = "contact", sequence = "2",
            describedAs = "Email address used for appointment reminders.",
            labelPosition = LabelPosition.TOP)
    private String emailAddress;

    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)
    @Column(length = 400)
    @PropertyLayout(fieldSetId = "details", sequence = "1", multiLine = 5,
            describedAs = "Additional notes about this pet owner.")
    private String notes;

    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)
    @Column
    @PropertyLayout(fieldSetId = "details", sequence = "2")
    private LocalDate lastVisit;

    @Collection
    @CollectionLayout(sortedBy = Pet.ByName.class)
    @OneToMany(mappedBy = "petOwner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Pet> pets = new TreeSet<>();

    @Inject
    @Transient
    private ClockService clockService;

    @Inject
    @Transient
    private RepositoryService repositoryService;

    @Inject
    @Transient
    private TitleService titleService;

    @Inject
    @Transient
    private MessageService messageService;

    protected PetOwner() {
    }

    public PetOwner(final String id, final String name) {
        this.id = id;
        this.name = name;
    }

    @ObjectSupport
    public String title() {
        return name + (knownAs == null || knownAs.isBlank() ? "" : " (" + knownAs + ")");
    }

    public String getId() {
        return id;
    }

    public long getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getKnownAs() {
        return knownAs;
    }

    public void setKnownAs(final String knownAs) {
        this.knownAs = knownAs;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(final String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(final String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(final String notes) {
        this.notes = notes;
    }

    public LocalDate getLastVisit() {
        return lastVisit;
    }

    public void setLastVisit(final LocalDate lastVisit) {
        this.lastVisit = lastVisit;
    }

    public Set<Pet> getPets() {
        return pets;
    }

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(fieldSetId = "details", sequence = "3")
    public Long getDaysSinceLastVisit() {
        return lastVisit == null || clockService == null
                ? null
                : ChronoUnit.DAYS.between(lastVisit, clockService.getClock().nowAsLocalDate());
    }

    @Action
    @ActionLayout(associateWith = "pets", sequence = "1")
    public PetOwner addPet(
            @Parameter(maxLength = 40) final String name,
            final PetSpecies species) {
        final var pet = new Pet("pet-" + UUID.randomUUID(), this, name, species);
        pets.add(pet);
        return this;
    }

    @MemberSupport
    public String validate0AddPet(final String candidateName) {
        return pets.stream().anyMatch(pet -> Objects.equals(pet.getName(), candidateName))
                ? "This owner already has a pet called '" + candidateName + "'."
                : null;
    }

    @Action(choicesFrom = "pets")
    @ActionLayout(associateWith = "pets", sequence = "2")
    public PetOwner removePet(final Pet pet) {
        pet.clearVisits();
        pets.remove(pet);
        return this;
    }

    @MemberSupport
    public Pet default0RemovePet() {
        return getPets().size() == 1 ? getPets().iterator().next() : null;
    }

    @Action(semantics = SemanticsOf.IDEMPOTENT)
    @ActionLayout(describedAs = "Updates the owner's full name.")
    public PetOwner updateName(@Parameter(maxLength = 80) final String name) {
        setName(name);
        return this;
    }

    @MemberSupport
    public String default0UpdateName() {
        return name;
    }

    @MemberSupport
    public String validate0UpdateName(final String candidateName) {
        if (candidateName == null || candidateName.isBlank()) {
            return "A name is required.";
        }
        return candidateName.matches(".*[!&%].*") ? "The name cannot contain !, &, or %." : null;
    }

    @Action(semantics = SemanticsOf.NON_IDEMPOTENT_ARE_YOU_SURE)
    @ActionLayout(describedAs = "Deletes this pet owner and their related pets and visits.")
    public void delete() {
        final var title = titleService.titleOf(this);
        repositoryService.removeAndFlush(this);
        messageService.informUser("'" + title + "' deleted");
    }

    void addSeedPet(final Pet pet) {
        pets.add(pet);
    }

    @Override
    public int compareTo(final PetOwner other) {
        return Comparator.comparing(PetOwner::getName).thenComparing(PetOwner::getId).compare(this, other);
    }
}
