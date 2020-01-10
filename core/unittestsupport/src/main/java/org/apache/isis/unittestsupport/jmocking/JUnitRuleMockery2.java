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
package org.apache.isis.unittestsupport.jmocking;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.internal.AllDeclaredFields;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;

import static org.junit.Assert.fail;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.environment.IsisSystemEnvironment;

import junit.framework.AssertionFailedError;


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
public class JUnitRuleMockery2 extends JUnit4Mockery implements MethodRule {

    /**
     * Factory method.
     */
    public static JUnitRuleMockery2 createFor(final Mode mode) {
        
        _Context.computeIfAbsent(IsisSystemEnvironment.class, IsisSystemEnvironment::new)
        .setUnitTesting(true);
        
        final JUnitRuleMockery2 jUnitRuleMockery2 = new JUnitRuleMockery2();
        if (mode == Mode.INTERFACES_AND_CLASSES) {
            jUnitRuleMockery2.setImposteriser(Imposterisers.getDefault());
        }
        jUnitRuleMockery2.setThreadingPolicy(new Synchroniser());
        return jUnitRuleMockery2;
    }


    /**
     * Annotate the field that references the class under test;
     * is automatically instantiated and autowired by this class,
     * accessible to the test using {@link JUnitRuleMockery2#getClassUnderTest()}.
     */
    @Retention(RUNTIME)
    @Target(FIELD)
    public static @interface ClassUnderTest {}

    /**
     * Annotate fields annotated with {@link Mock}, to indicate that they should be set up
     * with an {@link Expectations#ignoring(Object)} expectation.
     */
    @Retention(RUNTIME)
    @Target(FIELD)
    public static @interface Ignoring {}

    /**
     * Annotate fields annotated with {@link Mock}, to indicate that they should be set up
     * with an {@link Expectations#allowing(Object)} expectation.
     */
    @Retention(RUNTIME)
    @Target(FIELD)
    public static @interface Allowing {}

    /**
     * Annotate fields annotated with {@link Mock}, to indicate that they should be set up
     * with an {@link Expectations#never(Object)} expectation.
     */
    @Retention(RUNTIME)
    @Target(FIELD)
    public static @interface Never {}

    /**
     * Annotate fields annotated with {@link Mock}, to indicate that they should be set up
     * with an {@link Expectations#one(Object)} expectation.
     */
    @Retention(RUNTIME)
    @Target(FIELD)
    public static @interface One {}


    /**
     * Annotate fields annotated with {@link Mock}, to indicate that they should be set up
     * to check the specified {@link ExpectationsOn expectation}.
     */
    @Retention(RUNTIME)
    @Target(FIELD)
    public static @interface Checking {
        Class<? extends ExpectationsOn> value() default ExpectationsOn.class;
    }

    public static enum Mode {
        INTERFACES_ONLY, INTERFACES_AND_CLASSES;
    }

    private final MyMockomatic mockomatic = new MyMockomatic(this);
    private final MutablePicoContainer container = new PicoBuilder().withConstructorInjection().withSetterInjection().build();
    private Class<?> cutType;

    private JUnitRuleMockery2() {
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                prepare(target);
                base.evaluate();
                assertIsSatisfied();
            }

            private void prepare(final Object target) throws IllegalAccessException {
                final List<Field> allFields = AllDeclaredFields.in(target.getClass());
                assertOnlyOneJMockContextIn(allFields);
                List<Object> mocks = fillInAutoMocks(target, allFields);
                Field cutField = locateClassUnderTestFieldIfAny(allFields);
                if (cutField != null) {
                    cutType = cutField.getType();
                    for (Object mock : mocks) {
                        container.addComponent(mock);
                    }
                    container.addComponent(cutType);

                    final Object cut = container.getComponent(cutType);
                    cutField.setAccessible(true);
                    cutField.set(target, cut);

                } else {
                    cutType = null;
                }

            }

            private void assertOnlyOneJMockContextIn(final List<Field> allFields) {
                Field contextField = null;
                for (final Field field : allFields) {
                    if (JUnitRuleMockery2.class.isAssignableFrom(field.getType())) {
                        if (null != contextField) {
                            fail("Test class should only have one JUnitRuleMockery2 field, found " + contextField.getName() + " and " + field.getName());
                        }
                        contextField = field;
                    }
                }
            }


            protected Field locateClassUnderTestFieldIfAny(final List<Field> allFields) {
                Field cutField = null;
                for (final Field field : allFields) {
                    if(field.getAnnotation(ClassUnderTest.class) != null) {
                        if (null != cutField) {
                            fail("Test class should only have one field annotated with @ClassUnderTest, found " + cutField.getName() + " and " + field.getName());
                        }
                        cutField = field;
                    }
                }
                return cutField;
            }

            private List<Object> fillInAutoMocks(final Object target, final List<Field> allFields) {
                return mockomatic.fillIn(target, allFields);
            }
        };
    }


    public <T> T getClassUnderTest() {
        if(cutType == null) {
            throw new IllegalStateException("No field annotated @ClassUnderTest was found");
        }
        return _Casts.uncheckedCast( container.getComponent(cutType) );
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


    /**
     * Require one interaction
     * @return
     */
    public Object oneOf(final Object mock) {
        checking(new Expectations() {
            {
                oneOf(mock);
            }
        });
        return mock;
    }

    /**
     * Require one interaction
     * @return
     * @deprecated use {@link #oneOf(Object)} instead
     */
    @Deprecated
    public Object one(final Object mock) {
        return oneOf(mock);
    }

    public static class ExpectationsOn<T> extends Expectations {
        public ExpectationsOn(Object mock) {
            this.mockObj = _Casts.uncheckedCast( mock );
        }
        private T mockObj;
        public T mock() {
            return mockObj;
        }
    }

    public <T> T checking(T mock, Class<? extends ExpectationsOn<T>> expectationsClass) {
        try {
            Constructor<? extends ExpectationsOn<T>> constructor = expectationsClass.getConstructor(Object.class);
            ExpectationsOn<T> expectations = constructor.newInstance(mock);
            checking(expectations);
            return mock;
        } catch (Exception e) {
            throw new AssertionFailedError("Unable to instantiate expectations class '" + expectationsClass.getName() + "'");
        }
    }


}
