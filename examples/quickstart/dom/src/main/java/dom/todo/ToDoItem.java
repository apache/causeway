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

package dom.todo;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Optional;

public class ToDoItem extends AbstractDomainObject {

    // {{ Title
    public String title() {
        return getDescription();
    }
    // }}


    // {{ Description
    private String description;

    @MemberOrder(sequence = "1")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    // }}

    
    // {{ Done
    private boolean done;

    @Disabled
    @MemberOrder(sequence = "3")
    public boolean isComplete() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
    // }}



    // {{ markAsDone
    @MemberOrder(sequence = "1")
    public void markAsDone() {
        setDone(true);
    }
    public String disableMarkAsDone() {
        return done?"Already done":null;
    }
    // }}

    // {{ markAsNotDone
    @MemberOrder(sequence = "2")
    public void markAsNotDone() {
        setDone(false);
    }
    public String disableMarkAsNotDone() {
        return !done?"Not yet done":null;
    }
    // }}

}
