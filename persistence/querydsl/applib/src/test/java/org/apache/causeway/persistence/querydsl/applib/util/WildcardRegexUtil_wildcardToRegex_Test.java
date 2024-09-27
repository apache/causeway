package org.apache.causeway.persistence.querydsl.applib.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.apache.causeway.persistence.querydsl.applib.util.CaseSensitivity.*;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.RequiredArgsConstructor;

public class WildcardRegexUtil_wildcardToRegex_Test {

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
    public void should_convert_wildcard_to_regex(Scenario scenario) {
        String result = Wildcards.wildcardToRegex(scenario.input, scenario.caseSensitivity);
        assertThat(result).isEqualTo(scenario.expected);
    }
}

