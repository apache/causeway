package org.apache.isis.applib.services.bookmark;

import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import lombok.val;

class IdStringifierForCharacter_Test {

    public static Stream<Arguments> roundtrip() {
        return Stream.of(
                Arguments.of('a'),
                Arguments.of('b'),
                Arguments.of(' '),
                Arguments.of('^'), // shouldn't need ecoding
                Arguments.of('*'),
                Arguments.of('('),
                Arguments.of(')'),
                Arguments.of('_'),
                Arguments.of('-'),
                Arguments.of('['),
                Arguments.of(']'),
                Arguments.of(','),
                Arguments.of('.'),
                Arguments.of(';'),
                Arguments.of('/'), // should need encoding
                Arguments.of('\\'),
                Arguments.of('?'),
                Arguments.of(':'),
                Arguments.of('&'),
                Arguments.of('+'),
                Arguments.of('%')
        );
    }

    @ParameterizedTest
    @MethodSource()
    void roundtrip(String value) {

        val stringifier = new IdStringifierForString();

        val stringified = stringifier.enstring(value);
        val parse = stringifier.destring(stringified, null);

        Assertions.assertThat(parse).isEqualTo(value);
    }

}
