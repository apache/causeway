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

import java.util.Vector;

import junit.framework.Assert;


@SuppressWarnings("unchecked")
public class ExpectedSet {
	
	private final Vector expectedObjects = new Vector();
    private final Vector actualObjects = new Vector();

    public void addActual(final Object object) {
        actualObjects.addElement(object);
        Assert.assertTrue("More actuals than expected; only expected " + expectedObjects.size(),
                actualObjects.size() <= expectedObjects.size());
        Assert.assertEquals("Actual does not match expected.\n", expectedObjects.elementAt(actualObjects.size() - 1), object);
    }

    public void addExpected(final Object object) {
        expectedObjects.addElement(object);
    }

    public void verify() {
        Assert.assertTrue("Too few actuals added\n  Expected " + expectedObjects + "\n  but got " + actualObjects, actualObjects
                .size() == expectedObjects.size());
    }
}
