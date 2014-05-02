/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.unittestsupport.jmocking;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.jmock.api.Imposteriser;
import org.jmock.api.Invocation;
import org.jmock.api.Invokable;
import org.jmock.lib.JavaReflectionImposteriser;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

public class JavassistImposteriser implements Imposteriser {

    public static final Imposteriser INSTANCE = new JavassistImposteriser();

    private final Imposteriser reflectionImposteriser = new JavaReflectionImposteriser();
    private final Objenesis objenesis = new ObjenesisStd();

    private JavassistImposteriser() {
    }


    public boolean canImposterise(Class<?> mockedType) {

        if(mockedType.isInterface()) {
            return reflectionImposteriser.canImposterise(mockedType);
        }

        return !mockedType.isPrimitive() &&
               !Modifier.isFinal(mockedType.getModifiers()) &&
               !toStringMethodIsFinal(mockedType);
    }
    
    public <T> T imposterise(final Invokable mockObject, final Class<T> mockedType, Class<?>... ancilliaryTypes) {
        if (!canImposterise(mockedType)) {
            throw new IllegalArgumentException(mockedType.getName() + " cannot be imposterized (either a primitive, or a final type or has final toString method)");
        }

        if(mockedType.isInterface()) {
            return reflectionImposteriser.imposterise(mockObject, mockedType, ancilliaryTypes);
        }

        try {
            setConstructorsAccessible(mockedType, true);

            final Class<?> proxyClass = proxyClass(mockedType, ancilliaryTypes);
            final Object proxy = proxy(proxyClass, mockObject);
            return mockedType.cast(proxy);
        } finally {
            setConstructorsAccessible(mockedType, false);
        }
    }

    // //////////////////////////////////////

    private static boolean toStringMethodIsFinal(Class<?> type) {
        try {
            Method toString = type.getMethod("toString");
            return Modifier.isFinal(toString.getModifiers());
            
        }
        catch (SecurityException e) {
            throw new IllegalStateException("not allowed to reflect on toString method", e);
        }
        catch (NoSuchMethodException e) {
            throw new Error("no public toString method found", e);
        }
    }

    private static void setConstructorsAccessible(Class<?> mockedType, boolean accessible) {
        for (Constructor<?> constructor : mockedType.getDeclaredConstructors()) {
            constructor.setAccessible(accessible);
        }
    }

    private Class<?> proxyClass(Class<?> mockedType, Class<?>... ancilliaryTypes) {

        final ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setFilter(new MethodFilter() {
            @Override
            public boolean isHandled(final Method m) {
                // ignore finalize() and als bridge methods
                return !m.getName().equals("finalize") || m.isBridge();
            }
        });

        proxyFactory.setSuperclass(mockedType);
        proxyFactory.setInterfaces(ancilliaryTypes);

        return proxyFactory.createClass();

        // original cglib code:

    //        final Enhancer enhancer = new Enhancer() {
    //            @Override
    //            @SuppressWarnings("unchecked")
    //            protected void filterConstructors(Class sc, List constructors) {
    //                // Don't filter
    //            }
    //        };
    //        enhancer.setClassLoader(SearchingClassLoader.combineLoadersOf(mockedType, ancilliaryTypes));
    //        enhancer.setUseFactory(true);
    //        if (mockedType.isInterface()) {
    //            enhancer.setSuperclass(Object.class);
    //            enhancer.setInterfaces(prepend(mockedType, ancilliaryTypes));
    //        }
    //        else {
    //            enhancer.setSuperclass(mockedType);
    //            enhancer.setInterfaces(ancilliaryTypes);
    //        }
    //        enhancer.setCallbackTypes(new Class[]{InvocationHandler.class, NoOp.class});
    //        enhancer.setCallbackFilter(IGNORE_BRIDGE_METHODS);
    //        if (mockedType.getSigners() != null) {
    //            enhancer.setNamingPolicy(NAMING_POLICY_THAT_ALLOWS_IMPOSTERISATION_OF_CLASSES_IN_SIGNED_PACKAGES);
    //        }
    //
    //        try {
    //            return enhancer.createClass();
    //        }
    //        catch (CodeGenerationException e) {
    //            // Note: I've only been able to manually test this.  It exists to help people writing
    //            //       Eclipse plug-ins or using other environments that have sophisticated class loader
    //            //       structures.
    //            throw new IllegalArgumentException("could not imposterise " + mockedType, e);
    //        }

    }


    // original cglib code:

    //    private static final NamingPolicy NAMING_POLICY_THAT_ALLOWS_IMPOSTERISATION_OF_CLASSES_IN_SIGNED_PACKAGES = new DefaultNamingPolicy() {
    //        @Override
    //        public String getClassName(String prefix, String source, Object key, Predicate names) {
    //            return "org.jmock.codegen." + super.getClassName(prefix, source, key, names);
    //        }
    //    };
    //
    //    private static final CallbackFilter IGNORE_BRIDGE_METHODS = new CallbackFilter() {
    //        public int accept(Method method) {
    //            return method.isBridge() ? 1 : 0;
    //        }
    //    };


    private Object proxy(Class<?> proxyClass, final Invokable mockObject) {

        final ProxyObject proxyObject = (ProxyObject) objenesis.newInstance(proxyClass);
        proxyObject.setHandler(new MethodHandler() {
            @Override
            public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
                return mockObject.invoke(new Invocation(self, thisMethod, args));
            }
        });

        return proxyObject;

        // original cglib code:

        //        final Factory proxy = (Factory)objenesis.newInstance(proxyClass);
        //        proxy.setCallbacks(new Callback[] {
        //            new InvocationHandler() {
        //                public Object invoke(Object receiver, Method method, Object[] args) throws Throwable {
        //                    return mockObject.invoke(new Invocation(receiver, method, args));
        //                }
        //            },
        //            NoOp.INSTANCE
        //        });
        //        return proxy;
    }

    private static Class<?>[] combine(Class<?> first, Class<?>... rest) {
        Class<?>[] all = new Class<?>[rest.length+1];
        all[0] = first;
        System.arraycopy(rest, 0, all, 1, rest.length);
        return all;
    }
    
    //public static class ClassWithSuperclassToWorkAroundCglibBug {}
}
