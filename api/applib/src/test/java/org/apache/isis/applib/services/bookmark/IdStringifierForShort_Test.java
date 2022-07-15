package org.apache.isis.applib.services.bookmark;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.val;

class IdStringifierForShort_Test {

    public static Stream<Arguments> roundtrip() {
        return Stream.of(
                Arguments.of(Short.MAX_VALUE),
                Arguments.of(Short.MIN_VALUE),
                Arguments.of((short)0),
                Arguments.of((short)12345),
                Arguments.of((short)-12345)
        );
    }

    static class Customer {}

    @ParameterizedTest
    @MethodSource()
    void roundtrip(Short value) {

        val stringifier = new IdStringifierForShort();

        String stringified = stringifier.enstring(value);
        Short parse = stringifier.destring(stringified, Customer.class);

        assertThat(parse).isEqualTo(value);
    }

}
