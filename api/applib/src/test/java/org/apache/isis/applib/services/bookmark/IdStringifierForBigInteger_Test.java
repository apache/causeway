package org.apache.isis.applib.services.bookmark;

import java.math.BigInteger;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.*;

import lombok.val;

class IdStringifierForBigInteger_Test {

    public static Stream<Arguments> roundtrip() {
        return Stream.of(
                Arguments.of(BigInteger.ZERO),
                Arguments.of(BigInteger.ONE),
                Arguments.of(BigInteger.TEN),
                Arguments.of(BigInteger.valueOf(Long.MAX_VALUE)),
                Arguments.of(BigInteger.valueOf(Long.MIN_VALUE)),
                Arguments.of(BigInteger.valueOf(Double.MAX_EXPONENT)),
                Arguments.of(BigInteger.valueOf(Double.MIN_EXPONENT)),
                Arguments.of(BigInteger.valueOf(10)),
                Arguments.of(new BigInteger("12345678901234567890123456789012345678901234567890"))
        );
    }

    static class Customer {}

    @ParameterizedTest
    @MethodSource()
    void roundtrip(BigInteger bigInteger) {

        val stringifier = new IdStringifierForBigInteger();

        String stringified = stringifier.enstring(bigInteger);
        BigInteger parse = stringifier.destring(stringified, Customer.class);

        assertThat(parse).isEqualTo(bigInteger);
    }

}
