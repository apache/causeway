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
package org.apache.isis.extensions.fixtures.legacy.fixturescripts;

import javax.inject.Singleton;


/**
 * Acts as a factory by the {@link org.apache.isis.extensions.fixtures.legacy.fixturescripts.FixtureScripts} when
 * instantiating the {@link org.apache.isis.extensions.fixtures.legacy.fixturescripts.FixtureScript.ExecutionContext}.
 *
 * <p>
 *     Factoring this out as a service potentially allows for extensions to parsing; and also acts as an
 *     insurance policy to allow this part of the testing framework to be patched if the chosen parsing algorithms
 *     need refinement in the future).
 * </p>
 */
@Singleton
public class ExecutionParametersService {

    public ExecutionParameters newExecutionParameters(final String parameters) {
        return new ExecutionParameters(parameters);
    }

}
