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
package org.apache.isis.testing.unittestsupport.applib.jmocking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.reflection._Reflect;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;

import org.jmock.Expectations;
import org.jmock.api.Imposteriser;
import org.jmock.api.Invocation;
import org.jmock.api.Invokable;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.action.VoidAction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import lombok.val;

class ImposteriserTestUsingCodegenPlugin_Test {

    private Imposteriser imposteriser = Imposterisers.getDefault();

    private Invokable invokable;
    @SuppressWarnings("unused")
    private Invocation invocation;

    @BeforeEach
    void setUp() throws Exception {
        _Context.clear();
        invokable = new Invokable() {
            @Override
            public Object invoke(Invocation invocation) throws Throwable {
                ImposteriserTestUsingCodegenPlugin_Test.this.invocation = invocation;
                return "result";
            }
        };
    }

    @AfterEach
    void tearDown() throws Exception {
        invokable = null;
        invocation = null;
    }

    @Test
    void happyCaseWhenJdkInterface() {
        assertTrue(imposteriser.canImposterise(Runnable.class));
        final Runnable imposter = imposteriser.imposterise(invokable, Runnable.class);
        assertNotNull(imposter);
        imposter.run();
    }

    @Test @Disabled("fails with surefire on jdk-11, not with eclipse") //TODO[2112] jdk-11 issue?
    void happyCaseWhenJdkClass() {
        assertTrue(imposteriser.canImposterise(Date.class));
        final Date imposter = imposteriser.imposterise(invokable, Date.class);
        assertNotNull(imposter);
        imposter.toString();
    }

    // class we want to mock, while making sure, that we have access to non public methods 
    static class NonPublicMethodStub {
        Integer getInteger() {
            return 1;
        }
    }

    @Test
    void imposteriserShouldBeUsableForMockery() {

        final JUnit4Mockery context = new JUnit4Mockery() {
            {
                setImposteriser(imposteriser);
            }
        };

        final NonPublicMethodStub mocked = context.mock(NonPublicMethodStub.class);

        context.checking(new Expectations() {{
            allowing(mocked).getInteger();
            will(returnValue(Integer.valueOf(2)));
        }});

        assertNotNull(mocked);
        assertNotNull(mocked.getInteger());
        assertEquals(2, mocked.getInteger().intValue());
    }

    // //////////////////////////////////////

    @Test
    void cannotImposterisePrimitiveType() {
        assertFalse(imposteriser.canImposterise(int.class));
    }

    @Test
    void cannotImposteriseVoidType() {
        assertFalse(imposteriser.canImposterise(void.class));
    }


    // //////////////////////////////////////


    public static abstract class AnAbstractNestedClass {
        public abstract String foo();
    }

    @Test
    void happyCaseWhenAbstractClass() {
        assertTrue(imposteriser.canImposterise(AnAbstractNestedClass.class));
        final AnAbstractNestedClass imposter = imposteriser.imposterise(invokable, AnAbstractNestedClass.class);
        assertNotNull(imposter);
        assertEquals("result", imposter.foo());
    }


    // //////////////////////////////////////



    public static class AnInnerClass {
        public String foo() {return "original result";}
    }

    @Test
    void happyCaseWhenNonFinalInstantiableClass() {
        assertTrue(imposteriser.canImposterise(AnInnerClass.class));
        final AnInnerClass imposter = imposteriser.imposterise(invokable, AnInnerClass.class);
        assertNotNull(imposter);
        assertEquals("result", imposter.foo());
    }



    // //////////////////////////////////////


    public static final class AFinalClass {
        public String foo() {return "original result";}
    }

    @Test
    void cannotImposteriseWhenFinalInstantiableClasses() {
        assertFalse(imposteriser.canImposterise(AFinalClass.class));
    }


    // //////////////////////////////////////



    public static class AClassWithAPrivateConstructor {
        private AClassWithAPrivateConstructor(String someArgument) {}

        public String foo() {return "original result";}
    }

    @Test
    void happyCaseWhenClassWithNonPublicConstructor() {
        assertTrue(imposteriser.canImposterise(AClassWithAPrivateConstructor.class));
        AClassWithAPrivateConstructor imposter =
                imposteriser.imposterise(invokable, AClassWithAPrivateConstructor.class);

        assertNotNull(imposter);
        assertEquals("result", imposter.foo());
    }


