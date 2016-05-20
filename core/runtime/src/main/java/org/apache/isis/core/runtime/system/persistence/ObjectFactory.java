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

package org.apache.isis.core.runtime.system.persistence;

import java.lang.reflect.Modifier;

import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectInstantiationException;

public class ObjectFactory {

    private final PersistenceSession persistenceSession;
    private final ServicesInjector servicesInjector;

    public ObjectFactory(
            final PersistenceSession persistenceSession,
            final ServicesInjector servicesInjector) {
        this.persistenceSession = persistenceSession;
        this.servicesInjector = servicesInjector;
    }

    public <T> T instantiate(final Class<T> cls) throws ObjectInstantiationException {

        if (servicesInjector == null) {
            throw new IllegalStateException("ServicesInjector is not available (no open session)");
        }
        if (Modifier.isAbstract(cls.getModifiers())) {
            throw new ObjectInstantiationException("Cannot create an instance of an abstract class: " + cls);
        }
        final T newInstance;
        if (Modifier.isAbstract(cls.getModifiers())) {
            throw new ObjectInstantiationException("Cannot create an instance of an abstract class: " + cls);
        }
        try {
            newInstance = cls.newInstance();
        } catch (final IllegalAccessException | InstantiationException e) {
            throw new ObjectInstantiationException(e);
        }

        servicesInjector.injectServicesInto(newInstance);
        return newInstance;
    }

}
