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

import org.apache.causeway.applib.services.health.HealthCheckService;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.boot.actuate.health.Health;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthIndicatorUsingHealthCheckServiceTest {

    @Mock private InteractionService mockInteractionService;
    @Mock private HealthCheckService mockHealthCheckService1;
    @Mock private HealthCheckService mockHealthCheckService2;

    private org.apache.causeway.applib.services.health.Health causewayHealth;

    @InjectMocks
    private HealthIndicatorUsingHealthCheckService healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = HealthIndicatorUsingHealthCheckService.builder()
                            .interactionService(mockInteractionService)
                            .healthCheckServices(List.of(mockHealthCheckService1, mockHealthCheckService2))
                        .build();
    }

    @Test
    void shouldReturnUpWhenAllHealthChecksPass() {
        // Given
        causewayHealth = org.apache.causeway.applib.services.health.Health.ok();
        when(mockInteractionService.call(any(), any())).thenReturn(causewayHealth);

        Health.Builder builder = new Health.Builder();

        // When
        healthIndicator.doHealthCheck(builder);

        // Then
        Health health = builder.build();
        assertEquals(Health.up().build(), health);
        verify(mockInteractionService, times(2)).call(any(), any());
    }

    @Test
    void shouldReturnDownWhenAnyHealthCheckFailsWithoutException() {
        // Given
        causewayHealth = org.apache.causeway.applib.services.health.Health.error("no cause");
        when(mockInteractionService.call(any(), any())).thenReturn(causewayHealth);

        Health.Builder builder = new Health.Builder();

        // When
        healthIndicator.doHealthCheck(builder);

        // Then
        Health health = builder.build();
        assertEquals(Health.down().build(), health);
        verify(mockInteractionService, times(1)).call(any(), any());
    }

    @Test
    void shouldReturnDownWhenAnyHealthCheckFailsWithException() {
        // Given
        Exception exception = new RuntimeException("Database connection failed");
        causewayHealth = org.apache.causeway.applib.services.health.Health.error(exception);
        when(mockInteractionService.call(any(), any())).thenReturn(causewayHealth);

        Health.Builder builder = new Health.Builder();

        // When
        healthIndicator.doHealthCheck(builder);

        // Then
        Health health = builder.build();
        assertEquals(Health.down(exception).build(), health);
        verify(mockInteractionService, times(1)).call(any(), any());
    }

    @Test
    void shouldReturnUpWhenHealthCheckServiceIsEmpty() {
        // Given
        healthIndicator = new HealthIndicatorUsingHealthCheckService(mockInteractionService, List.of());
        Health.Builder builder = new Health.Builder();

        // When
        healthIndicator.doHealthCheck(builder);

        // Then
        Health health = builder.build();
        assertEquals(Health.up().build(), health);
        verifyNoInteractions(mockInteractionService);
    }
}
