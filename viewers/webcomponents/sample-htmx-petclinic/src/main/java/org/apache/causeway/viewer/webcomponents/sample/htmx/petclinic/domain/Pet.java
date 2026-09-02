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

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import jakarta.inject.Named;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Navigable;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;

@Entity
@Table(name = "petclinic_pet")
@Named(PetClinicDomainModule.NAMESPACE + ".Pet")
@DomainObject
@DomainObjectLayout
public class Pet implements Comparable<Pet> {

    public static final class ByName implements Comparator<Pet> {
        @Override
        public int compare(final Pet left, final Pet right) {
            return left.compareTo(right);
        }
    }

    @Id
    @Column(nullable = false, length = 40)
    private String id;

    @Version
    @Column(nullable = false)
    @PropertyLayout(fieldSetId = "metadata", sequence = "999")
    private long version;

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    @PropertyLayout(fieldSetId = "identity", sequence = "1", navigable = Navigable.PARENT)
    private PetOwner petOwner;

    @Column(nullable = false, length = 40)
    @PropertyLayout(fieldSetId = "identity", sequence = "2")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @PropertyLayout(fieldSetId = "details", sequence = "1")
    private PetSpecies species;

    @Property(editing = Editing.ENABLED)
    @Column(length = 400)
    @PropertyLayout(fieldSetId = "details", sequence = "2")
    private String notes;

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Visit> visits = new TreeSet<>();

    protected Pet() {
    }

    public Pet(final String id, final PetOwner petOwner, final String name, final PetSpecies species) {
        this.id = id;
        this.petOwner = petOwner;
        this.name = name;
        this.species = species;
    }

    @ObjectSupport
    public String title() {
        return name + " · " + species.name().toLowerCase();
    }

    @ObjectSupport
    public String iconName() {
        return species.name().toLowerCase();
    }

    public String getId() {
        return id;
    }

    public long getVersion() {
        return version;
    }

    public PetOwner getPetOwner() {
        return petOwner;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public PetSpecies getSpecies() {
        return species;
    }

    public void setSpecies(final PetSpecies species) {
        this.species = species;
    }

    public String getNotes() {
        return notes;
    }

    @Collection
    @CollectionLayout
    public Set<Visit> getVisits() {
        return visits;
    }

    void addVisit(final Visit visit) {
        visits.add(visit);
    }

    void clearVisits() {
        visits.clear();
    }

    public void setNotes(final String notes) {
        this.notes = notes;
    }

    @Action
    public void clearNotes() {
        this.notes = null;
    }

    @Override
    public int compareTo(final Pet other) {
        return Comparator.comparing(Pet::getName).thenComparing(Pet::getId).compare(this, other);
    }
}
