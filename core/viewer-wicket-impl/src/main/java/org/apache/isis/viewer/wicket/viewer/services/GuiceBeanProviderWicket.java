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
package org.apache.isis.viewer.wicket.viewer.services;

import java.lang.annotation.Annotation;
import com.google.inject.Injector;
import com.google.inject.Key;
import org.apache.wicket.Application;
import org.apache.wicket.guice.GuiceInjectorHolder;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.guice.GuiceBeanProvider;

/**
 * An implementation of {@link org.apache.isis.applib.services.guice.GuiceBeanProvider}
 * that uses the Injector configured for Wicket
 */
@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
        )
public class GuiceBeanProviderWicket implements GuiceBeanProvider {

    @Programmatic
    @Override
    public <T> T lookup(final Class<T> beanType) {
        final Application application = Application.get();
        final GuiceInjectorHolder injectorHolder = application.getMetaData(GuiceInjectorHolder.INJECTOR_KEY);
        final Injector injector = injectorHolder.getInjector();
        return injector.getInstance(beanType);
    }

    @Programmatic
    @Override
    public <T> T lookup(final Class<T> beanType, final Annotation qualifier) {
        final Application application = Application.get();
        final GuiceInjectorHolder injectorHolder = application.getMetaData(GuiceInjectorHolder.INJECTOR_KEY);
        final Injector injector = injectorHolder.getInjector();
        return injector.getInstance(Key.get(beanType, qualifier));
    }
}
