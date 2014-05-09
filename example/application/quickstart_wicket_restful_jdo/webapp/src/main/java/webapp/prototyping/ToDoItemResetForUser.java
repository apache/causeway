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

import org.apache.isis.applib.fixturescripts.CompositeFixtureScript;

public abstract class ToDoItemResetForUser extends CompositeFixtureScript {

    // //////////////////////////////////////
    // Subclasses
    // //////////////////////////////////////

    public static final class ToDoItemResetForDick extends ToDoItemResetForUser {
        public ToDoItemResetForDick() {
            super("dick");
        }
    }

    public static final class ToDoItemResetForJoe extends ToDoItemResetForUser {
        public ToDoItemResetForJoe() {
            super("joe");
        }
    }
    
    public static final class ToDoItemResetForSven extends ToDoItemResetForUser {
        public ToDoItemResetForSven() {
            super("sven");
        }
    }
    
    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////
    
    private String user;

    public ToDoItemResetForUser(String user) {
        super("Reset ToDoItems for '" + user + "'", user);
        this.user = user;
    }

    @Override
    protected void addChildren() {
        add("delete", new ToDoItemsDeleteForUser(user));
        add("create", new ToDoItemsCreateForUser(user));
    }
    
}