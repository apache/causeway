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

package org.apache.isis.core.metamodel.specloader.classsubstitutor;

import java.util.Set;

import com.google.common.collect.Sets;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.core.commons.lang.ClassUtil;

/**
 * Provides capability to translate or ignore classes.
 */
public class ClassSubstitutor {

    // -- constructor

    public ClassSubstitutor() {
        ignore("org.apache.isis.applib.DomainObjectContainer"); //TODO [ahuber] still required?

        // ignore cglib
        ignore("net.sf.cglib.proxy.Factory");
        ignore("net.sf.cglib.proxy.MethodProxy");
        ignore("net.sf.cglib.proxy.Callback");

        // ignore javassist
        ignore("javassist.util.proxy.ProxyObject");
        ignore("javassist.util.proxy.MethodHandler");

    }


    // -- getClass(Class)

    public Class<?> getClass(final Class<?> cls) {

        if (cls == null) {
            return null;
        }

        // ignore datanucleus proxies
        if(cls.getName().startsWith("org.datanucleus")) {
            return getClass(cls.getSuperclass());
        }

        if (shouldIgnore(cls)) {
            return null;
        }

        // primarily to ignore unit test fixtures if they happen to be on the classpath.
        // (we can't simply ignore them; for example ApplicationFeatureType enum
        // uses anonymous inner classes and these *are* part of the metamodel)
        if(cls.isAnonymousClass()) {
            return cls.getSuperclass();
        }

        final Class<?> superclass = cls.getSuperclass();
        if(superclass != null && superclass.isEnum()) {
            return superclass;
        }
        if (ClassUtil.directlyImplements(cls, ProxyEnhanced.class)) {
            return getClass(cls.getSuperclass());
        }
        return cls;
    }



    // -- helpers

    private final Set<Class<?>> classesToIgnore = Sets.newHashSet();
    private final Set<String> classNamesToIgnore = Sets.newHashSet();

    /**
     * For any classes registered as ignored, {@link #getClass(Class)} will
     * return <tt>null</tt>.
     */
    private boolean ignore(final Class<?> q) {
        return classesToIgnore.add(q);
    }

    /**
     * For any classes registered as ignored, {@link #getClass(Class)} will
     * return <tt>null</tt>.
     */
    private boolean ignore(final String className) {
        return classNamesToIgnore.add(className);
    }

    private boolean shouldIgnore(final Class<?> cls) {
        if (cls.isArray()) {
            return shouldIgnore(cls.getComponentType());
        }

        // ignore any classes
        if(cls.getAnnotation(Programmatic.class) != null) {
            return true;
        }

        return classesToIgnore.contains(cls) || classNamesToIgnore.contains(cls.getCanonicalName());
    }




}
