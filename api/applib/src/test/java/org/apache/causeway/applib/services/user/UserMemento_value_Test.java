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

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.internaltestsupport.contract.ValueTypeContractTestAbstract;

import lombok.SneakyThrows;

class UserMemento_value_Test extends ValueTypeContractTestAbstract<UserMemento> {

    private UserMemento.UserMementoBuilder baseline() {
        return
                UserMemento.builder().name("Joe")
                        .authenticationSource(UserMemento.AuthenticationSource.DEFAULT)
                        .authenticationCode("123")
                        .multiTenancyToken("/UK")
                        .roles(Can.of(
                                RoleMemento.builder().name("role-1").build(),
                                RoleMemento.builder().name("role-2").build()))
                        .impersonating(false)
                ;
    }

    @Override
    protected List<UserMemento> getObjectsWithSameValue() {
        return Arrays.asList(
                baseline().build(),
                baseline().realName("Joe Bloggs").build(),
                baseline().realName("Mary Suggs").build(),
                baseline().avatarUrl(newURL("https://joebloggs.net/avatar")).build(),
                baseline().avatarUrl(newURL("https://marysuggs.net/avatar")).build(),
                baseline().languageLocale(Locale.ENGLISH).build(),
                baseline().languageLocale(Locale.FRANCE).build(),
                baseline().timeFormatLocale(Locale.ENGLISH).build(),
                baseline().timeFormatLocale(Locale.FRANCE).build(),
                baseline().numberFormatLocale(Locale.ENGLISH).build(),
                baseline().numberFormatLocale(Locale.FRANCE).build()
        );
    }

    @Override
    protected List<UserMemento> getObjectsWithDifferentValue() {
        return Arrays.asList(
                baseline().name("mary").build(),
                baseline().authenticationSource(UserMemento.AuthenticationSource.EXTERNAL).build(),
                baseline().multiTenancyToken("/FR").build(),
                baseline().authenticationCode("456").build(),
                baseline().roles(Can.ofSingleton(RoleMemento.builder().name("role-3").build())).build()
        );
    }

    @SneakyThrows
    private static URL newURL(final String s) {
        return new URL(s);
    }

}
