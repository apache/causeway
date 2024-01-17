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
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Property;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//@Profile("demo-jpa")
@Entity
@Table(
        schema = "public",
        name = "Department"
)
@javax.inject.Named("gql.test.domain.Department")
@NoArgsConstructor
@DomainObject(nature = Nature.ENTITY)
public class Department implements WithName, Comparable {

    public Department(String name, DeptHead deptHead) {
        this.name = name;
        this.deptHead = deptHead;
    }

    @Id
    @GeneratedValue
    private Long id;

    @Getter @Setter
    private String name;

    @Getter @Setter
    @Property
    @OneToOne(optional = true)
    @JoinColumn(name = "employee_id")
    private DeptHead deptHead;

    @Override
    public int compareTo(final Object o) {
        Department department = (Department) o;
        return this.getName().compareTo(department.getName());
    }
}
