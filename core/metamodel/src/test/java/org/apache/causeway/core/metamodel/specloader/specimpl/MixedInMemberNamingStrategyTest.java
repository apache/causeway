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
package org.apache.causeway.core.metamodel.specloader.specimpl;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.commons.internal.base._Strings;

class MixedInMemberNamingStrategyTest {

    @Test
    void deriveMemberNameFrom_with_underscore() {
        assertEquals(
                "placeOrder",
                _MixedInMemberNamingStrategy.deriveMemberNameFrom("Customer_placeOrder"));
    }

    @Test
    void deriveMemberNameFrom_with_dollar() {
        assertEquals(
                "placeOrder",
                _MixedInMemberNamingStrategy.deriveMemberNameFrom("Customer$placeOrder"));
    }

    @Test
    void exactly_underscore() throws Exception {
        final String s = _Strings.capitalize(_MixedInMemberNamingStrategy.deriveMemberNameFrom("_"));
        assertThat(s, is("_"));
    }

    @Test
    void ends_with_underscore() throws Exception {
        final String s = _Strings.capitalize(_MixedInMemberNamingStrategy.deriveMemberNameFrom("abc_"));
        assertThat(s, is("Abc_"));
    }

    @Test
    void has_no_underscore() throws Exception {
        final String s = _Strings.capitalize(_MixedInMemberNamingStrategy.deriveMemberNameFrom("defghij"));
        assertThat(s, is("Defghij"));
    }

    @Test
    void contains_one_underscore() throws Exception {
        final String s = _Strings.capitalize(_MixedInMemberNamingStrategy.deriveMemberNameFrom("abc_def"));
        assertThat(s, is("Def"));
    }

    @Test
    void contains_more_than_one_underscore() throws Exception {
        final String s = _Strings.capitalize(_MixedInMemberNamingStrategy.deriveMemberNameFrom("abc_def_ghi"));
        assertThat(s, is("Ghi"));
    }


}