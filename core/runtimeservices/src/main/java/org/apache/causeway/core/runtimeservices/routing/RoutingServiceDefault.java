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
package org.apache.causeway.core.runtimeservices.routing;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.homepage.HomePageResolverService;
import org.apache.causeway.applib.services.routing.RoutingService;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;

import lombok.RequiredArgsConstructor;

/**
 * Default implementation of {@link RoutingService}, which will route any <code>void</code>action or action
 * returning <code>null</code> to the home page (as per {@link HomePageResolverService}.
 *
 * @since 2.0 {@index}
 *
 * @see HomePageResolverService
 */
@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".RoutingServiceDefault")
@Priority(PriorityPrecedence.EARLY)
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
//@Slf4j
public class RoutingServiceDefault implements RoutingService {

    private final HomePageResolverService homePageResolverService;

    @Override
    public boolean canRoute(final Object original) {
        return original == null;
    }

    @Override
    public Object route(final Object original) {
        return homePageResolverService.getHomePage();
    }

}
