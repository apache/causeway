package org.apache.isis.applib.services.bookmark;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.val;

class IdStringifierForInteger_Test {


    public static Stream<Arguments> roundtrip() {
        return Stream.of(
                Arguments.of(Integer.MAX_VALUE),
                Arguments.of(Integer.MIN_VALUE),
                Arguments.of(0),
                Arguments.of(12345),
                Arguments.of(-12345)
        );
    }

    @ParameterizedTest
    @MethodSource()
    void roundtrip(Integer value) {

        val stringifier = new IdStringifierForInteger();

        String stringified = stringifier.stringify(value);
        Integer parse = stringifier.parse(stringified, null);

        assertThat(parse).isEqualTo(value);
    }

}
