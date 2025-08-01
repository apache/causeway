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
package org.apache.causeway.core.metamodel.facets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.commons.internal.reflection._ClassCache;
import org.apache.causeway.core.mmtestsupport._TestDummies;

class MethodFinderUtilsTest {

    public static class NoPostConstruct {
        public void thisDoesNotHaveAnyAnnotation(){}
    }

    @BeforeEach
    public void setup() {
    }

    @Test
    public void whenExists() throws Exception {
        var methodIfAny = _ClassCache.getInstance().streamPostConstructMethods(_TestDummies.WithPostConstruct.class)
                .findFirst();
        assertTrue(methodIfAny.isPresent());
    }

    @Test
    public void whenDoesNotExist() throws Exception {
        var methodIfAny = _ClassCache.getInstance().streamPostConstructMethods(NoPostConstruct.class)
                .findFirst();
        assertFalse(methodIfAny.isPresent());
    }

}