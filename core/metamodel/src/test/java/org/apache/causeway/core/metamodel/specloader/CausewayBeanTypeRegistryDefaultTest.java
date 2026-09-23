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
package org.apache.causeway.core.metamodel.specloader;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.beans.CausewayBeanMetaData;
import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistryDefault;

class CausewayBeanTypeRegistryDefaultTest {

    static class AlphaMixin {}
    static class MiddleMixin {}
    static class ZuluMixin {}

    @Test
    void streamsMixinTypesByFullyQualifiedClassNameRegardlessOfRegistrationOrder() {
        final List<Class<?>> expected = List.of(AlphaMixin.class, MiddleMixin.class, ZuluMixin.class);

        assertEquals(expected, registry(ZuluMixin.class, AlphaMixin.class, MiddleMixin.class)
                .streamMixinTypes()
                .collect(Collectors.toList()));
        assertEquals(expected, registry(MiddleMixin.class, ZuluMixin.class, AlphaMixin.class)
                .streamMixinTypes()
                .collect(Collectors.toList()));
    }

    private CausewayBeanTypeRegistryDefault registry(final Class<?>... mixinTypes) {
        return new CausewayBeanTypeRegistryDefault(Can.ofArray(mixinTypes)
                .map(type -> CausewayBeanMetaData.notManaged(BeanSort.MIXIN, type)));
    }
}
