#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
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
package fixture.todo.scenarios;

import fixture.todo.items.actions.complete.ToDoItemCompleteForBuyStamps;
import fixture.todo.items.actions.complete.ToDoItemCompleteForWriteBlogPost;
import fixture.todo.util.Util;

import org.apache.isis.applib.fixturescripts.FixtureScript;

public class ToDoItemsRecreateAndCompleteSeveral extends FixtureScript {

    public ToDoItemsRecreateAndCompleteSeveral() {
        withDiscoverability(Discoverability.DISCOVERABLE);
    }

    //region > ownedBy (optional)
    private String ownedBy;

    public String getOwnedBy() {
        return ownedBy;
    }

    public void setOwnedBy(String ownedBy) {
        this.ownedBy = ownedBy;
    }
    //endregion

    @Override
    protected void execute(ExecutionContext executionContext) {

        // defaults
        executionContext.setParameterIfNotPresent(
                "ownedBy",
                Util.coalesce(getOwnedBy(), getContainer().getUser().getName()));

        // prereqs
        executeChild(new ToDoItemsRecreate(), executionContext);

        // this fixture
        executeChild(new ToDoItemCompleteForBuyStamps(), executionContext);
        executeChild(new ToDoItemCompleteForWriteBlogPost(), executionContext);
    }
}