    // //////////////////////////////////////



    public static class ConcreteClassWithConstructorAndInstanceInitializer {
        {
            shouldNotBeCalled("instance initializer");
        }

        public ConcreteClassWithConstructorAndInstanceInitializer() {
            shouldNotBeCalled("constructor");
        }

        public String foo() {
            shouldNotBeCalled("method foo()");
            return null; // never reached
        }

        private static void shouldNotBeCalled(String exceptionMessageIfCalled) {
            throw new IllegalStateException(exceptionMessageIfCalled + " should not be called");
        }
    }

    @Test
    void happyCaseWhenConcreteClassWithConstructorAndInitialisersThatShouldNotBeCalled() {
        assertTrue(imposteriser.canImposterise(ConcreteClassWithConstructorAndInstanceInitializer.class));
        ConcreteClassWithConstructorAndInstanceInitializer imposter =
                imposteriser.imposterise(invokable, ConcreteClassWithConstructorAndInstanceInitializer.class);
        assertNotNull(imposter);
        assertEquals("result", imposter.foo());
    }



    // //////////////////////////////////////



    public interface AnInterface {
        String foo();
    }

    @Test
    void happyCaseWhenCustomInterface() {
        assertTrue(imposteriser.canImposterise(AnInterface.class));
        AnInterface imposter = imposteriser.imposterise(invokable, AnInterface.class);

        assertNotNull(imposter);
        assertEquals("result", imposter.foo());
    }



    // //////////////////////////////////////




    @Test
    void happyCaseWhenClassInASignedJarFile() throws Exception {
        File jarFile = new File("src/test/resources/signed.jar").getAbsoluteFile();

        assertTrue(jarFile.exists());

        URL jarURL = jarFile.toURI().toURL();
        try(URLClassLoader loader = new URLClassLoader(new URL[]{jarURL})){
            Class<?> typeInSignedJar = loader.loadClass("TypeInSignedJar");

            assertTrue(imposteriser.canImposterise(typeInSignedJar));
            Object o = imposteriser.imposterise(new VoidAction(), typeInSignedJar);

            assertTrue(typeInSignedJar.isInstance(o));
        }
    }



    // //////////////////////////////////////


    public static class ClassWithFinalToStringMethod {
        @Override
        public final String toString() {
            return "you can't override me!";
        }
    }

    // See issue JMOCK-150
    @Test
    void cannotImposteriseAClassWithAFinalToStringMethod() {
        assertFalse(imposteriser.canImposterise(ClassWithFinalToStringMethod.class));

        try {
            imposteriser.imposterise(new VoidAction(), ClassWithFinalToStringMethod.class);
            fail("should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException expected) {

        }
    }





    // //////////////////////////////////////


    public interface EmptyInterface {}

    public interface AnInterface2 {
        String foo();
    }


    // See issue JMOCK-145
    @Test @Disabled("fails with surefire on jdk-11, not with eclipse") //TODO[2112] jdk-11 issue?
    void worksAroundBugInCglibWhenAskedToImposteriseObject() {
        imposteriser.imposterise(new VoidAction(), Object.class);
        imposteriser.imposterise(new VoidAction(), Object.class, EmptyInterface.class);
        imposteriser.imposterise(new VoidAction(), Object.class, AnInterface2.class);
    }

    // //////////////////////////////////////


    // See issue JMOCK-256 (GitHub #36)
    @Test @Disabled("fails with surefire on jdk-11, not with eclipse") //TODO[2112] jdk-11 issue?
    void doesntDelegateFinalizeMethod() throws Exception {
        Invokable failIfInvokedAction = new Invokable() {
            @Override
            public Object invoke(Invocation invocation) throws Throwable {
                fail("invocation should not have happened");
                return null;
            }
        };

        val imposter = imposteriser.imposterise(failIfInvokedAction, Object.class);
        _Reflect.invokeMethodOn(Object.class.getDeclaredMethod("finalize"), imposter);
        
    }

}