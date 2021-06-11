package org.apache.isis.applib.services.iactnlayer;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import lombok.val;

class InteractionContext_combine_Test {

    @Test
    void happy_case() {

        val mappers = Stream.<UnaryOperator<String>>of(
                s -> s + "bar",
                s -> "[" + s + "]",
                s -> s + s);
        val result = InteractionContext.combine(mappers).apply("foo");

        Assertions.assertThat(result).isEqualTo("[foobar][foobar]");
    }

}
