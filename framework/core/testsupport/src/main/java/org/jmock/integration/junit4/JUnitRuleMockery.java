/**
Copyright (c) 2000-2007, jMock.org
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of
conditions and the following disclaimer. Redistributions in binary form must reproduce
the above copyright notice, this list of conditions and the following disclaimer in
the documentation and/or other materials provided with the distribution.

Neither the name of jMock nor the names of its contributors may be used to endorse
or promote products derived from this software without specific prior written
permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
DAMAGE.
 */
package org.jmock.integration.junit4;

import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.List;

import org.jmock.auto.internal.AllDeclaredFields;
import org.jmock.auto.internal.Mockomatic;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * A <code>JUnitRuleMockery</code> is a JUnit Rule that manages JMock
 * expectations and allowances, and asserts that expectations have been met
 * after each test has finished. To use it, add a field to the test class (note
 * that you don't have to specify <code>@RunWith(JMock.class)</code> any more).
 * For example,
 * 
 * <pre>
 * public class ATestWithSatisfiedExpectations {
 *     &#064;Rule
 *     public final JUnitRuleMockery context = new JUnitRuleMockery();
 *     private final Runnable runnable = context.mock(Runnable.class);
 * 
 *     &#064;Test
 *     public void doesSatisfyExpectations() {
 *         context.checking(new Expectations() {
 *             {
 *                 oneOf(runnable).run();
 *             }
 *         });
 * 
 *         runnable.run();
 *     }
 * }
 * </pre>
 * 
 * Note that the Rule field must be declared public and as a
 * <code>JUnitRuleMockery</code> (not a <code>Mockery</code>) for JUnit to
 * recognise it, as it's checked statically.
 * 
 * @author smgf
 */
public class JUnitRuleMockery extends JUnit4Mockery implements MethodRule {
    private final Mockomatic mockomatic = new Mockomatic(this);

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                prepare(target);
                base.evaluate();
                assertIsSatisfied();
            }

            private void prepare(final Object target) {
                final List<Field> allFields = AllDeclaredFields.in(target.getClass());
                assertOnlyOneJMockContextIn(allFields);
                fillInAutoMocks(target, allFields);
            }

            private void assertOnlyOneJMockContextIn(final List<Field> allFields) {
                Field contextField = null;
                for (final Field field : allFields) {
                    if (JUnitRuleMockery.class.isAssignableFrom(field.getType())) {
                        if (null != contextField) {
                            fail("Test class should only have one JUnitRuleMockery field, found " + contextField.getName() + " and " + field.getName());
                        }
                        contextField = field;
                    }
                }
            }

            private void fillInAutoMocks(final Object target, final List<Field> allFields) {
                mockomatic.fillIn(target, allFields);
            }
        };
    }
}