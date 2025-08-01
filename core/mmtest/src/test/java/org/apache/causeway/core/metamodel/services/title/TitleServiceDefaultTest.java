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
package org.apache.causeway.core.metamodel.services.title;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.core.mmtestsupport.MetaModelContext_forTesting;

class TitleServiceDefaultTest {

    private TitleServiceDefault titleService;

    @BeforeEach
    void setUp() throws Exception {

        var mmc = MetaModelContext_forTesting.buildDefault();

        titleService = new TitleServiceDefault(null, mmc.getObjectManager());
    }

    // -- FEATURED

    static enum FeaturedEnum {
        FIRST,
        SECOND;

        public String title() {
            return name().toLowerCase();
        }

        public String iconName() {
            return name().toLowerCase();
        }

    }

    @Test
    void enum_shouldHonorTitleByMethod() {

        Object domainObject = FeaturedEnum.FIRST;

        var title = titleService.titleOf(domainObject);
        assertEquals("first", title);

    }

    // -- PLAIN

    static enum PlainEnum {
        FIRST,
        SECOND
    }

    @Test
    void enum_shouldFallbackTitleToEnumName() {

        Object domainObject = PlainEnum.FIRST;

        var title = titleService.titleOf(domainObject);
        assertEquals("First", title);

    }

}
