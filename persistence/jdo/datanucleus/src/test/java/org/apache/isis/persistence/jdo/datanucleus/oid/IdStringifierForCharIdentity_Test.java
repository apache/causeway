package org.apache.isis.persistence.jdo.datanucleus.oid;

import java.util.stream.Stream;

import javax.jdo.identity.CharIdentity;
import javax.jdo.identity.StringIdentity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.isis.applib.services.bookmark.IdStringifierForCharacter;
import org.apache.isis.applib.services.bookmark.IdStringifierForString;
import org.apache.isis.persistence.jdo.datanucleus.metamodel.facets.entity.IdStringifierForCharIdentity;
import org.apache.isis.persistence.jdo.datanucleus.metamodel.facets.entity.IdStringifierForStringIdentity;

import lombok.val;

class IdStringifierForCharIdentity_Test {

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

    static class Customer {}

    @ParameterizedTest
    @MethodSource()
    void roundtrip(char c) {

        val entityType = Customer.class;

        val stringifier = IdStringifierForCharIdentity.builder().idStringifierForCharacter(new IdStringifierForCharacter()).build();

        val value = new CharIdentity(entityType, c);
        val stringified = stringifier.enstring(value);
        val parse = stringifier.destring(stringified, entityType);

        Assertions.assertThat(parse).isEqualTo(value);
    }

}
