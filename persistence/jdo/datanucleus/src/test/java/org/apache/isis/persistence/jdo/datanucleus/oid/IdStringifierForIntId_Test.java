package org.apache.isis.persistence.jdo.datanucleus.oid;

import java.util.stream.Stream;

import javax.jdo.identity.IntIdentity;

import org.assertj.core.api.Assertions;
import org.datanucleus.identity.IntId;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.isis.persistence.jdo.datanucleus.metamodel.facets.entity.IdStringifierForIntId;
import org.apache.isis.persistence.jdo.datanucleus.metamodel.facets.entity.IdStringifierForIntIdentity;

import lombok.val;

class IdStringifierForIntId_Test {

    public static Stream<Arguments> roundtrip() {
        return Stream.of(
                Arguments.of(Integer.MAX_VALUE),
                Arguments.of(Integer.MIN_VALUE),
                Arguments.of(0),
                Arguments.of(12345678),
                Arguments.of(-12345678)
        );
    }

    static class Customer {}

    @ParameterizedTest
    @MethodSource()
    void roundtrip(Integer value) {

        val entityType = Customer.class;

        val stringifier = new IdStringifierForIntId();

        val stringified = stringifier.enstring(new IntId(entityType, value));
        val parse = stringifier.destring(stringified, entityType);

        Assertions.assertThat(parse.getKeyAsObject()).isEqualTo(value);
        Assertions.assertThat(parse.getTargetClassName()).isEqualTo(entityType.getName());
    }

}
