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

package org.apache.isis.core.metamodel.commons;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public class IoUtils_CopyTest {

    private static final class ArrayMatcher extends TypeSafeMatcher<byte[]> {

        private final byte[] expectedBytes;

        public ArrayMatcher(final byte[] expectedBytes) {
            this.expectedBytes = expectedBytes;
        }

        @Override
        public boolean matchesSafely(final byte[] actualBytes) {
            if (actualBytes.length != expectedBytes.length) {
                return false;
            }
            for (int i = 0; i < actualBytes.length; i++) {
                if (actualBytes[i] != expectedBytes[i]) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void describeTo(final Description arg0) {
            arg0.appendText("does not match expected array");
        }
    }

    private static int BUF_INTERNAL_SIZE = 1024;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test(expected = IllegalArgumentException.class)
    public void handlesNullInputStream() throws Exception {
        final ByteArrayInputStream bais = null;
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStreamExtensions.copyTo(bais, baos);
    }

    @Test(expected = IllegalArgumentException.class)
    public void handlesNullOutputStream() throws Exception {
        final byte[] bytes = createByteArray(10);
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final ByteArrayOutputStream baos = null;
        InputStreamExtensions.copyTo(bais, baos);
    }

    @Test
    public void copiesEmptyInputStream() throws Exception {
        final byte[] bytes = createByteArray(0);
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStreamExtensions.copyTo(bais, baos);
        assertThat(baos.toByteArray(), arrayEqualTo(bytes));
    }

    @Test
    public void copiesInputStreamSmallerThanInternalBuffer() throws Exception {
        final byte[] bytes = createByteArray(BUF_INTERNAL_SIZE - 1);
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStreamExtensions.copyTo(bais, baos);
        assertThat(baos.toByteArray(), arrayEqualTo(bytes));
    }

    @Test
    public void copiesInputStreamLargerThanInternalBuffer() throws Exception {
        final byte[] bytes = createByteArray(BUF_INTERNAL_SIZE + 1);
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStreamExtensions.copyTo(bais, baos);
        assertThat(baos.toByteArray(), arrayEqualTo(bytes));
    }

    @Test
    public void copiesInputStreamExactlySameSizeAsInternalBuffer() throws Exception {
        final byte[] bytes = createByteArray(BUF_INTERNAL_SIZE);
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStreamExtensions.copyTo(bais, baos);
        assertThat(baos.toByteArray(), arrayEqualTo(bytes));
    }

    private Matcher<byte[]> arrayEqualTo(final byte[] bytes) {
        return new ArrayMatcher(bytes);
    }

    private byte[] createByteArray(final int size) {
        final byte[] bytes = new byte[size];
        for (int i = 0; i < size; i++) {
            bytes[i] = (byte) i;
        }
        return bytes;
    }

}
