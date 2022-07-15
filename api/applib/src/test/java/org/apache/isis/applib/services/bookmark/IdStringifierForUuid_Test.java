package org.apache.isis.applib.services.bookmark;

import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.val;

class IdStringifierForUuid_Test {


    public static Stream<Arguments> roundtrip() {
        return Stream.of(
                Arguments.of(UUID.randomUUID()),
                Arguments.of(UUID.randomUUID()),
                Arguments.of(UUID.randomUUID())
        );
    }

    @ParameterizedTest
    @MethodSource()
    void roundtrip(UUID value) {

        val stringifier = new IdStringifierForUuid();

        String stringified = stringifier.enstring(value);
        UUID parse = stringifier.destring(stringified, null);

        assertThat(parse).isEqualTo(value);
    }

}
