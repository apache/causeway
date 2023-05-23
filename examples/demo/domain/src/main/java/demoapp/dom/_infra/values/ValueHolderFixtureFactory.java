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
package demoapp.dom._infra.values;

import java.util.function.Supplier;

import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScript;

import lombok.NonNull;

public class ValueHolderFixtureFactory {

    public static <T, E extends ValueHolder<T>> Supplier<FixtureScript>
        fixtureScriptSupplierFor(final @NonNull ValueHolderRepository<T, E> entities) {
        return ()->fixtureScriptFor(entities);
    }

    public static <T, E extends ValueHolder<T>> FixtureScript
        fixtureScriptFor(final @NonNull ValueHolderRepository<T, E> entities) {

        return new FixtureScript() {

            @Override
            protected void execute(ExecutionContext executionContext) {
                entities.seedSamples(domainObject->
                    executionContext.addResult(this, domainObject));
            }

        };
    }

}