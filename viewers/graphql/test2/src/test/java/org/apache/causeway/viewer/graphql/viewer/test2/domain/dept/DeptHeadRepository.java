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

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import org.apache.causeway.applib.services.repository.RepositoryService;

@Repository
public class DeptHeadRepository {

    @Inject private RepositoryService repositoryService;

    public DeptHead create(final String name, @Nullable final Department department) {
        DeptHead deptHead = new DeptHead(name);
        if (department != null) {
            deptHead.setDepartment(department);
            department.setDeptHead(deptHead);
        }
        repositoryService.persistAndFlush(deptHead);
        return deptHead;
    }

    public List<DeptHead> findAll() {
        return repositoryService.allInstances(DeptHead.class).stream()
                .sorted()
                .collect(Collectors.toList());
    }

    public void removeAll(){
        repositoryService.removeAll(DeptHead.class);
    }

    public DeptHead findByName(final String name){
        return findAll().stream()
                .filter(deptHead -> deptHead.getName().equals(name))
                .sorted()
                .findFirst()
                .orElse(null);
    }

    public List<DeptHead> findByNameContaining(final String name){
        return findAll().stream()
                .filter(deptHead -> deptHead.getName().contains(name))
                .sorted()
                .collect(Collectors.toList());
    }

}
