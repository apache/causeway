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
package org.apache.isis.core.metamodel.specloader.specimpl;

import org.junit.Test;

import org.apache.isis.core.metamodel.commons.StringExtensions;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ObjectActionMixedInTest {


    public static class SuffixAfterUnderscore extends ObjectActionMixedInTest {

        @Test
        public void exactly_underscore() throws Exception {
            final String s = StringExtensions.asCapitalizedName(ObjectMemberAbstract.deriveMemberNameFrom("_"));
            assertThat(s, is("_"));
        }

        @Test
        public void ends_with_underscore() throws Exception {
            final String s = StringExtensions.asCapitalizedName(ObjectMemberAbstract.deriveMemberNameFrom("abc_"));
            assertThat(s, is("Abc_"));
        }

        @Test
        public void has_no_underscore() throws Exception {
            final String s = StringExtensions.asCapitalizedName(ObjectMemberAbstract.deriveMemberNameFrom("defghij"));
            assertThat(s, is("Defghij"));
        }

        @Test
        public void contains_one_underscore() throws Exception {
            final String s = StringExtensions.asCapitalizedName(ObjectMemberAbstract.deriveMemberNameFrom("abc_def"));
            assertThat(s, is("Def"));
        }

        @Test
        public void contains_more_than_one_underscore() throws Exception {
            final String s = StringExtensions.asCapitalizedName(ObjectMemberAbstract.deriveMemberNameFrom("abc_def_ghi"));
            assertThat(s, is("Ghi"));
        }
    }

}