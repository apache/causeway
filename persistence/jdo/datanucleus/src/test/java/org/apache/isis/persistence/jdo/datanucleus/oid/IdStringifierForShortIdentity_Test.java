package org.apache.isis.persistence.jdo.datanucleus.oid;

import java.util.stream.Stream;

import javax.jdo.identity.ShortIdentity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.isis.persistence.jdo.datanucleus.metamodel.facets.entity.IdStringifierForShortIdentity;

import lombok.val;

class IdStringifierForShortIdentity_Test {

    public static Stream<Arguments> roundtrip() {
        return Stream.of(
                Arguments.of(Short.MAX_VALUE),
                Arguments.of(Short.MIN_VALUE),
                Arguments.of((short)0),
                Arguments.of((short)12345),
                Arguments.of((short)-12345)
        );
    }

    static class Customer {}

    @ParameterizedTest
    @MethodSource()
    void roundtrip(short value) {

        val entityType = Customer.class;

        val stringifier = new IdStringifierForShortIdentity();

        val stringified = stringifier.enstring(new ShortIdentity(entityType, value));
        val parse = stringifier.destring(stringified, entityType);

        Assertions.assertThat(parse.getKeyAsObject()).isEqualTo(value);
        Assertions.assertThat(parse.getTargetClass()).isEqualTo(entityType);
    }

}
