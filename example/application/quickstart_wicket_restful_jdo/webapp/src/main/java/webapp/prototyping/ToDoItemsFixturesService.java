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

import com.google.common.collect.Lists;

import dom.todo.ToDoItem;
import dom.todo.ToDoItems;
import fixture.todo.ToDoItemsFixture;

import org.apache.isis.applib.AbstractService;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Prototype;
import org.apache.isis.core.runtime.fixtures.FixturesInstallerDelegate;

/**
 * Enables fixtures to be installed from the application.
 */
@Named("Prototyping") // has the effect of defining a "Prototyping" menu item
public class ToDoItemsFixturesService extends AbstractService {

    @Prototype
    public String installFixtures() {
        installFixturesFor(null); // ie current user
        return "Example fixtures installed";
    }

    // //////////////////////////////////////

    @Prototype
    public String installFixturesForUser(@Named("User") String user) {
        installFixturesFor(user);
        return "Example fixtures installed for " + user;
    }
    public String default0InstallFixturesForUser() {
        return "guest";
    }
    public List<String> choices0InstallFixturesForUser() {
        return Lists.newArrayList("guest", "sven", "dick", "bob", "joe");
    }

    // //////////////////////////////////////

    @Prototype
    public ToDoItem installFixturesAndReturnFirst() {
        installFixtures();
        List<ToDoItem> notYetComplete = toDoItems.notYetComplete();
        return !notYetComplete.isEmpty() ? notYetComplete.get(0) : null;
    }

    // //////////////////////////////////////

    private static void installFixturesFor(String user) {
        final FixturesInstallerDelegate installer = new FixturesInstallerDelegate().withOverride();
        installer.addFixture(new ToDoItemsFixture(user));
        installer.installFixtures();
    }
    
    // //////////////////////////////////////

    private ToDoItems toDoItems;
    public void injectToDoItems(ToDoItems toDoItems) {
        this.toDoItems = toDoItems;
    }

}
