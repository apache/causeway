package org.apache.causeway.persistence.querydsl.applib.util;

import lombok.RequiredArgsConstructor;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

public class WildcardRegexUtil_toAnsiSqlWildcard_Test {

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
    public void should_replace_wildcards(Scenario scenario) {
        String result = Wildcards.toAnsiSqlWildcard(scenario.input);
        assertThat(result).isEqualTo(scenario.expected);
    }
}

