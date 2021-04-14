package org.apache.isis.tooling.j2adoc.format;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnitFormatterAbstractTest {

    @Nested
    class determineBaseName {

        @Test
        void happy_case() {
            final String s = UnitFormatterAbstract.determineBaseName("Action.adoc");
            Assertions.assertEquals("Action", s);
        }

        @Test
        void other_case() {
            final String s = UnitFormatterAbstract.determineBaseName("foobar");
            Assertions.assertEquals("foobar", s);
        }

    }
}
