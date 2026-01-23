package org.apache.causeway.applib.mixins.system;

import org.apache.causeway.applib.services.bookmark.Bookmark;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainChangeRecord_Test {

    @Test
    void when_populated() {
        final var dcr = new DomainChangeRecord.Empty() {
            @Override
            public Bookmark getTarget() {
                return Bookmark.forLogicalTypeNameAndIdentifier("Customer", "12345");
            }
        };

        assertThat(dcr.getTargetLogicalTypeName()).isEqualTo("Customer");
    }

    @Test
    void when_not_populated() {
        final var dcr = new DomainChangeRecord.Empty() {
            @Override
            public Bookmark getTarget() {
                return null;
            }
        };

        assertThat(dcr.getTargetLogicalTypeName()).isNull();
    }

}