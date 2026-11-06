/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.testdomain.domainmodel;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistry;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.testdomain.conf.Configuration_headless;
import org.apache.causeway.testdomain.domainmodel.introspectiondiagnostics.Configuration_usingDiagnosticNavigation;
import org.apache.causeway.testdomain.domainmodel.introspectiondiagnostics.DiagnosticCycleA;
import org.apache.causeway.testdomain.domainmodel.introspectiondiagnostics.DiagnosticCycleB;
import org.apache.causeway.testdomain.domainmodel.introspectiondiagnostics.DiagnosticCycleB_alpha;
import org.apache.causeway.testdomain.domainmodel.introspectiondiagnostics.DiagnosticCycleB_zulu;

/** Repeated real bootstrap, without assuming v4 reproduces maintenance's original overflow. */
class RecordingIntrospectionDiagnosticsTest {
    private static final String COLLECTION_NAVIGATION_PREFIX = "__causeway_navigate_to_one_of_";

    @Test void repeatedCyclicMixinBootstrapRetainsNavigationElementTypesAndExplicitOrder() {
        List<String> firstOrder = null;
        for (int run = 0; run < 2; run++) {
            long started = System.nanoTime();
            try (var context = new SpringApplicationBuilder(Configuration_headless.class, Configuration_usingDiagnosticNavigation.class)
                    .web(WebApplicationType.NONE).logStartupInfo(false)
                    .properties("spring.main.banner-mode=off", "causeway.core.meta-model.introspector.mode=FULL",
                            "causeway.core.meta-model.introspector.parallelize=false",
                            "causeway.core.meta-model.validator.explicit-object-type=FALSE",
                            "causeway.extensions.command-log.recording-support=ENABLED",
                            "logging.level.org.apache.causeway.core.metamodel=ERROR",
                            "logging.level.org.apache.causeway.core.metamodel.progmodel=ERROR")
                    .run()) {
                var loader = context.getBean(SpecificationLoader.class);
                var registry = context.getBean(CausewayBeanTypeRegistry.class);
                var a = loader.specForTypeElseFail(DiagnosticCycleA.class);
                var b = loader.specForTypeElseFail(DiagnosticCycleB.class);
                assertTrue(a.getAction(COLLECTION_NAVIGATION_PREFIX + "bs", MixedIn.INCLUDED).isPresent());
                assertTrue(b.getAction(COLLECTION_NAVIGATION_PREFIX + "as", MixedIn.INCLUDED).isPresent());
                assertEquals(DiagnosticCycleA.class, b.getAction("alpha", MixedIn.INCLUDED).orElseThrow().getElementType().correspondingClass());
                var actionIds = b.streamActions(ActionScope.ANY, MixedIn.INCLUDED).map(action -> action.getId()).toList();
                assertTrue(actionIds.containsAll(List.of("alpha", "zulu")));
                assertTrue(actionIds.indexOf("zulu") < actionIds.indexOf("alpha"), "Explicit sequence must override class-name traversal");
                var order = registry.streamMixinTypes().map(Class::getName).toList();
                assertEquals(order.stream().sorted().toList(), order);
                assertTrue(order.contains(DiagnosticCycleB_alpha.class.getName()));
                assertTrue(order.contains(DiagnosticCycleB_zulu.class.getName()));
                if (firstOrder == null) firstOrder = order;
                else assertEquals(firstOrder, order);
                System.out.println("DIAGNOSTIC_GRAPH_BOOT run=" + run + " elapsed_ms=" + (System.nanoTime() - started) / 1_000_000);
            }
        }
    }
}
