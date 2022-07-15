package org.apache.isis.applib.services.bookmark;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.isis.applib.services.urlencoding.UrlEncodingService;
import org.apache.isis.commons.internal.base._Bytes;
import org.apache.isis.commons.internal.base._Strings;

import lombok.val;

class IdStringifierForSerializable_Test {

    static final UrlEncodingService CODEC = new UrlEncodingService() {
        @Override
        public String encode(final byte[] bytes) {
            return _Strings.ofBytes(_Bytes.asCompressedUrlBase64.apply(bytes), StandardCharsets.UTF_8);
        }

        @Override
        public byte[] decode(final String str) {
            return _Bytes.ofCompressedUrlBase64.apply(_Strings.toBytes(str, StandardCharsets.UTF_8));
        }
    };

    public static Stream<Arguments> roundtrip() {
        return Stream.of(
                Arguments.of(Byte.MAX_VALUE),
                Arguments.of(Byte.MIN_VALUE),
                Arguments.of((byte)0),
                Arguments.of((byte)12345),
                Arguments.of((byte)-12345)
        );
    }

    static class Customer {}

    @ParameterizedTest
    @MethodSource()
    void roundtrip(Serializable value) {

        val stringifier = new IdStringifierForSerializable(CODEC);

        String stringified = stringifier.enstring(value);
        Serializable parse = stringifier.destring(stringified, Customer.class);

        assertThat(parse).isEqualTo(value);
    }

}
