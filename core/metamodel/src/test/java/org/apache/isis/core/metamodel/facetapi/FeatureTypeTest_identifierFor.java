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

package org.apache.isis.core.metamodel.facetapi;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.isis.applib.Identifier;

public class FeatureTypeTest_identifierFor {

    public static class SomeDomainClass {
        private BigDecimal aBigDecimal;

        public BigDecimal getABigDecimal() {
            return aBigDecimal;
        }

        public void setABigDecimal(final BigDecimal aBigDecimal) {
            this.aBigDecimal = aBigDecimal;
        }

        private BigDecimal anotherBigDecimal;

        public BigDecimal getAnotherBigDecimal() {
            return anotherBigDecimal;
        }

        public void setAnotherBigDecimal(final BigDecimal anotherBigDecimal) {
            this.anotherBigDecimal = anotherBigDecimal;
        }
    }

    @Test
    public void property_whenMethodNameIs_XYyyZzz() throws Exception {
        final Method method = SomeDomainClass.class.getMethod("getABigDecimal");
        final Identifier identifierFor = FeatureType.PROPERTY.identifierFor(SomeDomainClass.class, method);
        assertThat(identifierFor.getMemberName(), is("ABigDecimal")); // very
        // odd
        // compared
        // to
        // anotherBigDecimal,
        // but
        // arises
        // from
        // Introspector
        // class,
        // so
        // presumably
        // part of
        // the
        // javabeans
        // spec.
    }

    @Test
    public void property_whenMethodNameIs_XxxxYyyZzz() throws Exception {
        final Method method = SomeDomainClass.class.getMethod("getAnotherBigDecimal");
        final Identifier identifierFor = FeatureType.PROPERTY.identifierFor(SomeDomainClass.class, method);
        assertThat(identifierFor.getMemberName(), is("anotherBigDecimal"));
    }

}
