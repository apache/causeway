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
package org.apache.isis.applib.fixturescripts;

import org.apache.isis.applib.annotation.Named;

@Named("Composite Script")
public abstract class CompositeFixtureScript extends FixtureScript {

    private static Discoverability defaultDiscoverability() {
        return Discoverability.DISCOVERABLE;
    }

    /**
     * Initializes a {@link Discoverability#DISCOVERABLE} fixture, with 
     * {@link #getFriendlyName()} and {@link #getLocalName()} derived from the class name.
     * 
     * <p>
     * Use {@link #setDiscoverability(Discoverability)} to override.
     */
    public CompositeFixtureScript() {
        this(null, null);
    }
    
    /**
     * Initializes a {@link Discoverability#DISCOVERABLE} fixture.
     * 
     * <p>
     * Use {@link #setDiscoverability(Discoverability)} to override.
     * 
     * @param friendlyName - if null, will be derived from class name
     * @param localName - if null, will be derived from class name
     */
    public CompositeFixtureScript(final String friendlyName, final String localName) {
        super(friendlyName, localName, defaultDiscoverability());
    }
    
    // //////////////////////////////////////

    /**
     * Adds a child {@link FixtureScript fixture script}, overriding its default name with one more
     * meaningful in the context of this fixture.
     */
    protected final void execute(final String localNameOverride, final FixtureScript fixtureScript, ExecutionContext executionContext) {
        fixtureScript.setParentPath(pathWith(""));
        if(localNameOverride != null) {
            fixtureScript.setLocalName(localNameOverride);
        }
        getContainer().injectServicesInto(fixtureScript);
        fixtureScript.execute(executionContext);
    }
    /**
     * Executes a child {@link FixtureScript fixture script} (simply using its default name).
     */
    protected final void execute(final FixtureScript fixtureScript, ExecutionContext executionContext) {
        execute(null, fixtureScript, executionContext);
    }

}
