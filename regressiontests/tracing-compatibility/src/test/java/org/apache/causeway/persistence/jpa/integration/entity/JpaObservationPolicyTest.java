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
package org.apache.causeway.persistence.jpa.integration.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.query.Query;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.config.observation.CausewayObservationPolicy;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;

class JpaObservationPolicyTest {
    @Test void realFacetUsesStableNamesAndExcludesInstanceValues() {
        var stopped = new ArrayList<Observation.Context>();
        var facet = JpaObservationFixture.create(integration(stopped, CausewayObservationPolicy.DEFAULT), () -> {});
        for (String id : List.of("sentinel-id-one", "sentinel-id-two")) {
            facet.fetchByBookmark(Bookmark.forLogicalTypeNameAndIdentifier("fixture.Entity", id));
            facet.fetchByQuery(Query.named(JpaObservationFixture.Entity.class, "Entity.byName").withParameter("name", id));
        }
        facet.fetchByQuery(Query.allInstances(JpaObservationFixture.Entity.class));
        var entity = new JpaObservationFixture.Entity();
        facet.persist(entity);
        facet.refresh(entity);
        facet.delete(entity);
        facet.detach(entity);
        assertEquals(List.of("causeway.jpa.fetch-by-bookmark", "causeway.jpa.named-query",
                "causeway.jpa.fetch-by-bookmark", "causeway.jpa.named-query", "causeway.jpa.fetch-all",
                "causeway.jpa.persist", "causeway.jpa.refresh", "causeway.jpa.remove", "causeway.jpa.detach"),
                stopped.stream().map(Observation.Context::getName).toList());
        for (var context : stopped) {
            assertFalse((context.getName() + context.getContextualName() + context.getAllKeyValues()).contains("sentinel"));
            assertEquals(JpaObservationFixture.Entity.class.getName(), context.getLowCardinalityKeyValue("causeway.entity.type").getValue());
            assertNull(context.getLowCardinalityKeyValue("causeway.discard"));
        }
        assertEquals("Entity.byName", stopped.get(1).getLowCardinalityKeyValue("causeway.query.name").getValue());
    }
    @Test void facetUsesConfiguredThresholdAndRetainsErrors() {
        var stopped = new ArrayList<Observation.Context>();
        var policy = new CausewayObservationPolicy(java.time.Duration.ofDays(1));
        var integration = integration(stopped, policy);
        JpaObservationFixture.create(integration, () -> {}).persist(new JpaObservationFixture.Entity());
        assertNotNull(stopped.get(0).getLowCardinalityKeyValue("causeway.discard"));
        var failure = new IllegalStateException("work");
        var facet = JpaObservationFixture.create(integration, () -> { throw failure; });
        assertSame(failure, assertThrows(IllegalStateException.class, () -> facet.persist(new JpaObservationFixture.Entity())));
        assertSame(failure, stopped.get(1).getError());
        assertNull(stopped.get(1).getLowCardinalityKeyValue("causeway.discard"));
    }
    private CausewayObservationIntegration integration(List<Observation.Context> stopped, CausewayObservationPolicy policy) {
        var registry = ObservationRegistry.create();
        registry.observationConfig().observationHandler(new ObservationHandler<Observation.Context>() {
            public boolean supportsContext(Observation.Context context) { return true; }
            public void onStop(Observation.Context context) { stopped.add(context); }
        });
        return new CausewayObservationIntegration(registry, policy);
    }
}
