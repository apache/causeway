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
package org.apache.isis.core.webapp.health;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.services.health.HealthCheckService;

import lombok.val;

@Component
@Named("isisWebapp.HealthCheckService") // this appears in the endpoint.
public class HealthIndicatorUsingHealthCheckService extends AbstractHealthIndicator {

    private final Optional<HealthCheckService> healthCheckServiceIfAny;

    @Inject
    public HealthIndicatorUsingHealthCheckService(final Optional<HealthCheckService> healthCheckServiceIfAny) {
        this.healthCheckServiceIfAny = healthCheckServiceIfAny;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        val health = healthCheckServiceIfAny.map(HealthCheckService::check)
                     .orElse(null);
        if(health != null) {
            final boolean result = health.getResult();
            if(result) {
                builder.up();
            } else {
                final Throwable cause = health.getCause();
                if(cause != null) {
                    builder.down(cause);
                } else {
                    builder.down();
                }
            }
        } else {
            builder.unknown();
        }
    }
}
