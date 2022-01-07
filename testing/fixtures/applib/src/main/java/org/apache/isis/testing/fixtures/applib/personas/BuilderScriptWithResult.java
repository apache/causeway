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
package org.apache.isis.testing.fixtures.applib.personas;

import org.apache.isis.applib.annotations.Programmatic;

/**
 * A specialization of {@link BuilderScriptAbstract} which expects there to be
 * a top-level object, and so - by defining its own {@link #buildResult(ExecutionContext) hook method} - removes a little of the boilerplate..
 *
 * @since 2.x {@index}
 *
 * @param <T>
 */
public abstract class BuilderScriptWithResult<T> extends BuilderScriptAbstract<T> {

    public T object;

    /**
     * Simply returns the object returned by {@link #buildResult(ExecutionContext)}.
     */
    @Override
    public final T getObject() {
        return object;
    }

    /**
     * Concrete implementation that simply executes {@link #buildResult(ExecutionContext)} and stores the
     * result to be accessed by {@link #getObject()}.
     */
    @Override
    protected final void execute(final ExecutionContext executionContext) {
        object = buildResult(executionContext);
    }

    /**
     * Hook method to return a single object.
     */
    @Programmatic
    protected abstract T buildResult(final ExecutionContext ec);

}
