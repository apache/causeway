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
package org.apache.causeway.extensions.commandlog.jpa.integtests;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import org.apache.causeway.extensions.commandlog.applib.integtest.BackgroundService_IntegTestAbstract;
import org.apache.causeway.extensions.commandlog.jpa.integtests.model.Counter;

@SpringBootTest(
        classes = AppManifest.class
)
@ActiveProfiles("test")
public class BackgroundService_IntegTest extends BackgroundService_IntegTestAbstract {

    @Override
    protected org.apache.causeway.extensions.commandlog.applib.integtest.model.Counter newCounter(final String name) {
        return Counter.builder().name(name).build();
    }

}
