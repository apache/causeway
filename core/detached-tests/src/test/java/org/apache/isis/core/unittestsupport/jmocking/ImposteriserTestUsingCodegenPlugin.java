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
package org.apache.isis.core.unittestsupport.jmocking;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;

import org.jmock.Expectations;
import org.jmock.api.Imposteriser;
import org.jmock.api.Invocation;
import org.jmock.api.Invokable;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.action.VoidAction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.core.plugins.codegen.ProxyFactoryPlugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ImposteriserTestUsingCodegenPlugin {

    private Imposteriser imposteriser = Imposterisers.getDefault();

    private Invokable invokable;
    @SuppressWarnings("unused")
    private Invocation invocation;

    @Before
    public void setUp() throws Exception {
        invokable = new Invokable() {
            @Override
            public Object invoke(Invocation invocation) throws Throwable {
                ImposteriserTestUsingCodegenPlugin.this.invocation = invocation;
                return "result";
            }
        };
    }

    @After
    public void tearDown() throws Exception {
        invokable = null;
        invocation = null;
        _Context.clear(); // removes plugins from context
    }

    // //////////////////////////////////////

    @Test
    public void canLoadCodegenPlugin() throws MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        Assert.assertNotNull(ProxyFactoryPlugin.get());
    }

    // //////////////////////////////////////

    @Test
    public void happyCaseWhenJdkInterface() {
        assertTrue(imposteriser.canImposterise(Runnable.class));
        final Runnable imposter = imposteriser.imposterise(invokable, Runnable.class);
        assertNotNull(imposter);
        imposter.run();
    }

    @Test
    public void happyCaseWhenJdkClass() {
        assertTrue(imposteriser.canImposterise(Date.class));
        final Date imposter = imposteriser.imposterise(invokable, Date.class);
        assertNotNull(imposter);
        imposter.toString();
    }
    
    // class we want to mock, while making sure, that we have access to non public fields 
    static class NonPublicMethodStub {
        Integer getInteger() {
            return 1;
        }
    }
    
    @Test
    public void imposteriserShouldBeUsableForMockery() {
        
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
        
        Assert.assertNotNull(mocked);
        Assert.assertNotNull(mocked.getInteger());
        Assert.assertEquals(2, mocked.getInteger().intValue());
    }

    // //////////////////////////////////////

    @Test
    public void cannotImposterisePrimitiveType() {
        assertFalse(imposteriser.canImposterise(int.class));
    }

    @Test
    public void cannotImposteriseVoidType() {
        assertFalse(imposteriser.canImposterise(void.class));
    }


    // //////////////////////////////////////


    public static abstract class AnAbstractNestedClass {
        public abstract String foo();
    }

    @Test
    public void happyCaseWhenAbstractClass() {
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
    public void happyCaseWhenNonFinalInstantiableClass() {
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
    public void cannotImposteriseWhenFinalInstantiableClasses() {
        assertFalse(imposteriser.canImposterise(AFinalClass.class));
    }


    // //////////////////////////////////////



    public static class AClassWithAPrivateConstructor {
        @SuppressWarnings("unused")
        private AClassWithAPrivateConstructor(String someArgument) {}

        public String foo() {return "original result";}
    }

    @Test
    public void happyCaseWhenClassWithNonPublicConstructor() {
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
    public void happyCaseWhenConcreteClassWithConstructorAndInitialisersThatShouldNotBeCalled() {
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
    public void happyCaseWhenCustomInterface() {
        assertTrue(imposteriser.canImposterise(AnInterface.class));
        AnInterface imposter = imposteriser.imposterise(invokable, AnInterface.class);

        assertNotNull(imposter);
        assertEquals("result", imposter.foo());
    }



    // //////////////////////////////////////




    @Test
    public void happyCaseWhenClassInASignedJarFile() throws Exception {
        File jarFile = new File("src/test/resources/signed.jar");

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
    public void cannotImposteriseAClassWithAFinalToStringMethod() {
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
    @Test
    public void worksAroundBugInCglibWhenAskedToImposteriseObject() {
        imposteriser.imposterise(new VoidAction(), Object.class);
        imposteriser.imposterise(new VoidAction(), Object.class, EmptyInterface.class);
        imposteriser.imposterise(new VoidAction(), Object.class, AnInterface2.class);
    }

    private static Object invokeMethod(Object object, Method method, Object... args) throws IllegalAccessException, InvocationTargetException {
        method.setAccessible(true);
        return method.invoke(object, args);
    }



    // //////////////////////////////////////



    // See issue JMOCK-256 (Github #36)
    @Test
    public void doesntDelegateFinalizeMethod() throws Exception {
        Invokable failIfInvokedAction = new Invokable() {
            @Override
            public Object invoke(Invocation invocation) throws Throwable {
                fail("invocation should not have happened");
                return null;
            }
        };

        Object imposter = imposteriser.imposterise(failIfInvokedAction, Object.class);
        invokeMethod(imposter, Object.class.getDeclaredMethod("finalize"));
    }

}