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


package org.apache.isis.application.valueholder;

import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;


public class WholeNumberTests extends ValueTestCase {

    public static void main(final String[] args) {
        LogManager.getLoggerRepository().setThreshold(Level.OFF);
        TestRunner.run(new TestSuite(WholeNumberTests.class));
    }

    public void testEquals() throws NoSuchMethodException {
        WholeNumber one = new WholeNumber(189);
        WholeNumber two = new WholeNumber(189);
        WholeNumber three = new WholeNumber(1890);

        assertEquals(one, two);
        assertEquals(two, one);
        assertTrue(!one.equals(three));
    }
}
