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

import static org.apache.isis.commons.internal.functions._Functions.uncheckedFunction;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.context._Plugin;
import org.apache.isis.core.plugins.codegen.ProxyFactoryPlugin;
import org.jmock.api.Imposteriser;
import org.jmock.api.Invocation;
import org.jmock.api.Invokable;
import org.jmock.lib.action.VoidAction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DefaultImposteriserTest {

    private Imposteriser imposteriser = Imposterisers.getDefault();

    private Invokable invokable;
    @SuppressWarnings("unused")
	private Invocation invocation;
    
    @Before
    public void setUp() throws Exception {
        invokable = new Invokable() {
            @Override
            public Object invoke(Invocation invocation) throws Throwable {
                DefaultImposteriserTest.this.invocation = invocation;
                return "result";
            }
        };

        // -- loading codegen plugin by name (because not configured as a maven dependency) 
//        {
//        	String pluginFullyQualifiedClassName = "org.apache.isis.core.plugins.codegen.ProxyFactoryPluginUsingJavassist";
//        	// we are guessing where to find the pluginTarget
//        	File pluginTarget = getCoreTargetFolder("/core/plugins/codegen-javassist/");
//        	
//    		_Plugin.load(ProxyFactoryPlugin.class, pluginTarget, pluginFullyQualifiedClassName);
//        }
        
        {
        	// we are guessing where to find the pluginTarget
    		_Plugin.load(ProxyFactoryPlugin.class,
    				getCoreTargetFolder("/core/plugins/codegen-bytebuddy/"), 
    				"org.apache.isis.core.plugins.codegen.ProxyFactoryPluginUsingByteBuddy");
        }
        

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
    
    // -- HELPER
    
	/**
	 * TODO will break with java 9+
	 * @param relativeLocation 
	 * @return file-system path, where the frameworks core classes reside (after a build).
	 */
	private static File getCoreTargetFolder(String relativeLocation) {
		
		//if run with surefire, URL look like ...
		//file:/home/hobrom/isis/isis-master/core/unittestsupport/target/surefire/surefirebooter7883344605325794375.jar
		
		final AtomicReference<String> binaries = new AtomicReference<>("target");
		
		return Stream.of(((URLClassLoader )_Plugin.class.getClassLoader()).getURLs())
//		.peek(u->System.out.println("searching unittestsupport: "+u.toString()))
		.filter(url->url.toString().contains("/core/unittestsupport/"))
		.map(uncheckedFunction(URL::toURI, RuntimeException::new))
		.map(Paths::get)
		.map(Path::toFile)
		.map(uncheckedFunction(File::getCanonicalPath, RuntimeException::new))
		.map(s->s.replace('\\', '/'))
		.peek(s->{
			_Strings.splitThenStream(s.substring(s.indexOf("/core/unittestsupport/")), "/")
			.skip(3)
			.findFirst()
			.ifPresent(t->binaries.set(t));	
//			System.out.println("binaries ("+s+"): "+binaries.get());
		})
		.map(s->s.substring(0, s.indexOf("/core/unittestsupport/")))
		.map(s->new File(s, relativeLocation + "/" + binaries.get() + "/classes"))
		.findFirst()
		.orElseThrow(()->new RuntimeException("Failed to find file-system path, where the frameworks core classes reside."))
		;
	}
    

}