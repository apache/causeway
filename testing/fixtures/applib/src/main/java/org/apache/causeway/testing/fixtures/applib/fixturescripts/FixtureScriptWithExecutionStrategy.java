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
package org.apache.causeway.testing.fixtures.applib.fixturescripts;

import org.apache.causeway.applib.annotation.Programmatic;

/**
 * Interface for {@link org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScript}s to optionally implement,
 * used to override the {@link FixtureScriptsSpecification#getMultipleExecutionStrategy() globally-defined}
 * {@link org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScripts.MultipleExecutionStrategy}
 * strategy.
 *
 * <p>
 *     This therefore allows individual fixture scripts to indicate that they have their own execution strategy.
 * </p>
 *
 * @since 2.x {@index}
 */
public interface FixtureScriptWithExecutionStrategy {

    @Programmatic
    FixtureScripts.MultipleExecutionStrategy getMultipleExecutionStrategy();
}
