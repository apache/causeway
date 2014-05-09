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

import org.apache.isis.applib.fixturescripts.FixtureResultList;
import org.apache.isis.applib.fixturescripts.SimpleFixtureScript;
import org.apache.isis.objectstore.jdo.applib.service.support.IsisJdoSupport;

public class ToDoItemsDeleteForUser extends SimpleFixtureScript {

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    private final String user;
    
    public ToDoItemsDeleteForUser(final String user) {
        super(friendlyNameFor(user), localNameFor(user));
        this.user = user;
    }
    
    static String localNameFor(final String user) {
        return user != null? user: "current";
    }

    static String friendlyNameFor(final String user) {
        return "Delete ToDoItems for " + (user != null ? "'" + user + "'" : "current user");
    }

    @Override
    protected void doRun(final FixtureResultList resultList) {
        final String ownedBy = user != null? user: getContainer().getUser().getName();
        isisJdoSupport.executeUpdate("delete from \"ToDoItem\" where \"ownedBy\" = '" + ownedBy + "'");
        getContainer().flush();
    }

    // //////////////////////////////////////
    // Injected services
    // //////////////////////////////////////

    @javax.inject.Inject
    private IsisJdoSupport isisJdoSupport;

}