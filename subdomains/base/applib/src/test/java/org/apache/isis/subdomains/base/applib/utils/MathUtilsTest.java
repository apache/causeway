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
package org.apache.isis.subdomains.base.applib.utils;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MathUtilsTest {

    public static class IsZeroOrNull extends MathUtilsTest {

        @Test
        public void testIsZeroOrNull() {
            Assert.assertTrue(MathUtils.isZeroOrNull(null));
            Assert.assertTrue(MathUtils.isZeroOrNull(BigDecimal.valueOf(0)));
            Assert.assertFalse(MathUtils.isZeroOrNull(BigDecimal.valueOf(100)));
            Assert.assertFalse(MathUtils.isNotZeroOrNull(null));
            Assert.assertFalse(MathUtils.isNotZeroOrNull(BigDecimal.valueOf(0)));
            Assert.assertTrue(MathUtils.isNotZeroOrNull(BigDecimal.valueOf(100)));
        }
    }

    public static class Round extends MathUtilsTest {

        @Test
        public void roundDown() throws Exception {
            assertThat(MathUtils.round(new BigDecimal("4.54"), 1), is(new BigDecimal("4.5")));
        }

        @Test
        public void noRounding() throws Exception {
            assertThat(MathUtils.round(new BigDecimal("4.54"), 2), is(new BigDecimal("4.54")));
        }

        @Test
        public void roundUp() throws Exception {
            assertThat(MathUtils.round(new BigDecimal("4.55"), 1), is(new BigDecimal("4.6")));
        }
    }

    public static class Max extends MathUtilsTest {

        @Test
        public void happyCase() throws Exception {
            assertThat(MathUtils.max(null, BigDecimal.ZERO, new BigDecimal("123.45"), new BigDecimal("123")), is(new BigDecimal("123.45")));
        }
    }

    public static class SomeThing extends MathUtilsTest {

        @Test
        public void happyCase() throws Exception {
            assertThat(MathUtils.maxUsingFirstSignum(new BigDecimal("-123.45"), new BigDecimal("-123")), is(new BigDecimal("-123.45")));
            assertThat(MathUtils.maxUsingFirstSignum(null, BigDecimal.ZERO, new BigDecimal("-123.45"), new BigDecimal("-123")), is(new BigDecimal("-123.45")));
        }
    }

}
