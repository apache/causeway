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

package org.apache.isis.core.runtime.test.dom;

public class JavaActionTestObject {
    private boolean actionCalled = false;

    public void actionMethod() {
        actionCalled = true;
    }

    public static String nameMethod() {
        return "about for test";
    }

    public boolean invisibleMethod() {
        return true;
    }

    public String validMethod() {
        return "invalid";
    }

    public void actionWithParam(final String str) {
    }

    public static boolean[] mandatoryMethod(final String str) {
        return new boolean[] { true };
    }

    public static String[] labelMethod(final String str) {
        return new String[] { "label" };
    }

    public boolean actionCalled() {
        return actionCalled;
    }
}
