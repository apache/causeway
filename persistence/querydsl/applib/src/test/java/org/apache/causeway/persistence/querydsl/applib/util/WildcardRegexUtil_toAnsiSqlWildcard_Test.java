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
package org.apache.causeway.persistence.querydsl.applib.util;

import lombok.RequiredArgsConstructor;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class WildcardRegexUtil_toAnsiSqlWildcard_Test {

    @RequiredArgsConstructor
    enum Scenario {

        NO_WILDCARD         ("test",      "%test%"),
        WITH_STAR           ("te*st",     "te%st"),
        WITH_QUESTION_MARK  ("te?t",      "te_t"),
        WITH_REGEX_PATTERN  ("(?i)te?t",  "(?i)te?t"),
        NO_WILDCARD_ADDED   ("tes",       "%tes%"),
        BOTH_WILDCARDS      ("t*e?t",     "t%e_t"),
        ;

        private final String input;
        private final String expected;
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    void should_replace_wildcards(Scenario scenario) {
        String result = Wildcards.toAnsiSqlWildcard(scenario.input);
        assertThat(result).isEqualTo(scenario.expected);
    }
}

