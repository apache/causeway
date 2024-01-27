package org.apache.causeway.viewer.graphql.viewer.test.domain.calc;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class Month_Test {

    @Test
    public void testNextMonth() {
        assertThat(Month.JANUARY.nextMonth()).isEqualTo(Month.FEBRUARY);
        assertThat(Month.DECEMBER.nextMonth()).isEqualTo(Month.JANUARY);
    }
}