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

package org.apache.isis.examples.onlinedemo.dom.items;

import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.value.Date;

public class ToDoItem {

    // {{ Description
    private String description;

    @Title
    @MemberOrder(sequence = "1")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    // }}

    // {{ Date (property)
    private Date dueBy;
    
    @MemberOrder(sequence = "1")
    public Date getDate() {
        return dueBy;
    }

    public void setDate(final Date dueBy) {
        this.dueBy = dueBy;
    }
    // }}

    // {{ Category (property)
    private Category category;

    @MemberOrder(sequence = "1")
    public Category getCategory() {
        return category;
    }

    public void setCategory(final Category category) {
        this.category = category;
    }
    // }}



    // {{ Complete
    private boolean complete;

    @Disabled
    @MemberOrder(sequence = "3")
    public boolean getComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }
    // }}

    // {{ completed
    @MemberOrder(sequence = "1")
    public void completed() {
        setComplete(true);
    }
    public String disableCompleted() {
        return complete?"Already completed":null;
    }
    // }}

    // {{ notYetCompleted
    @MemberOrder(sequence = "2")
    public void notYetCompleted() {
        setComplete(false);
    }
    public String disableNotYetCompleted() {
        return !complete?"Not yet completed":null;
    }
    // }}

    // {{ clone (action)
    @Named("Clone")
    @MemberOrder(sequence = "1")
    public ToDoItem duplicate() {
        return toDoItems.newToDo(getDescription(), getCategory()); 
    }
    // }}


    
    // {{ injected: ToDoItems
    private ToDoItems toDoItems;

    public void setToDoItems(final ToDoItems toDoItems) {
        this.toDoItems = toDoItems;
    }
    // }}

    
}
