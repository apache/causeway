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

import org.springframework.stereotype.Repository;

import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.applib.value.Blob;

@Repository
public class StaffMemberRepository {

    @Inject private RepositoryService repositoryService;

    public StaffMember create(final String name, final Department department) {
        return create(name, department, null);
    }
    public StaffMember create(final String name, final Department department, final Blob photo) {
        StaffMember staffMember = new StaffMember(name, department, Grade.LECTURER);
        staffMember.setPhoto(photo);
        department.new addStaffMember().act(staffMember);
        repositoryService.persistAndFlush(staffMember);
        return staffMember;
    }

    public List<StaffMember> findAll() {
        return repositoryService.allInstances(StaffMember.class).stream()
                .sorted()
                .collect(Collectors.toList());
    }

    public void removeAll(){
        repositoryService.removeAll(StaffMember.class);
    }

    public StaffMember findByName(final String name){
        return findAll().stream()
                .filter(dept -> dept.getName().equals(name))
                .sorted()
                .findFirst()
                .orElse(null);
    }

    public List<StaffMember> findByNameMatching(final String name){
        return findAll().stream()
                .filter(dept -> dept.getName().contains(name))
                .sorted()
                .collect(Collectors.toList());
    }

}
