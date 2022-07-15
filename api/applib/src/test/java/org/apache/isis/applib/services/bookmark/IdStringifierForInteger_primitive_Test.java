package org.apache.isis.applib.services.bookmark;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.val;

class IdStringifierForInteger_primitive_Test {


    public static Stream<Arguments> roundtrip() {
        return Stream.of(
                Arguments.of(Integer.MAX_VALUE),
                Arguments.of(Integer.MIN_VALUE),
                Arguments.of(0),
                Arguments.of(12345678),
                Arguments.of(-12345678)
        );
    }

    @ParameterizedTest
    @MethodSource()
    void roundtrip(int value) {

        val stringifier = new IdStringifierForInteger();

        String stringified = stringifier.enstring(value);
        Integer parse = stringifier.destring(stringified, null);

        assertThat(parse).isEqualTo(value);
    }

}
