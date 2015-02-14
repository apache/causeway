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
package fixture.todo.items.actions.complete;

import dom.todo.ToDoItem;

import java.util.Collection;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.apache.isis.applib.fixturescripts.FixtureScript;

public abstract class ToDoItemCompleteAbstract extends FixtureScript {

    /**
     * Looks up item from repository, and completes.
     */
    protected void complete(final String description, final ExecutionContext executionContext) {
        String ownedBy = executionContext.getParameter("ownedBy");
        final ToDoItem toDoItem = findToDoItem(description, ownedBy);
        toDoItem.setComplete(true);
        executionContext.addResult(this, toDoItem);
    }

    private ToDoItem findToDoItem(final String description, final String ownedBy) {
        final Collection<ToDoItem> filtered = Collections2.filter(getContainer().allInstances(ToDoItem.class), new Predicate<ToDoItem>() {
            @Override
            public boolean apply(ToDoItem input) {
                return Objects.equal(description, input.getDescription()) &&
                       Objects.equal(ownedBy, input.getOwnedBy());
            }
        });
        return filtered.isEmpty()? null: filtered.iterator().next();
    }
    //endregion
}