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
package fixture.todo.items.create;

import dom.todo.ToDoItem.Category;
import dom.todo.ToDoItem.Subcategory;

public class ToDoItemForBuyStamps extends ToDoItemAbstract {

    public static final String DESCRIPTION = "Buy stamps";

    @Override
    protected void execute(ExecutionContext executionContext) {

        createToDoItem(
                DESCRIPTION,
                Category.Domestic, Subcategory.Shopping,
                nowPlusDays(0),
                BD("10.00"),
                executionContext);
    }

}