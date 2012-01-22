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

package org.apache.isis.examples.onlinedemo.dom.demo;

import java.util.List;

import org.apache.isis.applib.annotation.Idempotent;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.examples.onlinedemo.dom.items.ToDoItem;

/**
 * Install fixtures (test data set) for the currently logged-in user.
 * 
 * <p>
 * Typically fixtures are installed directly by the framework, using the
 * <tt>isis.fixtures</tt> key within the <tt>isis.properties</tt> configuration
 * file. In this case though, because the online demo is intended to be
 * multi-user, we have made this capability available directly in the UI. (In
 * production, this service would be excluded from the final build)
 */
@Named("Demo")
// name to use in the UI
public interface DemoFixtures {

    @Idempotent
    // post-conditions are always same
    @MemberOrder(sequence = "1")
    public List<ToDoItem> resetFixtures();

}
