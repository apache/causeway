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

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.Bounding;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.SemanticsOf;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        schema = "public",
        name = "DeptHead"
)
@javax.inject.Named("gql.test.domain.DeptHead")
@DomainObject(nature = Nature.ENTITY, bounding = Bounding.BOUNDED)
public class DeptHead implements WithName {

    @Id
    @GeneratedValue
    private Long id;

    @Getter @Setter
    @Column(unique=true)
    private String name;

    @Getter @Setter
    @Property
    @OneToOne(optional = true)
    @JoinColumn(name = "department_id")
    private Department department;


    @Action(semantics = SemanticsOf.IDEMPOTENT)
    public class changeName {

        public DeptHead act(final String newName){
            setName(newName);
            return DeptHead.this;
        }

        public String default0Act(){
            return getName();
        }
    }


    @Action(semantics = SemanticsOf.IDEMPOTENT)
    public class changeDeptHead {

        public DeptHead act(final Department department){
            setDepartment(department);
            return DeptHead.this;
        }

        public List<Department> choices0Act(){
            return departmentRepository.findAll().stream().
                    filter(d -> d != getDepartment()).
                    collect(Collectors.toList());
        }

        public String validateAct(final Department department){
            if (getDepartment() == department) return "Already there";
            return null;
        }
    }


    @Inject @Transient DepartmentRepository departmentRepository;

}
