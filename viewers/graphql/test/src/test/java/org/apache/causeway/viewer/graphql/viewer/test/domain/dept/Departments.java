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

import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

@Named("university.dept.Departments")
@DomainService
@jakarta.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class Departments {

    final DepartmentRepository departmentRepository;

    @Action(semantics = SemanticsOf.NON_IDEMPOTENT)
    public Department createDepartment(
            final String name,
            final @Nullable DeptHead deptHead
    ){
        return departmentRepository.create(name, deptHead);
    }

    @Action(semantics = SemanticsOf.SAFE)
    public List<Department> findAllDepartments(){
        return departmentRepository.findAll();
    }

    @Action(semantics = SemanticsOf.SAFE)
    public Department findDepartmentByName(final String name){
        return departmentRepository.findByName(name);
    }

}
