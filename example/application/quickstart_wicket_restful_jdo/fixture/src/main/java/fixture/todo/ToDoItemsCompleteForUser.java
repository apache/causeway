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

import java.util.Collection;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import dom.todo.ToDoItem;
import dom.todo.ToDoItems;

import org.apache.isis.applib.fixturescripts.FixtureResultList;
import org.apache.isis.applib.fixturescripts.SimpleFixtureScript;

public class ToDoItemsCompleteForUser extends SimpleFixtureScript {

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    private final String user;
    
    public ToDoItemsCompleteForUser(String user) {
        super(friendlyNameFor(user), localNameFor(user));
        this.user = user;
    }
    
    static String localNameFor(String user) {
        return user != null? user: "current";
    }

    static String friendlyNameFor(String user) {
        return "Complete selected ToDoItems for " + (user != null ? "'" + user + "'" : "current user");
    }

    @Override
    protected void doRun(final FixtureResultList resultList) {
        final String ownedBy = user != null? user: getContainer().getUser().getName();
        installFor(ownedBy, resultList);
        getContainer().flush();
    }

    private void installFor(final String user, final FixtureResultList resultList) {
        complete(user, "Buy stamps", resultList);
        complete(user, "Write blog post", resultList);

        getContainer().flush();
    }

    private void complete(final String user, String description, final FixtureResultList resultList) {
        final ToDoItem toDoItem = findToDoItem(description, user);
        toDoItem.setComplete(true);
        resultList.add(this, toDoItem);
    }

    private ToDoItem findToDoItem(final String description, final String user) {
        final Collection<ToDoItem> filtered = Collections2.filter(toDoItems.allToDos(), new Predicate<ToDoItem>() {
            @Override
            public boolean apply(ToDoItem input) {
                return Objects.equal(description, input.getDescription()) &&
                       Objects.equal(user, input.getOwnedBy());
            }
        });
        return filtered.isEmpty()? null: filtered.iterator().next();
    }


    // //////////////////////////////////////
    // Injected services
    // //////////////////////////////////////

    @javax.inject.Inject
    private ToDoItems toDoItems;
}