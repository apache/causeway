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
import org.apache.isis.core.metamodel.services.ServicesInjectorSpi;
import org.apache.isis.core.metamodel.spec.ObjectInstantiationException;
import org.apache.isis.core.runtime.system.context.IsisContext;

public class ObjectFactory {

    private final Mode mode;

    public enum Mode {
        /**
         * Fail if no {@link ObjectFactory#getServicesInjector() services injector} has been injected.
         */
        STRICT,
        /**
         * Ignore if no {@link ObjectFactory#getServicesInjector() services injector} has been injected
         * (intended for testing only).
         */
        RELAXED
    }

    public ObjectFactory() {
        this(Mode.STRICT);
    }

    public ObjectFactory(final Mode mode) {
        this.mode = mode;
    }

    public <T> T instantiate(final Class<T> cls) throws ObjectInstantiationException {

        if (mode == Mode.STRICT && getServicesInjector() == null) {
            throw new IllegalStateException("ServicesInjector is not available (no open session)");
        }
        if (Modifier.isAbstract(cls.getModifiers())) {
            throw new ObjectInstantiationException("Cannot create an instance of an abstract class: " + cls);
        }
        final T newInstance = doInstantiate(cls);

        if (getServicesInjector() != null) {
            getServicesInjector().injectServicesInto(newInstance);
        }
        return newInstance;
    }


    //region > doInstantiate

    /**
     * Simply instantiates reflectively.
     */
    protected <T> T doInstantiate(final Class<T> cls) throws ObjectInstantiationException {
        if (Modifier.isAbstract(cls.getModifiers())) {
            throw new ObjectInstantiationException("Cannot create an instance of an abstract class: " + cls);
        }
        try {
            return cls.newInstance();
        } catch (final IllegalAccessException | InstantiationException e) {
            throw new ObjectInstantiationException(e);
        }
    }
    //endregion

    //region > dependencies (looked up from context)

    private PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected ServicesInjectorSpi getServicesInjector() {
        return getPersistenceSession().getServicesInjector();
    }

    //endregion

}
