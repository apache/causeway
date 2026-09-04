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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

import jakarta.inject.Named;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Navigable;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.value.Blob;

@Entity
@Table(name = "petclinic_visit")
@Named(PetClinicDomainModule.NAMESPACE + ".Visit")
@DomainObject
@DomainObjectLayout
public class Visit implements Comparable<Visit> {

    @Id
    @Column(nullable = false, length = 40)
    private String id;

    @Version
    @Column(nullable = false)
    @PropertyLayout(fieldSetId = "metadata", sequence = "999")
    private long version;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    @PropertyLayout(fieldSetId = "identity", sequence = "1", navigable = Navigable.PARENT)
    private Pet pet;

    @Column(nullable = false)
    @PropertyLayout(fieldSetId = "identity", sequence = "2")
    private LocalDateTime visitAt;

    @Property(editing = Editing.ENABLED)
    @Column(nullable = false, length = 120)
    @PropertyLayout(fieldSetId = "details", sequence = "1")
    private String reason;

    @Property(editing = Editing.ENABLED)
    @Column(length = 400)
    @PropertyLayout(fieldSetId = "details", sequence = "2")
    private String notes;

    protected Visit() {
    }

    public Visit(
            final String id,
            final Pet pet,
            final LocalDateTime visitAt,
            final String reason) {
        this.id = id;
        this.pet = pet;
        this.visitAt = visitAt;
        this.reason = reason;
    }

    @ObjectSupport
    public String title() {
        return pet.getName() + " · " + visitAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"));
    }

    public String getId() {
        return id;
    }

    public long getVersion() {
        return version;
    }

    public Pet getPet() {
        return pet;
    }

    public LocalDateTime getVisitAt() {
        return visitAt;
    }

    public void setVisitAt(final LocalDateTime visitAt) {
        this.visitAt = visitAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(final String reason) {
        this.reason = reason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(final String notes) {
        this.notes = notes;
    }

    @Property
    @PropertyLayout(fieldSetId = "documents", sequence = "1", named = "PDF resource link")
    public Blob getPdfLink() {
        return PetClinicPdfDocument.sample();
    }

    @Override
    public int compareTo(final Visit other) {
        return Comparator.comparing(Visit::getVisitAt).thenComparing(Visit::getId).compare(this, other);
    }
}
