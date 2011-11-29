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
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Ignore;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.applib.util.TitleBuffer;
import org.apache.isis.applib.value.Date;

import com.google.common.base.Objects;

public class ToDoItem implements Comparable<ToDoItem> {

    // {{ filters (programmatic)
    public static Filter<ToDoItem> thoseDue() {
        return Filters.and(
                Filters.not(thoseComplete()), 
                new Filter<ToDoItem>() {
            @Override
            public boolean accept(ToDoItem t) {
                return t.isDue();
            }
        });
    }
    
    public static Filter<ToDoItem> thoseComplete() {
        return new Filter<ToDoItem>() {
            @Override
            public boolean accept(ToDoItem t) {
                return t.isComplete();
            }
        };
    }

    public static Filter<ToDoItem> thoseOwnedBy(final String currentUser) {
        return new Filter<ToDoItem>() {
            @Override
            public boolean accept(ToDoItem toDoItem) {
                return Objects.equal(toDoItem.getUserName(), currentUser);
            }
            
        };
    }
    // }}
    
    
    // {{ Identification
    public String title() {
        final TitleBuffer buf = new TitleBuffer();
        buf.append(getDescription());
        if(isComplete()) {
            buf.append(" - Completed!");
        } else {
            if(getDueBy() != null) {
                buf.append(" due by ", getDueBy());
            }
        }
        return buf.toString();
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

    // {{ Date (property)
    private Date dueBy;
    
    @MemberOrder(sequence = "1")
    public Date getDueBy() {
        return dueBy;
    }

    public void setDueBy(final Date dueBy) {
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

    // {{ UserName (property)
    private String userName;

    @MemberOrder(sequence = "1")
    @Hidden
    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }
    // }}


    // {{ Complete
    private boolean complete;

    @Disabled
    @MemberOrder(sequence = "3")
    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }
    // }}

    // {{ completed
    @MemberOrder(sequence = "1")
    public ToDoItem completed() {
        setComplete(true);
        return this;
    }
    public String disableCompleted() {
        return complete?"Already completed":null;
    }
    // }}

    // {{ notYetCompleted
    @MemberOrder(sequence = "2")
    public ToDoItem notYetCompleted() {
        setComplete(false);
        return this;
    }
    public String disableNotYetCompleted() {
        return !complete?"Not yet completed":null;
    }
    // }}

    
    // {{ clone (action)
    @Named("Clone")
    @MemberOrder(sequence = "1")
    public ToDoItem duplicate() {
        return toDoItems.newToDo(getDescription() + " - Copy", getCategory(), getDueBy()); 
    }
    // }}


    // {{ isDue (programmatic)
    @Ignore
    public boolean isDue() {
        if(getDueBy() == null) {
            return false;
        }
        final Date now = new Date(Clock.getTimeAsDateTime());
        return now.isGreaterThan(getDueBy());
    }
    // }}

    // {{ compareTo (programmatic)
    /**
     * by complete flag, then due by date, then description
     */
    @Ignore
    @Override
    public int compareTo(ToDoItem other) {
        if(isComplete() && !other.isComplete()) {
            return +1;
        }
        if(!isComplete() && other.isComplete()) {
            return -1;
        }
        if(getDueBy() == null && other.getDueBy() != null) {
            return +1;
        }
        if(getDueBy() != null && other.getDueBy() == null) {
            return -1;
        }
        if( getDueBy() == null && other.getDueBy() == null || 
            getDueBy().equals(this.getDescription())) {
            return getDescription().compareTo(other.getDescription());
        }
        return (int) (getDueBy().getMillisSinceEpoch() - other.getDueBy().getMillisSinceEpoch());
    }
    // }}

    
    // {{ injected: ToDoItems
    private ToDoItems toDoItems;
    public void setToDoItems(final ToDoItems toDoItems) {
        this.toDoItems = toDoItems;
    }
    // }}



    
    

    
}
