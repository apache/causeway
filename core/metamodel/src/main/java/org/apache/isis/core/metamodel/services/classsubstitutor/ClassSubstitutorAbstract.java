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
package org.apache.isis.core.metamodel.services.classsubstitutor;

import java.util.Objects;
import java.util.Set;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.proxy._ProxyFactoryService;
import org.apache.isis.commons.internal.reflection._ClassCache;
import org.apache.isis.core.metamodel.commons.ClassUtil;

import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class ClassSubstitutorAbstract implements ClassSubstitutor {

    private final _ClassCache classCache = _ClassCache.getInstance();

    @Override
    public final Substitution getSubstitution(@NonNull final Class<?> cls) {
        val replacement = getReplacement(cls);
        if(Objects.equals(cls, replacement)) {
            return Substitution.passThrough(); // indifferent
        }
        if(replacement==null) {
            return Substitution.neverIntrospect();
        }
        return Substitution.replaceWith(replacement) ;
    }

    protected Class<?> getReplacement(final Class<?> cls) {

        if(cls == null) {
            return null;
        }

        if(proxyPackageNamesToSkip.stream()
                .anyMatch(packageName -> cls.getName().startsWith(packageName))) {
            return getReplacement(cls.getSuperclass());
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
        if (ClassUtil.directlyImplements(cls, _ProxyFactoryService.ProxyEnhanced.class)) {
            // REVIEW: arguably this should now go back to the ClassSubstitorRegistry
            return getReplacement(cls.getSuperclass());
        }

        try {
            // guard against cannot introspect
            classCache.add(cls);
        } catch (Throwable e) {

            log.warn("cannot introspect type {}, adding it to list of ignored types;\n reason: {}",
                    cls.getName(),
                    _Exceptions.getMessage(e));

            classesToIgnore.add(cls);
            return null;
        }

        return cls;
    }

    // -- HELPERS

    private final Set<Class<?>> classesToIgnore = _Sets.newConcurrentHashSet();
    private final Set<String> classNamesToIgnore = _Sets.newHashSet();
    private final Set<String> packageNamesToIgnore = _Sets.newHashSet();
    private final Set<String> proxyPackageNamesToSkip = _Sets.newHashSet();


    /**
     * For any classes registered as ignored, {@link #getReplacement(Class)} will
     * return <tt>null</tt>.
     */
    protected void ignoreClass(final String className) {
        classNamesToIgnore.add(className);
    }

    protected void ignorePackage(final String packageName) {
        packageNamesToIgnore.add(packageName);
    }

    /**
     * Any classes in these package represent a proxy (eg DN-enhanced bytecode).
     * We skip the proxy class itself but traverse up to the supertype.
     */
    protected void skipProxyPackage(final String packageName) {
        proxyPackageNamesToSkip.add(packageName);
    }

    private boolean shouldIgnore(final Class<?> cls) {
        if (cls.isArray()) {
            return shouldIgnore(cls.getComponentType());
        }

        // ignore any classes
        if(cls.getAnnotation(Programmatic.class) != null) {
            return true;
        }

        val className = cls.getName();

        try{
            return classesToIgnore.contains(cls)
                    || classNamesToIgnore.contains(cls.getCanonicalName())
                    || packageNamesToIgnore.stream().anyMatch(className::startsWith);

        } catch(NoClassDefFoundError e) {

            try{
                if(cls.isAnonymousClass()) {
                    return shouldIgnore(cls.getSuperclass());
                } else {
                    return false;
                }
            } catch(NoClassDefFoundError ex) {
                return true;
            }
        }
    }

}
