package org.apache.isis.applib.services.bookmark;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.val;

class IdStringifierForByte_Test {


    public static Stream<Arguments> roundtrip() {
        return Stream.of(
                Arguments.of(Byte.MAX_VALUE),
                Arguments.of(Byte.MIN_VALUE),
                Arguments.of((byte)0),
                Arguments.of((byte)12345),
                Arguments.of((byte)-12345)
        );
    }

    @ParameterizedTest
    @MethodSource()
    void roundtrip(Byte value) {

        val stringifier = new IdStringifierForByte();

        String stringified = stringifier.stringify(value);
        Byte parse = stringifier.parse(stringified, null);

        assertThat(parse).isEqualTo(value);
    }

}
