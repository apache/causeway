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
package org.apache.causeway.core.config;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.causeway.core.config.metamodel.facets.DomainObjectConfigOptions.LockingPolicy;

class CausewayConfiguration_locking_Test {
    @Test
    void defaultsToOptimistic() {
        assertThat(new CausewayConfiguration(new StandardEnvironment(), Optional.empty())
                .getApplib().getAnnotation().getDomainObject().getLocking()).isEqualTo(LockingPolicy.OPTIMISTIC);
    }

    @Test
    void bindsPessimistic() {
        assertThat(bind("pessimistic").getApplib().getAnnotation().getDomainObject().getLocking())
                .isEqualTo(LockingPolicy.PESSIMISTIC);
    }

    @Test
    void rejectsUnresolvedValues() {
        assertThrows(BindException.class, () -> bind("DEFAULT"));
        assertThrows(BindException.class, () -> bind("AS_CONFIGURED"));
    }

    private CausewayConfiguration bind(final String value) {
        final StandardEnvironment env = new StandardEnvironment();
        env.getPropertySources().addFirst(new MapPropertySource("test",
                Map.of("causeway.applib.annotation.domain-object.locking", value)));
        final CausewayConfiguration config = new CausewayConfiguration(env, Optional.empty());
        Binder.get(env).bind(CausewayConfiguration.ROOT_PREFIX, Bindable.ofInstance(config));
        return config;
    }
}
