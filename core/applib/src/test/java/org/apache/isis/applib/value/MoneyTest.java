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

package org.apache.isis.applib.value;

import java.math.BigDecimal;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MoneyTest {

    @Test
    public void testAdd() {
        final Money m1 = new Money(110, "pds");
        final Money m2 = new Money(220, "pds");
        final Money m3 = m1.add(m2);
        assertEquals(330.0, m3.doubleValue(), 0.0);
    }

    @Test
    public void testAddWithCents() {
        final Money m1 = new Money(110.10, "pds");
        final Money m2 = new Money(220.50, "pds");
        final Money m3 = m1.add(m2);
        assertEquals(330.60, m3.doubleValue(), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddThrowsExceptionForDifferentCurrencies() {
        final Money m1 = new Money(100, "pds");
        final Money m2 = new Money(200, "uds");

        m1.add(m2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDoubleConstructorExpectsCurrencyToBeSpecified() {
        new Money(100.50, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLongConstructorExpectsCurrencyToBeSpecified() {
        new Money(100L, null);
    }

    @Test
    public void testCreateFromDouble() {
        final Money m1 = new Money(100.50, "pds");
        assertEquals("100.50", m1.getAmount().toString());
        assertEquals(100.50, m1.doubleValue(), 0.0);
    }

    @Test
    public void testDouble() {
        final Money m1 = new Money(100, "pds");
        assertEquals(100.0, m1.doubleValue(), 0.0);
    }

    @Test
    public void testDoubleConstructor() {
        final Money m1 = new Money(100.15, "pds");
        assertEquals(100.15, m1.doubleValue(), 0.0);
    }

    @Test
    public void testDoubleRoundingDown() {
        final Money m1 = new Money(100.154, "pds");
        assertEquals(100.15, m1.doubleValue(), 0.0);
    }

    @Test
    public void testDoubleRoundingUp() {
        final Money m1 = new Money(100.156, "pds");
        assertEquals(100.16, m1.doubleValue(), 0.0);
    }

    @Test
    public void testEqualsObject() {
        final Money m1 = new Money(100.25, "pds");
        final Money m2 = new Money(100.25, "pds");
        assertTrue(m1.equals(m2));
    }

    @Test
    public void testEqualsObjectFailsWithDifferentAmounts() {
        final Money m1 = new Money(100, "pds");
        final Money m2 = new Money(101, "pds");
        assertFalse(m1.equals(m2));
    }

    @Test
    public void testEqualsObjectFailsWithDifferentCurrencies() {
        final Money m1 = new Money(100, "pds");
        final Money m2 = new Money(100, "usd");
        assertFalse(m1.equals(m2));
    }

    @Test
    public void testGetAmount() {
        final Money m1 = new Money(100, "pds");
        assertEquals(BigDecimal.valueOf(10000, 2), m1.getAmount());
    }

    @Test
    public void testHasSameCurrency() {
        final Money m1 = new Money(100, "pds");
        final Money m2 = new Money(200, "pds");
        final Money m3 = new Money(200, "usd");
        assertTrue(m1.hasSameCurrency(m2));
        assertFalse(m1.hasSameCurrency(m3));
    }

    @Test
    public void testIsEqualTo() {
        final Money m1 = new Money(100, "pds");
        final Money m2 = new Money(100, "pds");
        assertTrue(m1.isEqualTo(m2));
    }

    @Test
    public void testIsEqualToFailsWithDifferentAmount() {
        final Money m1 = new Money(100, "pds");
        final Money m2 = new Money(101, "pds");
        assertFalse(m1.isEqualTo(m2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsEqualToThrowsExceptionWithDifferentCurrencies() {
        final Money m1 = new Money(100, "pds");
        final Money m2 = new Money(100, "usd");

        m1.isEqualTo(m2);
    }

    @Test
    public void testIsGreaterThanZero() {
        final Money m1 = new Money(1, "usd");
        assertTrue(m1.isGreaterThanZero());
    }

    @Test
    public void testIsGreaterThanZeroFailsWhenZero() {
        final Money m1 = new Money(0, "usd");
        assertFalse(m1.isGreaterThanZero());
    }

    @Test
    public void testIsLessThan() {
        final Money m1 = new Money(98, "pds");
        final Money m2 = new Money(100, "pds");
        assertTrue(m1.isLessThan(m2));
    }

    @Test
    public void testIsLessThanFails() {
        final Money m1 = new Money(98, "pds");
        final Money m2 = new Money(100, "pds");
        assertFalse(m2.isLessThan(m1));
    }

    @Test
    public void testIsLessThanZero() {
        final Money m1 = new Money(-1, "usd");
        assertTrue(m1.isLessThanZero());
    }

    @Test
    public void testIsLessThanZeroFailsWhenZero() {
        final Money m1 = new Money(0, "usd");
        assertFalse(m1.isLessThanZero());
    }

    public void testIsLessThrowsExceptionWithDifferentCurrencies() {
        final Money m1 = new Money(98, "pds");
        final Money m2 = new Money(100, "usd");
        try {
            m2.isLessThan(m1);
            fail();
        } catch (final IllegalArgumentException expected) {
        }
    }

    @Test
    public void testIsZero() {
        final Money m1 = new Money(0, "usd");
        assertTrue(m1.isZero());
    }

    @Test
    public void testIsZeroFailsWhenPositive() {
        final Money m1 = new Money(1, "usd");
        assertFalse(m1.isZero());
    }

    @Test
    public void testSubtract() {
        final Money m1 = new Money(300, "pds");
        final Money m2 = new Money(100, "pds");
        final Money m3 = m1.subtract(m2);
        assertEquals(200.0, m3.doubleValue(), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubtractThrowsExceptionForDifferentCurrencies() {
        final Money m1 = new Money(100, "pds");
        final Money m2 = new Money(200, "uds");

        m1.subtract(m2);
    }

}
