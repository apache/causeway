package org.apache.isis.applib.services.bookmark;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.isis.applib.services.urlencoding.UrlEncodingService;
import org.apache.isis.commons.internal.base._Bytes;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.memento._Mementos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;

@ExtendWith(MockitoExtension.class)
class IdStringifierForEntity_Test {

    @Mock private BookmarkService mockBookmarkService;

    public static Stream<Arguments> roundtrip() {
        return Stream.of(
                Arguments.of(Customer.builder().id(123).build(), Bookmark.forLogicalTypeNameAndIdentifier("cust.Customer", "123")),
                Arguments.of(Customer.builder().id(456).build(), Bookmark.forLogicalTypeNameAndIdentifier("cust.Customer", "456")),
                Arguments.of(Order.builder().id("789").build(), Bookmark.forLogicalTypeNameAndIdentifier("order.Order", "789"))
        );
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    static class Customer {
        private int id;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    static class Order {
        private String id;
    }

    @ParameterizedTest
    @MethodSource()
    void roundtrip(Object entity, Bookmark bookmark) {

        Mockito.when(mockBookmarkService.bookmarkFor(entity)).thenReturn(Optional.of(bookmark));
        Mockito.when(mockBookmarkService.lookup(bookmark)).thenReturn(Optional.of(entity));
        val stringifier = new IdStringifierForEntity(mockBookmarkService, new IdStringifierForBookmark());

        String stringified = stringifier.stringify(entity);
        Object parse = stringifier.parse(stringified, null);

        assertThat(parse).isSameAs(entity);
    }

}
