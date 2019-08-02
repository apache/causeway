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

package org.apache.isis.runtime.system;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import org.apache.isis.metamodel.commons.ClassExtensions;
import org.apache.isis.metamodel.commons.MethodUtil;
import org.apache.isis.metamodel.methodutils.MethodScope;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class MethodFinderUtilsTest {

    private static Method staticMethod;
    private static Method instanceMethod;

    static {
        staticMethod = ClassExtensions.getMethodElseNull(MethodFinderUtilsTest.class, "someStaticMethod");
        instanceMethod = ClassExtensions.getMethodElseNull(MethodFinderUtilsTest.class, "someInstanceMethod");
    }

    private final MethodScope methodScope;
    private final Method method;
    private final boolean result;

    @Before
    public void setUp() {
        assertThat(staticMethod, is(not(nullValue())));
        assertThat(instanceMethod, is(not(nullValue())));
    }

    @Parameters
    public static Collection parameters() {
        return Arrays.asList(new Object[][] { { MethodScope.OBJECT, staticMethod, false }, { MethodScope.CLASS, staticMethod, true }, { MethodScope.OBJECT, instanceMethod, true }, { MethodScope.CLASS, instanceMethod, false }, });
    }

    public static void someStaticMethod() {
    }

    public void someInstanceMethod() {
    }

    public MethodFinderUtilsTest(final MethodScope methodScope, final Method method, final boolean result) {
        this.methodScope = methodScope;
        this.method = method;
        this.result = result;
    }

    @Test
    public void all() {
        assertThat(MethodUtil.inScope(method, methodScope), is(result));
    }

}
