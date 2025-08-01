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

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.context.MetaModelContextFactory;

/**
 * @since 2.0 {@index}
 */
public class CausewayInteractionHandler implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(final ExtensionContext extensionContext) throws Exception {

        //[CAUSEWAY-3647] set MMC singleton reference explicitly on each test run
        _Helper
            .getSpringContext(extensionContext)
            .ifPresent(springContext->{
                var mmc = springContext.getBean(MetaModelContext.class);
                _Assert.assertNotNull(mmc, ()->
                        "MetaModelContext not found on Spring's test context.");
                MetaModelContextFactory.setTestContext(mmc);
            });

        _Helper
            .getInteractionFactory(extensionContext)
            .ifPresent(interactionService->
                _Helper
                    .getCustomInteractionContext(extensionContext)
                    .ifPresentOrElse(
                        interactionService::openInteraction,
                        interactionService::openInteraction));
    }

    @Override
    public void afterEach(final ExtensionContext extensionContext) throws Exception {
        _Helper
            .getInteractionFactory(extensionContext)
            .ifPresent(InteractionService::closeInteractionLayers);
    }

}
