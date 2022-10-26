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
package org.apache.causeway.testing.integtestsupport.applib;

import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionContext;

import org.apache.causeway.applib.services.exceprecog.ExceptionRecognizerService;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.commons.internal.reflection._Annotations;
import org.apache.causeway.testing.integtestsupport.applib.annotation.InteractAs;
import org.apache.causeway.testing.integtestsupport.applib.annotation.InteractAsUtils;

class _Helper {

    static Optional<ServiceRegistry> getServiceRegistry(final ExtensionContext extensionContext) {
        return extensionContext.getTestInstance()
        .filter(CausewayIntegrationTestAbstract.class::isInstance)
        .map(CausewayIntegrationTestAbstract.class::cast)
        .map(CausewayIntegrationTestAbstract::getServiceRegistry);
    }

    /**
     * Eg. as declared on test method via {@link InteractAs}.
     */
    static Optional<InteractionContext> getCustomInteractionContext(final ExtensionContext extensionContext) {
        return extensionContext.getTestMethod()
        .flatMap(testMethod->_Annotations.synthesize(testMethod, InteractAs.class))
        .map(InteractAsUtils::toInteractionContext);
    }

    // -- SHORTCUTS

    static Optional<InteractionService> getInteractionFactory(final ExtensionContext extensionContext) {
        return getServiceRegistry(extensionContext)
        .flatMap(serviceRegistry->serviceRegistry.lookupService(InteractionService.class));
    }

    static Optional<ExceptionRecognizerService> getExceptionRecognizerService(
            final ExtensionContext extensionContext) {
        return getServiceRegistry(extensionContext)
        .flatMap(serviceRegistry->serviceRegistry.lookupService(ExceptionRecognizerService.class));
    }

}
