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
package fixture.todo;

import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Prototype;
import org.apache.isis.applib.fixturescripts.FixtureResult;
import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.applib.fixturescripts.FixtureScripts;

/**
 * Enables fixtures to be installed from the application.
 */
@Named("Prototyping") // has the effect of defining a "Prototyping" menu item
public class ToDoItemsFixturesService extends FixtureScripts {

    public ToDoItemsFixturesService() {
        super("fixture.todo");
    }

    @Prototype
    public List<FixtureResult> runFixtureScriptForCurrent() {
        return findFixtureScriptFor("current").run();
    }
    public String disableRunFixtureScriptForCurrent() {
        return findFixtureScriptFor("current") == null? "Could not find fixture script 'current'": null;
    }
    private FixtureScript findFixtureScriptFor(String qualifiedName) {
        List<FixtureScript> fixtureScripts = choices0RunFixtureScript();
        for (FixtureScript fs : fixtureScripts) {
            fs.getQualifiedName().equals(qualifiedName);
            return fs;
        }
        return null;
    }
}
