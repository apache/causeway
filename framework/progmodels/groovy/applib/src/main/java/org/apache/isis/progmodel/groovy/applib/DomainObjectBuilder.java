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
package org.apache.isis.progmodel.groovy.applib;

import groovy.util.ObjectGraphBuilder;

import java.util.Map;

import org.apache.isis.applib.DomainObjectContainer;

public class DomainObjectBuilder<T> extends ObjectGraphBuilder {

    private final DomainObjectContainer container;

    public DomainObjectBuilder(final DomainObjectContainer container, final Class<?>... classes) {
        this.container = container;
        final ClassNameResolver classNameResolver = new ClassNameResolver() {
            @Override
            public String resolveClassname(final String classname) {
                for (final Class<?> cls : classes) {
                    final String packageName = cls.getPackage().getName();
                    final String fqcn = packageName + "." + upperFirst(classname);
                    try {
                        Thread.currentThread().getContextClassLoader().loadClass(fqcn);
                        return fqcn;
                    } catch (final ClassNotFoundException ex) {
                        // continue
                    }
                }
                throw new RuntimeException("could not resolve " + classname + "'");
            }

        };
        this.setClassNameResolver(classNameResolver);
        final NewInstanceResolver instanceResolver = new DefaultNewInstanceResolver() {
            @SuppressWarnings("unchecked")
            @Override
            public Object newInstance(final Class cls, final Map attributes) throws InstantiationException, IllegalAccessException {
                return container.newTransientInstance(cls);
            }
        };
        this.setNewInstanceResolver(instanceResolver);
    }

    private static String upperFirst(final String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Object createNode(final Object arg0, final Map arg1, final Object arg2) {
        final Object domainObject = super.createNode(arg0, arg1, arg2);
        container.persistIfNotAlready(domainObject);
        return domainObject;
    }
}
