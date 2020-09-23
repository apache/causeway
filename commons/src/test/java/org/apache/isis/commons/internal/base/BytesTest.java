/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.commons.internal.base;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.isis.commons.internal._Constants;

import lombok.val;

class BytesTest {

    final int n = 256;
    private final byte[] allBytes = new byte[n];

    private static final byte[] testimonial = 
            _Strings.toBytes(
                    "https://docs.oracle.com/javase/8/docs/api/java/util/Base64.html#basic?"+
                            "0-theme-entityPageContainer-entity-rows-2-rowContents-1-col-tabGroups-1-panel-"
                            + "tabPanel-rows-1-rowContents-1-col-fieldSets-1-memberGroup-properties-1-property-"
                            + "scalarTypeContainer-scalarIfRegular-associatedActionLinksBelow-additionalLinkList-"
                            + "additionalLinkItem-0-additionalLink", 
                            StandardCharsets.UTF_8);

    @BeforeEach
    void before() {
        for(int i=0; i<n; ++i) {
            allBytes[i] = (byte)i;
        }
    }

    // -- PREPEND/APPEND

    @Test
    void concatNullWithNull() throws Exception {
        assertNull(_Bytes.append(null, null));
        assertNull(_Bytes.prepend(null, null));
    }

    @Test
    void concatNullWithEmpty() throws Exception {
        assertArrayEquals(_Constants.emptyBytes, _Bytes.append(null));
        assertArrayEquals(_Constants.emptyBytes, _Bytes.prepend(null));
    }

    @Test
    void concatWithNull() throws Exception {
        assertArrayEqualsButNotSame(allBytes, _Bytes.append(allBytes, null));
        assertArrayEqualsButNotSame(allBytes, _Bytes.prepend(allBytes, null));
    }

    @Test
    void concatWithEmpty() throws Exception {
        assertArrayEqualsButNotSame(allBytes, _Bytes.append(allBytes));
        assertArrayEqualsButNotSame(allBytes, _Bytes.prepend(allBytes));
    }

    @Test
    void concatHappyCase() throws Exception {
        assertArrayEqualsButNotSame(new byte[] {1,2,3,4,5}, _Bytes.append(new byte[] {1,2,3}, (byte)4, (byte)5));
        assertArrayEqualsButNotSame(new byte[] {4,5,1,2,3}, _Bytes.prepend(new byte[] {1,2,3}, (byte)4, (byte)5));
    }

    @Test
    void compressIdentityWithNull() throws Exception {
        assertNull(_Bytes.decompress(_Bytes.compress(null)));
    }

    @Test
    void compressIdentityWithByteRange() throws Exception {
        assertArrayEquals(allBytes,
                _Bytes.decompress(_Bytes.compress(allBytes)));
    }

    @Test
    void compressIdentityWithTestimonial() throws Exception {
        assertArrayEquals(testimonial,
                _Bytes.decompress(_Bytes.compress(testimonial)));
    }

    @Test
    void compressionRatio() throws Exception {
        // lower is better
        final double compressionRatio = (double)_Bytes.compress(testimonial).length / testimonial.length;
        assertThat(compressionRatio, lessThan(0.7));
    }


    // -- COMPRESSION

    private static byte[] data(int index) {
        return (byte[]) new Object[] { 
                (byte[]) null,
                new byte[] { },
                new byte[] { 0 }, 
                new byte[] { 0, 1 },
                new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, // 17 
                new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, // 18
                new byte[] { 31, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, // 19
        }[index];
    }
    
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6}) // data indices
    void compressionRundtrip(int dataIndex) throws Exception {
        val input = data(dataIndex);
        assertArrayEquals(input,
                _Bytes.decompress(_Bytes.compress(input)));
    }

    // -- BASE-64

    @Test
    void base64IdentityWithNull() throws Exception {
        assertNull(_Bytes.decodeBase64(
                Base64.getUrlDecoder(), 
                _Bytes.encodeToBase64(Base64.getUrlEncoder(), null)));
    }

    @Test
    void base64IdentityWithByteRange() throws Exception {
        assertArrayEquals(allBytes,
                _Bytes.decodeBase64(
                        Base64.getUrlDecoder(), 
                        _Bytes.encodeToBase64(Base64.getUrlEncoder(), allBytes)));
    }

    @Test
    void base64IdentityWithTestimonial() throws Exception {
        assertArrayEquals(testimonial,
                _Bytes.decodeBase64(
                        Base64.getUrlDecoder(), 
                        _Bytes.encodeToBase64(Base64.getUrlEncoder(), testimonial)));
    }

    // -- OPERATOR COMPOSITION

    @Test
    void composedOperatorWithNull() throws Exception {
        assertNull(_Bytes.asCompressedUrlBase64.apply(null));
        assertNull(_Bytes.ofCompressedUrlBase64.apply(null));
        assertNull(_Bytes.asUrlBase64.apply(null));
        assertNull(_Bytes.ofUrlBase64.apply(null));
    }

    @Test
    void composedIdentityWithByteRange() throws Exception {
        assertArrayEquals(allBytes,
                _Bytes.ofCompressedUrlBase64.apply(
                        _Bytes.asCompressedUrlBase64.apply(allBytes)));
    }

    @Test
    void composedIdentityWithTestimonial() throws Exception {
        assertArrayEquals(testimonial,
                _Bytes.ofCompressedUrlBase64.apply(
                        _Bytes.asCompressedUrlBase64.apply(testimonial)));
    }

    // -- HELPER

    private void assertArrayEqualsButNotSame(byte[] a, byte[] b) {
        assertFalse(a == b);
        assertArrayEquals(a, b);
    }

}
