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
package org.apache.causeway.viewer.graphql.viewer.test2.domain.dept;

import java.util.Comparator;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.persistence.jpa.applib.types.BlobJpaEmbeddable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        schema = "public",
        name = "StaffMember"
)
@javax.inject.Named("university.dept.StaffMember")
@DomainObject(nature = Nature.ENTITY, autoCompleteRepository = StaffMemberRepository.class, autoCompleteMethod = "findByNameMatching")
@DomainObjectLayout(describedAs = "Staff member of a university department, responsible for delivering lectures, tutorials, exam invigilation and candidate interviews")
@NoArgsConstructor
public class StaffMember extends Person implements Comparable<StaffMember> {

    public StaffMember(
            final String name,
            final Department department,
            final Grade grade) {
        this.name = name;
        this.department = department;
        this.grade = grade;
    }

    @Id
    @GeneratedValue
    private Long id;

    @Getter @Setter
    @Property(editing = Editing.ENABLED)
    private String name;
    public String validateName(String proposedName) {
        if(proposedName.contains("!")) {
            return "Name cannot contain '!' character";
        }
        return null;
    }

    @Getter @Setter
    @Property
    @ManyToOne(optional = false)
    @JoinColumn(name = "department_id")
    private Department department;

    @Getter @Setter
    @Property(editing = Editing.ENABLED)
    private Grade grade;

    @AttributeOverrides({
            @AttributeOverride(name="name",    column=@Column(name="photo_name")),
            @AttributeOverride(name="mimeType",column=@Column(name="photo_mimeType")),
            @AttributeOverride(name="bytes",   column=@Column(name="photo_bytes"))
    })
    @Embedded
    private BlobJpaEmbeddable photo;

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(fieldSetId = "content", sequence = "1")
    public Blob getPhoto() {
        return BlobJpaEmbeddable.toBlob(photo);
    }
    public void setPhoto(final Blob photo) {
        this.photo = BlobJpaEmbeddable.fromBlob(photo);
    }

    @Override
    public int compareTo(final StaffMember o) {
        return Comparator.comparing(StaffMember::getName).compare(this, o);
    }
}
