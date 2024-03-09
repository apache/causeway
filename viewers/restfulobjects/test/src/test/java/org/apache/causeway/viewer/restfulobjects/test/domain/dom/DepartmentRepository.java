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
package org.apache.causeway.viewer.restfulobjects.test.domain.dom;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import org.apache.causeway.applib.services.repository.RepositoryService;

@Repository
public class DepartmentRepository {

    @Inject private RepositoryService repositoryService;

    public Department create(final String name, @Nullable final DeptHead deptHead) {
        Department department = new Department(name, deptHead);
        repositoryService.persistAndFlush(department);
        return department;
    }

    public List<Department> findAll() {
        return repositoryService.allInstances(Department.class).stream()
                .sorted()
                .collect(Collectors.toList());
    }

    public void removeAll(){
        repositoryService.removeAll(Department.class);
    }

    public Department findByName(final String name){
        return findAll().stream()
                .filter(dept -> dept.getName().equals(name))
                .sorted()
                .findFirst()
                .orElse(null);
    }

}
