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
package org.apache.causeway.viewer.graphql.viewer.test.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.Bounding;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.SemanticsOf;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Entity
@Table(
        schema = "public",
        name = "Department"
)
@javax.inject.Named("university.dept.Department")
@NoArgsConstructor
@DomainObject(nature = Nature.ENTITY, bounding = Bounding.BOUNDED)
public class Department implements Comparable<Department> {

    public Department(String name, DeptHead deptHead) {
        this.name = name;
        this.deptHead = deptHead;
    }

    @Id
    @GeneratedValue
    private Long id;

    @Getter @Setter
    private String name;
    @Action(semantics = SemanticsOf.IDEMPOTENT)
    public class changeName {

        public Department act(final String newName){
            setName(newName);
            return Department.this;
        }

        public String default0Act(){
            return getName();
        }

        public String validate0Act(String name) {
            if (name.contains("!")) {
                return "Name cannot contain '!' character";
            }
            return null;
        }
    }



    @Getter @Setter
    @Property
    @OneToOne(optional = true)
    @JoinColumn(name = "deptHead_id")
    private DeptHead deptHead;


    @OneToMany(mappedBy = "department")
    private Set<StaffMember> staffMembers = new TreeSet<>();

    // because the ordering seems not to be deterministic?
    @Collection
    public List<StaffMember> getStaffMembers() {
        return staffMembers.stream().sorted().collect(Collectors.toList());
    }

    @Action(semantics = SemanticsOf.IDEMPOTENT)
    @ActionLayout(associateWith = "staffMembers")
    public class addStaffMember {

        public Department act(StaffMember staffMember) {
            val department = Department.this;

            department.staffMembers.add(staffMember);
            staffMember.setDepartment(department);
            return department;
        }
    }

    @Action(semantics = SemanticsOf.IDEMPOTENT)
    @ActionLayout(associateWith = "staffMembers")
    @RequiredArgsConstructor
    public class removeStaffMember {

        public Department act(StaffMember staffMember) {
            val department = Department.this;

            department.getStaffMembers().add(staffMember);
            staffMember.setDepartment(department);
            return department;
        }
        public List<StaffMember> choices0Act() {
            val department = Department.this;
            return department.getStaffMembers()
                        .stream()
                        .sorted(Comparator.comparing(StaffMember::getName))
                        .collect(Collectors.toList());
        }
    }

    @Override
    public int compareTo(final Department o) {
        return Comparator.comparing(Department::getName).compare(this, o);
    }
}
