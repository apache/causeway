package org.apache.isis.persistence.jdo.datanucleus.oid;

import java.util.stream.Stream;

import javax.jdo.identity.StringIdentity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.isis.applib.services.bookmark.IdStringifierForString;
import org.apache.isis.persistence.jdo.datanucleus.metamodel.facets.entity.IdStringifierForStringIdentity;

import lombok.val;

class IdStringifierForStringIdentity_Test {

    public static Stream<Arguments> roundtrip() {
        return Stream.of(
                Arguments.of("abc"),
                Arguments.of("abcdefghijklmnopqrstuvwxyz"),
                Arguments.of("this is a long string with spaces in it"),
                Arguments.of("these characters ^&*()[],.; do not require encoding"),
                Arguments.of("this needs to be encoded because it contains a / within it"),
                Arguments.of("this needs to be encoded because it contains a \\ within it"),
                Arguments.of("this needs to be encoded because it contains a ? within it"),
                Arguments.of("this needs to be encoded because it contains a : within it"),
                Arguments.of("this needs to be encoded because it contains a & within it"),
                Arguments.of("this needs to be encoded because it contains a % within it")
        );
    }

    static class Customer {}

    @ParameterizedTest
    @MethodSource()
    void roundtrip(String value) {

        val entityType = Customer.class;

        val stringifier = IdStringifierForStringIdentity.builder().idStringifierForString(new IdStringifierForString()).build();

        val stringified = stringifier.stringify(new StringIdentity(entityType, value));
        val parse = stringifier.parse(stringified, entityType);

        Assertions.assertThat(parse.getKeyAsObject()).isEqualTo(value);
        Assertions.assertThat(parse.getTargetClass()).isEqualTo(entityType);
    }

}
