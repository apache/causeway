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
package org.apache.isis.testing.fixtures.applib.api;

import org.apache.isis.testing.fixtures.applib.fixturescripts.BuilderScriptAbstract;

/**
 * Intended for persona enums to implement, to provide an instance of a {@link BuilderScriptAbstract} in order to
 * instantiate an instance of the persona (normally in the form of a domain entity or set of related domain entities).
 *
 * <p>
 *     ({@link BuilderScriptAbstract} is a specialization of
 *     {@link org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript}).
 * </p>
 *
 * @see PersonaWithFinder
 * @since 2.x {@index}
 */
public interface PersonaWithBuilderScript<T extends BuilderScriptAbstract<?>>  {

    /**
     * Returns a {@link BuilderScriptAbstract} to use to instantiate this persona.
     */
    T builder();

}

