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

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.util.TestPropertyValues;
import org.apache.causeway.core.config.CausewayConfiguration.Viewer.Wicket.Observation.Detail;
import static org.assertj.core.api.Assertions.assertThat;

class CausewayConfiguration_WicketObservation_Test {
    @Test void defaults() {
        new ConfigurationFactory().test(TestPropertyValues.empty(), config -> {
            assertThat(config.viewer().wicket().observation().detail()).isEqualTo(Detail.MEMBERS);
            assertThat(config.viewer().wicket().observation().maxSpansPerRequest()).isZero();
        });
    }
    @Test void allLevelsBindWithBudget() {
        for(var detail : Detail.values()) {
            new ConfigurationFactory().test(TestPropertyValues.of(
                    "causeway.viewer.wicket.observation.detail=" + detail,
                    "causeway.viewer.wicket.observation.max-spans-per-request=37"), config -> {
                assertThat(config.viewer().wicket().observation().detail()).isEqualTo(detail);
                assertThat(config.viewer().wicket().observation().maxSpansPerRequest()).isEqualTo(37);
            });
        }
    }
    @Test void invalidSettingsFailStartup() {
        for(var property : new String[] { "detail=unsupported", "max-spans-per-request=-1" }) {
            new ApplicationContextRunner().withUserConfiguration(CausewayModuleCoreConfig.class)
                    .withPropertyValues("causeway.viewer.wicket.observation." + property)
                    .run(context -> assertThat(context).hasFailed());
        }
    }
}
