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
package org.apache.causeway.core.metamodel.services.registry;

import java.lang.annotation.Annotation;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.ioc.SpringContextHolder;
import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistry;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;

class ServiceRegistryDefaultTest {

    @Test
    void selectReturnsEmptyWhenSpringContextHolderIsUnavailable() {
        var serviceRegistry = new ServiceRegistryDefault(
                new CausewaySystemEnvironment(),
                Mockito.mock(CausewayBeanTypeRegistry.class));

        assertTrue(serviceRegistry.select(String.class, new Annotation[0]).isEmpty());
    }

    @Test
    void selectDelegatesTypeAndQualifiersWhenSpringContextHolderIsAvailable() {
        var systemEnvironment = Mockito.mock(CausewaySystemEnvironment.class);
        var springContextHolder = Mockito.mock(SpringContextHolder.class);
        var qualifiers = new Annotation[] { Mockito.mock(Annotation.class) };
        var selectedServices = Can.of("selected");

        Mockito.when(systemEnvironment.springContextHolder()).thenReturn(springContextHolder);
        Mockito.when(springContextHolder.select(String.class, qualifiers)).thenReturn(selectedServices);

        var serviceRegistry = new ServiceRegistryDefault(
                systemEnvironment,
                Mockito.mock(CausewayBeanTypeRegistry.class));

        assertSame(selectedServices, serviceRegistry.select(String.class, qualifiers));
        Mockito.verify(springContextHolder).select(String.class, qualifiers);
    }

}
