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
package org.apache.causeway.viewer.graphql.viewer.test.schema;

import org.apache.causeway.viewer.graphql.viewer.test.CausewayViewerGraphqlTestModuleIntegTestAbstract;

import org.approvaltests.Approvals;
import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.UseReporter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.condition.DisabledIfSystemProperties;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import org.springframework.test.context.ActiveProfiles;

import static org.apache.causeway.commons.internal.assertions._Assert.assertEquals;
import static org.apache.causeway.commons.internal.assertions._Assert.assertNotNull;
import static org.apache.causeway.commons.internal.assertions._Assert.assertTrue;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@ActiveProfiles("test")
public class Schema_IntegTest extends CausewayViewerGraphqlTestModuleIntegTestAbstract {

    @Test
    @Disabled // to avoid having to keep re-verify
    @UseReporter(DiffReporter.class)
    void schema() throws Exception {
        Approvals.verify(submit(), jsonOptions());
    }

    @Test
    @Disabled // to avoid having to keep re-verify
    @UseReporter(DiffReporter.class)
    void schema_types_name() throws Exception {
        Approvals.verify(submit(), jsonOptions());
    }
}