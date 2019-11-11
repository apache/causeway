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
package org.apache.isis.extensions.fixtures.fixturescripts;

/**
 * A convenience subclass of {@link org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript} that is
 * {@link org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript.Discoverability#DISCOVERABLE discoverable}
 * by default.
 */
public abstract class DiscoverableFixtureScript extends FixtureScript {

    /**
     * Initializes a {@link Discoverability#DISCOVERABLE} fixture, with
     * {@link #getFriendlyName()} and {@link #getLocalName()} derived from the class name.
     *
     * <p>
     * Use {@link #withDiscoverability(Discoverability)} to override.
     */
    public DiscoverableFixtureScript() {
        this(null, null);
    }

    /**
     * Initializes a {@link Discoverability#DISCOVERABLE} fixture.
     *
     * <p>
     * Use {@link #withDiscoverability(Discoverability)} to override.
     *
     * @param friendlyName - if null, will be derived from class name
     * @param localName - if null, will be derived from class name
     */
    public DiscoverableFixtureScript(final String friendlyName, final String localName) {
        super(friendlyName, localName, Discoverability.DISCOVERABLE);
    }

}
