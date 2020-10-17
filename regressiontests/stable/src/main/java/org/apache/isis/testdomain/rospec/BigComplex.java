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
package org.apache.isis.testdomain.rospec;

import java.math.BigDecimal;
import java.math.MathContext;

import org.apache.isis.commons.internal.assertions._Assert;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.val;

/**
 * Example (composite) type for testing.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor(staticName = "of")
public final class BigComplex {

    private BigDecimal re;
    private BigDecimal im;
    
    public static BigComplex zero() {
        return BigComplex.of(BigDecimal.ZERO, BigDecimal.ZERO); 
    }
    
    public static BigComplex of(String re, String im) {
        return BigComplex.of(new BigDecimal(re), new BigDecimal(im)); 
    }
    
    public BigComplex add(BigComplex other) {
        return BigComplex.of(
                this.re.add(other.re), 
                this.im.add(other.im));
    }
    
    public BigComplex subtract(BigComplex other) {
        return BigComplex.of(
                this.re.subtract(other.re), 
                this.im.subtract(other.im));
    }
    
    public BigDecimal norm() {
        return this.re.multiply(this.re)
                .add(this.im.multiply(this.im))
                .sqrt(MathContext.UNLIMITED); 
    }
    
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof BigComplex)) {
            return false;    
        }
        val other = (BigComplex) obj;
        return this.re.compareTo(other.re) == 0
                && this.im.compareTo(other.im) == 0;
    }
    
    public static void assertEquals(BigComplex a, BigComplex b) {
        _Assert.assertEquals(a.re.toPlainString(), b.re.toPlainString());
        _Assert.assertEquals(a.im.toPlainString(), b.im.toPlainString());
    }
    
    public static void assertNumberEquals(BigComplex a, BigComplex b, double epsilon) {
        _Assert.assertTrue(a.subtract(b).norm().doubleValue()<epsilon);
    }
    
}
