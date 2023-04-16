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
package demoapp.dom._infra.resources;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.mock.env.MockEnvironment;

import org.apache.causeway.core.config.CausewayConfiguration;

import lombok.val;

class ResourceReaderService_Test {

    ResourceReaderService resourceReaderService;

    @BeforeEach
    void setUp() {
        resourceReaderService = new ResourceReaderService();
        resourceReaderService.markupVariableResolverService =
                new MarkupVariableResolverService(new CausewayConfiguration(null), new MockEnvironment().withProperty("spring.profiles.active", "demo-jpa"));
    }

    @Test
    void read_with_tags() {

        // given
        val attributes = new HashMap<String, Object>();
        attributes.put("tags", "class");

        // when
        String actual = resourceReaderService.readResource(getClass(), "ResourceReaderService_Test-Test1.java", attributes);

        // then
        String expected = resourceReaderService.readResource(getClass(), "ResourceReaderService_Test-Test1-expected.java");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void read_missing_tags() {

        // given
        val attributes = new HashMap<String, Object>();
        attributes.put("tags", "other");

        // when
        String actual = resourceReaderService.readResource(getClass(), "ResourceReaderService_Test-Test1.java", attributes);

        // then
        String expected = "";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void read_subdir_with_tags() {

        // given
        val attributes = new HashMap<String, Object>();
        attributes.put("tags", "class");

        // when
        String actual = resourceReaderService.readResource(getClass(), "subdir/ResourceReaderService_Test-Test1.java", attributes);

        // then
        String expected = resourceReaderService.readResource(getClass(), "subdir/ResourceReaderService_Test-Test1-expected.java");
        assertThat(actual).isEqualTo(expected);
    }

}
