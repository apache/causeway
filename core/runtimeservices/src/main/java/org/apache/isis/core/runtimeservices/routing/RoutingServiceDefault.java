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
package org.apache.isis.core.runtimeservices.routing;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.homepage.HomePageResolverService;
import org.apache.isis.applib.services.routing.RoutingService;

@Service
@Named("isis.runtimeservices.RoutingServiceDefault")
@Priority(PriorityPrecedence.EARLY)
@Qualifier("Default")
//@Log4j2
public class RoutingServiceDefault implements RoutingService {

    private final HomePageResolverService homePageResolverService;

    @Inject
    public RoutingServiceDefault(final HomePageResolverService homePageResolverService) {
        this.homePageResolverService = homePageResolverService;
    }

    @Override
    public boolean canRoute(final Object original) {
        return true;
    }

    @Override
    public Object route(final Object original) {
        return original != null ? original : homePageResolverService.getHomePage();
    }

}
