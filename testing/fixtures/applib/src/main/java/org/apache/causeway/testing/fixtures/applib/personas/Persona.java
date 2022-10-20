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
package org.apache.causeway.testing.fixtures.applib.personas;

import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScript;

/**
 * Unifies {@link PersonaWithFinder} and {@link PersonaWithBuilderScript}, so that an implementation (usually
 * an enum) can both be {@link #build(FixtureScript, FixtureScript.ExecutionContext)} built) (in the context of an
 * existing {@link FixtureScript}) and {@link PersonaWithFinder#findUsing(ServiceRegistry) found}.
 *
 * @since 2.x {@index}
 */
public interface Persona<T, B extends BuilderScriptAbstract<T>>
    extends PersonaWithFinder<T>, PersonaWithBuilderScript<T, B> {

    default T build(final FixtureScript parentFixtureScript, FixtureScript.ExecutionContext executionContext) {
        return builder().build(parentFixtureScript, executionContext).getObject();
    }

}

