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
package org.apache.causeway.core.config.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.commons.collections.Can;

class CausewayBeanTypeRegistryMixinOrderTest {
    @Test void allInsertionPermutationsProduceClassNameOrderWithoutChangingMembership() {
        var input = List.<Class<?>>of(Zulu.class, Alpha.class, Beta.class);
        var expected = input.stream().map(Class::getName).sorted().toList();
        for (int seed = 0; seed < 30; seed++) {
            var shuffled = new ArrayList<>(input);
            Collections.shuffle(shuffled, new Random(seed));
            var metadata = new ArrayList<CausewayBeanMetaData>();
            for (var type : shuffled) metadata.add(CausewayBeanMetaData.mixin(
                    LogicalType.fqcn(type), CausewayBeanMetaData.DiscoveredBy.CAUSEWAY_UPFRONT));
            metadata.add(CausewayBeanMetaData.programmatic(LogicalType.fqcn(Other.class)));
            var registry = new CausewayBeanTypeRegistry(Can.ofCollection(metadata));
            assertEquals(expected, registry.streamMixinTypes().map(Class::getName).toList());
            assertEquals(expected, registry.streamMixinTypes().map(Class::getName).toList());
            assertEquals(4, registry.streamScannedTypes().count());
        }
    }

    @Test void emptyRegistryIsStillEmpty() {
        assertEquals(0, CausewayBeanTypeRegistry.empty().streamMixinTypes().count());
    }
    private static final class Zulu { }
    private static final class Alpha { }
    private static final class Beta { }
    private static final class Other { }
}
