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
package demoapp.dom._infra.fixtures;

import javax.inject.Inject;

import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScript;

import demoapp.dom._infra.seed.SeedService;

public class DemoFixtureScript extends FixtureScript {

    @Override
    protected void execute(final ExecutionContext executionContext) {
        serviceRegistry
                .select(SeedService.class)  // lookup rather than injection to avoid circular reference.
                .forEach(seedService -> seedService.seed(this, executionContext));
    }

    @Inject
    ServiceRegistry serviceRegistry;
}
