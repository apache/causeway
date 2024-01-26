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
package org.apache.causeway.viewer.graphql.viewer.test.domain.dept;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Comparator;

import javax.persistence.*;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Property;

@Entity
@Table(
        schema = "public",
        name = "StaffMember"
)
@javax.inject.Named("university.dept.StaffMember")
@NoArgsConstructor
@DomainObject(nature = Nature.ENTITY, autoCompleteRepository = StaffMemberRepository.class, autoCompleteMethod = "findByNameMatching")
public class StaffMember implements Comparable<StaffMember> {

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


    @Override
    public int compareTo(final StaffMember o) {
        return Comparator.comparing(StaffMember::getName).compare(this, o);
    }
}
