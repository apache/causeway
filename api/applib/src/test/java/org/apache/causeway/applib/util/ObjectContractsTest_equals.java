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
package org.apache.causeway.applib.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.util.ObjectContracts.ObjectContract;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

class ObjectContractsTest_equals {

    static class Invoice4 {
        private static final ObjectContract<Invoice4> objContract = ObjectContracts.parse(Invoice4.class, "number");

        @Getter @Setter private String number;
        @Override
        public int hashCode() {
            return objContract.hashCode(this);
        }
        @Override
        public boolean equals(final Object obj) {
            return objContract.equals(this, obj);
        }

    }

    private Invoice4 p;
    private Invoice4 q;
    private Invoice4 r;
    private String x;

    @BeforeEach
    public void setUp() throws Exception {
        p = new Invoice4();
        p.setNumber("123");
        q = new Invoice4();
        q.setNumber("123");
        r = new Invoice4();
        r.setNumber("456");

        x = "this is not an invoice";
    }

    @Test
    public void happyCase() throws Exception {
        assertEquals(p, q);
    }

    @Test
    public void nullsAreEqual() throws Exception {
        var objContract = ObjectContracts.parse(Invoice4.class, "number");
        assertTrue(objContract.equals(null, null));
    }

    @Test
    public void notEqualDifferentValues() throws Exception {
        assertNotEquals(p, r);
        assertNotEquals(r, p);
    }

    @Test
    public void notEqualDifferentTypes() throws Exception {
        assertNotEquals(p, x);
        assertNotEquals(x, p);
    }

    @Test
    public void notEqualNull() throws Exception {
        assertNotEquals(p, null);
        assertNotEquals(null, p);
    }

    @RequiredArgsConstructor(staticName = "of")
    static class ComplexNumber implements Comparable<ComplexNumber> {

        @Getter private final int real;
        @Getter private final int imaginary;

        private ObjectContracts.ObjectContract<ComplexNumber> contract
                = ObjectContracts.contract(ComplexNumber.class)
                    .thenUse("real", ComplexNumber::getReal)
                    .thenUse("imaginary", ComplexNumber::getImaginary);

        @Override
        public boolean equals(final Object o) {
            return contract.equals(this, o);
        }

        @Override
        public int hashCode() {
            return contract.hashCode(this);
        }

        @Override
        public int compareTo(final ComplexNumber other) {
            return contract.compare(this, other);
        }

        @Override
        public String toString() {
            return contract.toString(this);
        }
    }

    @Test
    public void more_complex_scenario() {
        final ComplexNumber cn1 = ComplexNumber.of(3, 4);
        final ComplexNumber cn2 = ComplexNumber.of(3, 4);

        assertEquals(cn1, cn2);
    }

}
