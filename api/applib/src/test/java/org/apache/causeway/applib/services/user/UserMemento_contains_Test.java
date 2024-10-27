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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.services.sudo.SudoService;
import org.apache.causeway.commons.collections.Can;

class UserMemento_contains_Test {

    @Test
    void when_contains_instance() {

        // given
        UserMemento userMemento = UserMemento.builder().name("foo").roles(Can.ofSingleton(SudoService.ACCESS_ALL_ROLE)).build();

        // when, then
        Assertions.assertThat(userMemento.getRoles()).contains(SudoService.ACCESS_ALL_ROLE);

    }

    @Test
    void when_contains_equivalent() {

        // given
        UserMemento userMemento = UserMemento.builder().name("foo").roles(Can.ofSingleton(SudoService.ACCESS_ALL_ROLE)).build();

        // when, then
        Assertions.assertThat(userMemento.getRoles()).contains(new RoleMemento(SudoService.ACCESS_ALL_ROLE.getName()));
    }

    @Test
    void when_does_not_contain() {

        // given
        UserMemento userMemento = UserMemento.builder().name("foo").build();

        // when, then
        Assertions.assertThat(userMemento.getRoles()).doesNotContain(SudoService.ACCESS_ALL_ROLE);
    }
}
