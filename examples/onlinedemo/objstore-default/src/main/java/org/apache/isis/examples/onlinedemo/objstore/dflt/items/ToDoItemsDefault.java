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

package org.apache.isis.examples.onlinedemo.objstore.dflt.items;

import java.util.List;


import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.examples.onlinedemo.dom.items.Category;
import org.apache.isis.examples.onlinedemo.dom.items.ToDoItem;
import org.apache.isis.examples.onlinedemo.dom.items.ToDoItems;

public class ToDoItemsDefault extends AbstractFactoryAndRepository implements ToDoItems {

    // {{ Id, iconName
    @Override
    public String getId() {
        return "toDoItems";
    }

    public String iconName() {
        return "ToDoItem";
    }
    // }}

    
    @Override
    public List<ToDoItem> toDosForToday() {
        return allMatches(ToDoItem.class, new Filter<ToDoItem>() {
            @Override
            public boolean accept(ToDoItem t) {
                return !t.getComplete();
            }
        });
    }

    
    // {{ NewToDo
    @Override
    public ToDoItem newToDo(String description, Category category) {
        ToDoItem toDoItem = newTransientInstance(ToDoItem.class);
        toDoItem.setDescription(description);
        toDoItem.setCategory(category);
        persist(toDoItem);
        return toDoItem;
    }
    // }}


}
