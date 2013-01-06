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

import java.util.List;

import org.apache.isis.applib.AbstractService;
import org.apache.isis.applib.annotation.Named;

import com.google.common.collect.Lists;

import dom.todo.ToDoItems;

/**
 * Enables fixtures to be installed from the application.
 */
@Named("Fixtures")
public class ToDoItemsFixturesService extends AbstractService {

    public String install() {
        final ToDoItemsFixture fixture = new ToDoItemsFixture();
        fixture.setContainer(getContainer());
        fixture.setToDoItems(toDoItems);
        fixture.install();
        return "Example fixtures installed";
    }

    public String installFor(@Named("User") String user) {
        final ToDoItemsFixture fixture = new ToDoItemsFixture();
        fixture.setContainer(getContainer());
        fixture.setToDoItems(toDoItems);
        fixture.installFor(user);
        return "Example fixtures installed for " + user;
    }
    public String default0InstallFor() {
        return "guest";
    }
    public List<String> choices0InstallFor() {
        return Lists.newArrayList("guest", "dick", "bob", "joe");
    }

    
    private ToDoItems toDoItems;
    public void setToDoItems(final ToDoItems toDoItems) {
        this.toDoItems = toDoItems;
    }

}
