package org.apache.isis.persistence.jdo.datanucleus.oid;

import java.util.stream.Stream;

import javax.jdo.identity.ObjectIdentity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.isis.applib.services.bookmark.IdStringifierForUuid;
import org.apache.isis.persistence.jdo.datanucleus.metamodel.facets.entity.IdStringifierForObjectIdentity;

import lombok.val;

class IdStringifierForObjectIdentity_other_Test {

    public static Stream<Arguments> roundtrip() {
        return Stream.of(
                Arguments.of(1),
                Arguments.of(0),
                Arguments.of(10),
                Arguments.of(Integer.MAX_VALUE),
                Arguments.of(Integer.MIN_VALUE)
        );
    }

    static class Customer {}

    @ParameterizedTest
    @MethodSource()
    void roundtrip(int value) {

        val entityType = Customer.class;

        val stringifier = IdStringifierForObjectIdentity.builder()
                                .idStringifierForUuid(new IdStringifierForUuid())
                                .build();

        val stringified = stringifier.enstring(new ObjectIdentity(entityType, value));
        val parse = stringifier.destring(stringified, entityType);

        Assertions.assertThat(parse.getKeyAsObject()).isEqualTo(value);
        Assertions.assertThat(parse.getTargetClass()).isEqualTo(entityType);
    }

}
