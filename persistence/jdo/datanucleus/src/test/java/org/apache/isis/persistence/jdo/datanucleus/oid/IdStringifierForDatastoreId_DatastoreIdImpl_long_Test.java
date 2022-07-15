package org.apache.isis.persistence.jdo.datanucleus.oid;

import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.datanucleus.identity.DatastoreIdImpl;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.isis.persistence.jdo.datanucleus.metamodel.facets.entity.IdStringifierForDatastoreIdImpl;

import lombok.val;

class IdStringifierForDatastoreId_DatastoreIdImpl_long_Test {

    public static Stream<Arguments> roundtrip() {
        return Stream.of(
                Arguments.of(1L, "1[OID]org.apache.isis.persistence.jdo.datanucleus.oid.IdStringifierForDatastoreId_DatastoreIdImpl_long_Test$Customer"),
                Arguments.of(0L, null),
                Arguments.of(10L, null),
                Arguments.of(Long.MAX_VALUE, null),
                Arguments.of(Long.MIN_VALUE, null)
        );
    }

    static class Customer {}

    @ParameterizedTest
    @MethodSource()
    void roundtrip(long value, String enstringed) {

        val entityType = Customer.class;

        val stringifier = new IdStringifierForDatastoreIdImpl();

        val stringified = stringifier.enstring(new DatastoreIdImpl(entityType.getName(), value));
        if(enstringed != null) {
            Assertions.assertThat(stringified).isEqualTo(enstringed);
        }
        val parse = stringifier.destring(stringified, entityType);

        Assertions.assertThat(parse.getKeyAsObject()).isEqualTo(value);
        Assertions.assertThat(parse.getTargetClassName()).isEqualTo(entityType.getName());
    }

}
