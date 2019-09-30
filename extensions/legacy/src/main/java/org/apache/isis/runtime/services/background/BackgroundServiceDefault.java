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
package org.apache.isis.runtime.services.background;

import java.lang.reflect.InvocationHandler;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.apache.isis.applib.services.background.BackgroundCommandService;
import org.apache.isis.applib.services.background.BackgroundService;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.commons.internal._Constants;
import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.commons.internal.plugins.codegen.ProxyFactory;
import org.apache.isis.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.metamodel.services.command.CommandDtoServiceInternal;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.metamodel.specloader.classsubstitutor.ProxyEnhanced;

import static org.apache.isis.commons.internal.base._Casts.uncheckedCast;

import lombok.val;

/**
 * For command-reification depends on an implementation of
 * {@link org.apache.isis.applib.services.background.BackgroundCommandService} to
 * be configured.
 * @deprecated compare with v1.x and check whether we can cleanup the API
 */
//@Service
class BackgroundServiceDefault implements BackgroundService {

    private InvocationHandlerFactory invocationHandlerFactory;  


    @PostConstruct
    public void init() {
        val backgroundCommandService = 
                serviceRegistry.select(BackgroundCommandService.class)
                .getFirst()
                .orElse(null);

        this.invocationHandlerFactory = (backgroundCommandService!=null)
                ? InvocationHandlerFactoryUsingBackgroundCommandService.builder()
                        .backgroundCommandService(backgroundCommandService)
                        .specificationLoader(specificationLoader)
                        .commandDtoServiceInternal(commandDtoServiceInternal)
                        .commandContext(commandContext)
                        .objectAdapterProvider(objectAdapterProvider)
                        .build()
                        : new InvocationHandlerFactoryFallback(transactionService);

    }


    @PreDestroy
    public void shutdown() {
        if(invocationHandlerFactory!=null) {
            invocationHandlerFactory.close();
        }
    }

    // -- EXECUTION

    @Override
    public <T> T execute(final T domainObject) {
        final Class<T> cls = uncheckedCast(domainObject.getClass());
        val mixedIn = null;
        val methodHandler = invocationHandlerFactory.newMethodHandler(domainObject, mixedIn);
        return newProxy(cls, mixedIn, methodHandler);
    }

    @Override
    public <T> T executeMixin(Class<T> mixinClass, Object mixedIn) {
        val mixin = factoryService.<T>mixin(mixinClass, mixedIn);
        val methodHandler = invocationHandlerFactory.newMethodHandler(mixin, mixedIn);
        return newProxy(mixinClass, mixedIn, methodHandler);
    }

    // -- HELPER

    protected ObjectSpecification getSpecification(final Class<?> type) {
        return specificationLoader.loadSpecification(type);
    }

    private <T> T newProxy(
            final Class<T> cls,
            final Object mixedInIfAny, 
            final InvocationHandler methodHandler) {

        val interfaces = _Arrays.combine(
                cls.getInterfaces(),
                new Class<?>[] { ProxyEnhanced.class });

        val initialize = mixedInIfAny!=null;


        val constructorArgTypes = initialize 
                ? new Class<?>[] {mixedInIfAny.getClass()} 
                    : _Constants.emptyClasses;

        val constructorArgs = initialize 
                ? new Object[] {mixedInIfAny} 
                    : _Constants.emptyObjects;

        val proxyFactory = ProxyFactory.<T>builder(cls)
                .interfaces(interfaces)
                .constructorArgTypes(constructorArgTypes)
                .build();

        return initialize
                ? proxyFactory.createInstance(methodHandler, constructorArgs)
                        : proxyFactory.createInstance(methodHandler, /*initialize*/false);
    }

    // -- DEPENDENCIES

    @Inject private ServiceRegistry serviceRegistry;
    @Inject private CommandDtoServiceInternal commandDtoServiceInternal;
    @Inject private CommandContext commandContext;
    @Inject private FactoryService factoryService;
    @Inject private SpecificationLoader specificationLoader;
    @Inject private ObjectAdapterProvider objectAdapterProvider;
    @Inject private TransactionService transactionService;


}
