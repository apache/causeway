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
import org.apache.isis.applib.annotation.Programmatic;

@Named("Simple Script")
public abstract class SimpleFixtureScript extends FixtureScript {

    private static Discoverability defaultDiscoverability() {
        return Discoverability.NON_DISCOVERABLE;
    }

    public SimpleFixtureScript(String localName) {
        this(localName, defaultDiscoverability());
    }
    public SimpleFixtureScript(String friendlyName, String localName) {
        this(friendlyName, localName, defaultDiscoverability());
    }
    public SimpleFixtureScript(FixtureScript parent, String friendlyName, String localName) {
        this(parent, friendlyName, localName, defaultDiscoverability());
    }
    
    public SimpleFixtureScript(String localName, Discoverability discoverability) {
        this(localName, localName, discoverability);
    }
    public SimpleFixtureScript(String friendlyName, String localName, Discoverability discoverability) {
        super(friendlyName, localName, discoverability);
    }
    public SimpleFixtureScript(FixtureScript parent, String friendlyName, String localName, Discoverability discoverability) {
        super(parent, friendlyName, localName, discoverability);
    }
    
    // //////////////////////////////////////

    @Programmatic
    protected abstract void doRun(FixtureResultList fixtureResults);

}
