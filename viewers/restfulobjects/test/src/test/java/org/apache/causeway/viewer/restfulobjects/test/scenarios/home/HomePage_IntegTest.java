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
package org.apache.causeway.viewer.restfulobjects.test.scenarios.home;

import org.approvaltests.Approvals;
import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.UseReporter;
import org.junit.jupiter.api.Test;

import org.apache.causeway.viewer.restfulobjects.test.scenarios.Abstract_IntegTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class HomePage_IntegTest extends Abstract_IntegTest {

    @Test
    @UseReporter(DiffReporter.class)
    void homePage() {

        // given
        var response = restGetJson("/", log);

        // when
        var entity = response
            .body(String.class);

        // then
        Approvals.verify(entity, jsonOptions());
    }

}
