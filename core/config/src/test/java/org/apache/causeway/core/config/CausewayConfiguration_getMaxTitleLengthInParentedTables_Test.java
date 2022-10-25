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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CausewayConfiguration_getMaxTitleLengthInParentedTables_Test {

    private CausewayConfiguration configuration;

    @BeforeEach
    void setUp() throws Exception {
        configuration = new CausewayConfiguration(null);
    }

    @Test
    void when_not_set() throws Exception {
        // when
        int val = configuration.getViewer().getWicket().getMaxTitleLengthInParentedTables();

        // then
        Assertions.assertThat(val).isEqualTo(12);
    }

    @Test
    void when_not_set_explicitly_but_fallback_has_been() throws Exception {
        // given
        configuration.getViewer().getWicket().setMaxTitleLengthInTables(20);

        // when
        int val = configuration.getViewer().getWicket().getMaxTitleLengthInParentedTables();

        // then
        Assertions.assertThat(val).isEqualTo(20);
    }

    @Test
    void when_set_explicitly() throws Exception {
        // given
        configuration.getViewer().getWicket().setMaxTitleLengthInParentedTables(25);

        // when
        int val = configuration.getViewer().getWicket().getMaxTitleLengthInParentedTables();

        // then
        Assertions.assertThat(val).isEqualTo(25);
    }

    @Test
    void when_set_explicitly_ignores_fallback_has_been() throws Exception {
        // given
        configuration.getViewer().getWicket().setMaxTitleLengthInTables(20);
        configuration.getViewer().getWicket().setMaxTitleLengthInParentedTables(25);

        // when
        int val = configuration.getViewer().getWicket().getMaxTitleLengthInParentedTables();

        // then
        Assertions.assertThat(val).isEqualTo(25);
    }
}