package org.apache.isis.applib.services.bookmark;

import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import lombok.val;

class IdStringifierForString_Test {

    public static Stream<Arguments> roundtrip() {
        return Stream.of(
                Arguments.of("abc"),
                Arguments.of("abcdefghijklmnopqrstuvwxyz"),
                Arguments.of("this is a long string with spaces in it"),
                Arguments.of("these characters ^*()[],.;_-+ do not require encoding"),
                Arguments.of("this needs to be encoded because it contains a / within it"),
                Arguments.of("this needs to be encoded because it contains a \\ within it"),
                Arguments.of("this needs to be encoded because it contains a ? within it"),
                Arguments.of("this needs to be encoded because it contains a : within it"),
                Arguments.of("this needs to be encoded because it contains a & within it"),
                Arguments.of("this needs to be encoded because it contains a + within it"),
                Arguments.of("this needs to be encoded because it contains a % within it")
        );
    }

    static class Customer {}

    @ParameterizedTest
    @MethodSource()
    void roundtrip(String value) {

        val stringifier = new IdStringifierForString();

        val stringified = stringifier.enstring(value);
        val parse = stringifier.destring(stringified, Customer.class);

        Assertions.assertThat(parse).isEqualTo(value);
    }

}
