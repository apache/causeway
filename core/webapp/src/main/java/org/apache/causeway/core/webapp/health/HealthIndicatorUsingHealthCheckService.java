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
package org.apache.causeway.core.webapp.health;

import lombok.Builder;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import org.apache.causeway.applib.services.health.HealthCheckService;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.core.security.authentication.InteractionContextFactory;

@Component
@Named("causeway.webapp.HealthCheckService") // logical name appears in the endpoint
public class HealthIndicatorUsingHealthCheckService extends AbstractHealthIndicator {

    private final InteractionService interactionService;
    private final List<HealthCheckService> healthCheckServices;

    @Builder
    @Inject
    public HealthIndicatorUsingHealthCheckService(
            final InteractionService interactionService,
            final List<HealthCheckService> healthCheckServices) {
        this.interactionService = interactionService;
        this.healthCheckServices = healthCheckServices;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        for (HealthCheckService healthCheckService : healthCheckServices) {
            org.apache.causeway.applib.services.health.Health health = interactionService.call(InteractionContextFactory.health(), healthCheckService::check);
            if (health != null) {
                var success = health.getResult();
                if(! success) {
                    Optional.ofNullable(health.getCause())
                            .ifPresentOrElse(ex -> builder.down(ex), () -> builder.down());
                    return;
                }
            }
        }
        builder.up();
    }
}
