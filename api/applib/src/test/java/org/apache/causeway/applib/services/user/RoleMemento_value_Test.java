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
package org.apache.causeway.applib.services.user;

import java.util.Arrays;
import java.util.List;

import org.apache.causeway.applib.services.sudo.SudoService;
import org.apache.causeway.core.internaltestsupport.contract.ValueTypeContractTestAbstract;

class RoleMemento_value_Test extends ValueTypeContractTestAbstract<RoleMemento> {

    @Override
    protected List<RoleMemento> getObjectsWithSameValue() {
        return Arrays.asList(SudoService.ACCESS_ALL_ROLE,
                new RoleMemento(SudoService.ACCESS_ALL_ROLE.name(), SudoService.ACCESS_ALL_ROLE.description()),
                new RoleMemento(SudoService.ACCESS_ALL_ROLE.name(), SudoService.ACCESS_ALL_ROLE.description()),
                new RoleMemento(SudoService.ACCESS_ALL_ROLE.name(), ""),
                new RoleMemento(SudoService.ACCESS_ALL_ROLE.name())
        );
    }

    @Override
    protected List<RoleMemento> getObjectsWithDifferentValue() {
        return Arrays.asList(
                new RoleMemento(SudoService.ACCESS_ALL_ROLE.name() + "x"),
                new RoleMemento(SudoService.ACCESS_ALL_ROLE.name() + "y")
        );
    }
}
