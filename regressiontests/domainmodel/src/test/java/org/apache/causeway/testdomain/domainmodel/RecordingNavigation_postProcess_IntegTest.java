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
package org.apache.causeway.testdomain.domainmodel;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.testdomain.conf.Configuration_headless;
import org.apache.causeway.testdomain.domainmodel.recordingnav.Configuration_usingRecordingNavigation;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
                Configuration_usingRecordingNavigation.class,
        },
        properties = {
                "causeway.core.meta-model.introspector.mode=FULL",
                "causeway.core.meta-model.validator.explicit-object-type=FALSE",
                "causeway.extensions.command-log.recording-support=ENABLED",
                "causeway.extensions.command-log.navigation-action-synthesis=POST_PROCESS",
        })
@TestPropertySource({
    CausewayPresets.SilenceMetaModel,
    CausewayPresets.SilenceProgrammingModel,
})
class RecordingNavigation_postProcess_IntegTest extends RecordingNavigation_IntegTestAbstract {
}
