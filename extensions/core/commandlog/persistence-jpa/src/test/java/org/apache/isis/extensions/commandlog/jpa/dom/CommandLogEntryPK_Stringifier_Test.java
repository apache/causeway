package org.apache.isis.extensions.commandlog.jpa.dom;

import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.val;

class CommandLogEntryPK_Stringifier_Test {

    public static Stream<Arguments> roundtrip() {
        return Stream.of(
                Arguments.of(UUID.randomUUID()),
                Arguments.of(UUID.randomUUID()),
                Arguments.of(UUID.randomUUID())
        );
    }

    @ParameterizedTest
    @MethodSource()
    void roundtrip(UUID uuid) {
        CommandLogEntryPK value = new CommandLogEntryPK(uuid);

        val stringifier = new CommandLogEntryPK.Stringifier();

        String stringified = stringifier.stringify(value);
        val parse = stringifier.parse(stringified, null);

        assertThat(parse).isEqualTo(value);
    }

}
