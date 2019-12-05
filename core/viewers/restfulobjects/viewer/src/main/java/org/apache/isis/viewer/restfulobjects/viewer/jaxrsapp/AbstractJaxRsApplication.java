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
package org.apache.isis.viewer.restfulobjects.viewer.jaxrsapp;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

public abstract class AbstractJaxRsApplication extends Application {

    private final Set<Object> singletons = new LinkedHashSet<Object>();
    private final Set<Class<?>> classes = new LinkedHashSet<Class<?>>();

    public AbstractJaxRsApplication() {
    }

    @Override
    public Set<Class<?>> getClasses() {
        return Collections.unmodifiableSet(classes);
    }

    @Override
    public Set<Object> getSingletons() {
        return Collections.unmodifiableSet(singletons);
    }

    protected boolean addClass(final Class<?> cls) {
        return classes.add(cls);
    }

    protected boolean addSingleton(final Object resource) {
        return singletons.add(resource);
    }

}