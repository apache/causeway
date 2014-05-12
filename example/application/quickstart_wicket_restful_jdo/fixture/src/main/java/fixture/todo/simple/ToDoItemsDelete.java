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

import org.apache.isis.applib.fixturescripts.FixtureResultList;
import org.apache.isis.applib.fixturescripts.SimpleFixtureScript;
import org.apache.isis.objectstore.jdo.applib.service.support.IsisJdoSupport;

public class ToDoItemsDelete extends SimpleFixtureScript {

    //region > factory methods & constructor
    public static ToDoItemsDelete forCurrent() {
        return new ToDoItemsDelete(null);
    }

    public static ToDoItemsDelete forUser(final String user) {
        return new ToDoItemsDelete(user);
    }

    private final String user;
    
    private ToDoItemsDelete(final String user) {
        super(Util.friendlyNameFor("Delete ToDoItems for ", user), Util.localNameFor(user));
        this.user = user;
    }
    //endregion

    //region > doRun
    @Override
    protected void doRun(final String parameters, final FixtureResultList resultList) {
        final String ownedBy = Util.coalesce(user, parameters, getContainer().getUser().getName());
        installFor(ownedBy);
        getContainer().flush();
    }

    private void installFor(final String ownedBy) {
        isisJdoSupport.executeUpdate("delete from \"ToDoItem\" where \"ownedBy\" = '" + ownedBy + "'");
    }
    //endregion

    //region > injected services
    @javax.inject.Inject
    private IsisJdoSupport isisJdoSupport;
    //endregion

}