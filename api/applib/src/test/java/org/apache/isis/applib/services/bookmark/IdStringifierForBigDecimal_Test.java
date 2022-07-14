package org.apache.isis.applib.services.bookmark;

import java.math.BigDecimal;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.val;

class IdStringifierForBigDecimal_Test {


    public static Stream<Arguments> roundtrip() {
        return Stream.of(
                Arguments.of(BigDecimal.ZERO),
                Arguments.of(BigDecimal.ONE),
                Arguments.of(BigDecimal.TEN),
                Arguments.of(BigDecimal.valueOf(Long.MAX_VALUE)),
                Arguments.of(BigDecimal.valueOf(Long.MIN_VALUE)),
                Arguments.of(BigDecimal.valueOf(Double.MAX_VALUE)),
                Arguments.of(BigDecimal.valueOf(Double.MIN_VALUE)),
                Arguments.of(BigDecimal.valueOf(10, 0)),
                Arguments.of(BigDecimal.valueOf(10, 1)),
                Arguments.of(BigDecimal.valueOf(10, 2)),
                Arguments.of(BigDecimal.valueOf(10, 3)),
                Arguments.of(new BigDecimal("1234567890123456789012345678901234567890.12345678901234567890"))
        );
    }

    @ParameterizedTest
    @MethodSource()
    void roundtrip(BigDecimal bigDecimal) {

        val stringifier = new IdStringifierForBigDecimal();

        String stringified = stringifier.stringify(bigDecimal);
        BigDecimal parse = stringifier.parse(stringified, null);

        assertThat(parse).isEqualTo(bigDecimal);
    }

}
