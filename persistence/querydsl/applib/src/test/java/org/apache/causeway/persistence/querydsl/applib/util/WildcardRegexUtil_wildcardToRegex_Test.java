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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.apache.causeway.persistence.querydsl.applib.util.CaseSensitivity.*;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.RequiredArgsConstructor;

class WildcardRegexUtil_wildcardToRegex_Test {

    @RequiredArgsConstructor
    enum Scenario {
        CASE_SENSITIVE                      ("te*t", SENSITIVE,     "te.*t"),
        CASE_INSENSITIVE                    ("te*t", INSENSITIVE,   "(?i)te.*t"),
        CASE_SENSITIVE_NO_WILDCARDS         ("test", SENSITIVE,     ".*test.*"),
        CASE_INSENSITIVE_NO_WILDCARDS       ("test", INSENSITIVE,   "(?i).*test.*"),
        CASE_SENSITIVE_WITH_QUESTION_MARK   ("te?t", SENSITIVE,     "te.t"),
        CASE_INSENSITIVE_WITH_QUESTION_MARK ("te?t", INSENSITIVE,   "(?i)te.t"),
        CASE_SENSITIVE_OUTER_WILDCARD       ("*test*", SENSITIVE,     ".*test.*"),
        CASE_INSENSITIVE_OUTER_WILDCARD     ("*test*", INSENSITIVE,   "(?i).*test.*"),
        NULL_PATTERN_CASE_SENSITIVE         (null,   SENSITIVE,     ".*"),
        NULL_PATTERN_CASE_INSENSITIVE       (null,   INSENSITIVE,   "(?i).*");

        private final String           input;
        private final CaseSensitivity  caseSensitivity;
        private final String           expected;
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    void should_convert_wildcard_to_regex(Scenario scenario) {
        String result = Wildcards.wildcardToRegex(scenario.input, scenario.caseSensitivity);
        assertThat(result).isEqualTo(scenario.expected);
    }
}

