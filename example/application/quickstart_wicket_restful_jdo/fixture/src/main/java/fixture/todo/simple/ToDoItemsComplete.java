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
package fixture.todo.simple;

import java.util.Collection;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import dom.todo.ToDoItem;

import org.apache.isis.applib.fixturescripts.FixtureResultList;
import org.apache.isis.applib.fixturescripts.SimpleFixtureScript;
import org.apache.isis.applib.fixturescripts.FixtureScript.ExecutionContext;

public class ToDoItemsComplete extends SimpleFixtureScript {

    //region > factory methods & constructor
    public static ToDoItemsComplete forCurrent() {
        return new ToDoItemsComplete(null);
    }

    public static ToDoItemsComplete forUser(final String user) {
        return new ToDoItemsComplete(user);
    }

    private final String user;
    private ToDoItemsComplete(final String user) {
        super(null, Util.localNameFor("complete", user));
        this.user = user;
    }
    //endregion

    //region > doRun
    @Override
    protected void execute(ExecutionContext executionContext) {
        final String ownedBy = Util.coalesce(user, executionContext.getParameters(), getContainer().getUser().getName());
        installFor(ownedBy, executionContext);
        getContainer().flush();
    }

    private void installFor(final String user, final ExecutionContext executionContext) {
        complete(user, "Buy stamps", executionContext);
        complete(user, "Write blog post", executionContext);

        getContainer().flush();
    }

    private void complete(final String user, final String description, final ExecutionContext executionContext) {
        final ToDoItem toDoItem = findToDoItem(description, user);
        toDoItem.setComplete(true);
        executionContext.add(this, toDoItem);
    }

    private ToDoItem findToDoItem(final String description, final String user) {
        final Collection<ToDoItem> filtered = Collections2.filter(getContainer().allInstances(ToDoItem.class), new Predicate<ToDoItem>() {
            @Override
            public boolean apply(ToDoItem input) {
                return Objects.equal(description, input.getDescription()) &&
                       Objects.equal(user, input.getOwnedBy());
            }
        });
        return filtered.isEmpty()? null: filtered.iterator().next();
    }
    //endregion
}