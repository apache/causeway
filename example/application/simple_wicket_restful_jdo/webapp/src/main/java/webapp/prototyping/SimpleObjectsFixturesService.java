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
package webapp.prototyping;

import java.util.List;

import dom.simple.SimpleObject;
import dom.simple.SimpleObjects;
import fixture.simple.SimpleObjectsFixture;

import org.apache.isis.applib.AbstractService;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Prototype;
import org.apache.isis.core.runtime.fixtures.FixturesInstallerDelegate;

/**
 * Enables fixtures to be installed from the application.
 */
@Named("Prototyping")
public class SimpleObjectsFixturesService extends AbstractService {

    @Prototype
    public String installFixtures() {
        final FixturesInstallerDelegate installer = new FixturesInstallerDelegate().withOverride();
        installer.addFixture(new SimpleObjectsFixture());
        installer.installFixtures();
        return "Example fixtures installed";
    }

    // //////////////////////////////////////

    @Prototype
    public SimpleObject installFixturesAndReturnFirst() {
        installFixtures();
        List<SimpleObject> all = simpleObjects.listAll();
        return !all.isEmpty() ? all.get(0) : null;
    }

    
    // //////////////////////////////////////

    private SimpleObjects simpleObjects;
    public void injectSimpleObjects(SimpleObjects simpleObjects) {
        this.simpleObjects = simpleObjects;
    }

}
