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

package org.apache.isis.core.testsupport.jmock;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;

/**
 * Use as a <tt>@Rule</tt>, meaning that the <tt>@RunWith(JMock.class)</tt> can
 * be ignored.
 * 
 * <pre>
 * public class MyTest {
 * 
 *     &#064;Rule
 *     public final Junit4Mockery2 context = Junit4Mockery2.createFor(Mode.INTERFACES);
 * 
 * }
 * </pre>
 * 
 * <p>
 * The class also adds some convenience methods, and uses a factory method to
 * make it explicit whether the context can mock only interfaces or interfaces
 * and classes.
 */
public class JUnitRuleMockery2 extends JUnitRuleMockery {

    public static enum Mode {
        INTERFACES_ONLY, INTERFACES_AND_CLASSES;
    }

    /**
     * Factory method.
     */
    public static JUnitRuleMockery2 createFor(final Mode mode) {
        final JUnitRuleMockery2 jUnitRuleMockery2 = new JUnitRuleMockery2();
        if (mode == Mode.INTERFACES_AND_CLASSES) {
            jUnitRuleMockery2.setImposteriser(ClassImposteriser.INSTANCE);
        }
        return jUnitRuleMockery2;
    }

    private JUnitRuleMockery2() {
    }

    /**
     * Ignoring any interaction with the mock; an allowing/ignoring mock will be
     * returned in turn.
     */
    public <T> T ignoring(final T mock) {
        checking(new Expectations() {
            {
                ignoring(mock);
            }
        });
        return mock;
    }

    /**
     * Allow any interaction with the mock; an allowing mock will be returned in
     * turn.
     */
    public <T> T allowing(final T mock) {
        checking(new Expectations() {
            {
                allowing(mock);
            }
        });
        return mock;
    }

    /**
     * Prohibit any interaction with the mock.
     */
    public <T> T never(final T mock) {
        checking(new Expectations() {
            {
                never(mock);
            }
        });
        return mock;
    }

    /**
     * Ignore a set of mocks.
     */
    public void ignoring(Object... mocks) {
        for (Object mock : mocks) {
            ignoring(mock);
        }
    }
}
