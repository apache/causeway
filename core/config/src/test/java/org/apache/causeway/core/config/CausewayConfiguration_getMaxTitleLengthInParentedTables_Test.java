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
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.util.TestPropertyValues;

//--add-exports org.hibernate.validator/org.hibernate.validator.internal.engine=spring.core
//--add-exports org.hibernate.validator/org.hibernate.validator.internal.constraintvalidators.bv=org.apache.causeway.core.config
class CausewayConfiguration_getMaxTitleLengthInParentedTables_Test {

    private ConfigurationFactory configurationFactory = new ConfigurationFactory();

    @Test
    void when_not_set() throws Exception {
        configurationFactory.test(
            TestPropertyValues.empty(),
            causeway->{
                // when
                int val = causeway.viewer().wicket().maxTitleLengthInParentedTables();
                // then
                Assertions.assertThat(val).isEqualTo(12);
            });
    }

    @Test
    void when_not_set_explicitly_but_fallback_has_been() {
        configurationFactory.test(
            TestPropertyValues.of(
                // given
                "causeway.viewer.wicket.maxTitleLengthInTables=20"),
            causeway->{
                // when
                int val = causeway.viewer().wicket().maxTitleLengthInParentedTables();
                // then
                Assertions.assertThat(val).isEqualTo(20);
            });
    }

    @Test
    void when_set_explicitly() {
        configurationFactory.test(
            TestPropertyValues.of(
                // given
                "causeway.viewer.wicket.maxTitleLengthInParentedTables=25"),
            causeway->{
                // when
                int val = causeway.viewer().wicket().maxTitleLengthInParentedTables();
                // then
                Assertions.assertThat(val).isEqualTo(25);
            });
    }

    @Test
    void when_set_explicitly_ignores_fallback_has_been() {
        configurationFactory.test(
            TestPropertyValues.of(
                // given
                "causeway.viewer.wicket.maxTitleLengthInTables=20",
                "causeway.viewer.wicket.maxTitleLengthInParentedTables=25"),
            causeway->{
                // when
                int val = causeway.viewer().wicket().maxTitleLengthInParentedTables();
                // then
                Assertions.assertThat(val).isEqualTo(25);
            });

    }
}