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

package org.apache.isis.runtimes.dflt.runtime.system.specpeer;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public class ExpectedSetTest extends TestCase {

    public static void main(final String[] args) {
        junit.textui.TestRunner.run(ExpectedSetTest.class);
    }

    private ExpectedSet set;

    @Override
    protected void setUp() throws Exception {
        set = new ExpectedSet();

        set.addExpected("test");
        set.addExpected("expected");
        set.addExpected("list");
    }

    public void testAddActuals() {
        set.addActual("test");
        set.addActual("expected");
        set.addActual("list");
        set.verify();
    }

    public void testAddActualsInWrongOrder() {
        try {
            set.addActual("test");
            set.addActual("list");
        } catch (final AssertionFailedError e) {
            return;
        }
        fail();
    }

    public void testAddInvalidActuals() {
        try {
            set.addActual("not");
            set.addActual("part");
        } catch (final AssertionFailedError e) {
            return;
        }
        fail();
    }

    public void testAddTooFewActuals() {
        try {
            set.addActual("test");
            set.addActual("expected");
            set.verify();
        } catch (final AssertionFailedError e) {
            return;
        }
        fail();
    }

    public void testAddTooManyActuals() {
        try {
            set.addActual("test");
            set.addActual("expected");
            set.addActual("list");
            set.addActual("overrun");
        } catch (final AssertionFailedError e) {
            return;
        }
        fail();
    }

    public void testNoActuals() {
        try {
            set.verify();
        } catch (final AssertionFailedError e) {
            return;
        }
        fail();
    }

    public void testNoExpectedNoActuals() {
        set = new ExpectedSet();
        set.verify();
    }

}
