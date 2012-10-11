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

package objstore.jdo.todo;

import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import dom.todo.ToDoItem;
import dom.todo.ToDoItems;

import org.apache.isis.applib.query.QueryDefault;

public class ToDoItemsJdo extends ToDoItems {

    @Override
    public List<ToDoItem> notYetDone() {
        final String userName = getContainer().getUser().getName();
        return allMatches(
                new QueryDefault<ToDoItem>(ToDoItem.class, "todo_notYetDone", "ownedBy", userName));
    }


    // {{ SimilarTo (action)
    @Override
    public List<ToDoItem> similarTo(final ToDoItem thisToDoItem) {
        final List<ToDoItem> similarToDoItems = allMatches(
                new QueryDefault<ToDoItem>(ToDoItem.class, 
                        "todo_similarTo", 
                        "ownedBy", thisToDoItem.getOwnedBy(), 
                        "category", thisToDoItem.getCategory()));
        return Lists.newArrayList(Iterables.filter(similarToDoItems, excluding(thisToDoItem)));
    }

    private static Predicate<ToDoItem> excluding(final ToDoItem toDoItem) {
        return new Predicate<ToDoItem>() {
            @Override
            public boolean apply(ToDoItem input) {
                return input != toDoItem;
            }
        };
    }
    // }}


}
