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

import org.apache.isis.applib.services.background.BackgroundCommandService;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.metamodel.services.command.CommandDtoServiceInternal;
import org.apache.isis.metamodel.specloader.SpecificationLoader;

import lombok.Builder;

@Builder
final class InvocationHandlerFactoryUsingBackgroundCommandService implements InvocationHandlerFactory {

    private final BackgroundCommandService backgroundCommandService;
    private final SpecificationLoader specificationLoader;
    private final CommandDtoServiceInternal commandDtoServiceInternal;
    private final CommandContext commandContext;
    private final ObjectAdapterProvider objectAdapterProvider;

    @Override
    public <T> InvocationHandler newMethodHandler(
    		T target, 
    		Object mixedInIfAny) {
        
        return new CommandInvocationHandler<T>(
                backgroundCommandService,
                target,
                mixedInIfAny,
                specificationLoader,
                commandDtoServiceInternal,
                commandContext,
                objectAdapterProvider);
    }

    @Override
    public void close() {
        backgroundCommandService.close();
    }



}
