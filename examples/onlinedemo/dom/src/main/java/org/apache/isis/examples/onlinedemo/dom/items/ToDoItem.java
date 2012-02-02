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

import java.util.List;

import com.google.common.base.Objects;

import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Ignore;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.RegEx;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.applib.util.TitleBuffer;
import org.apache.isis.applib.value.Date;

/**
 * A todo item (task) owned by a particular user.
 */
public class ToDoItem implements Comparable<ToDoItem> {

    private static final long ONE_WEEK_IN_MILLIS = 7 * 24 * 60 * 60 * 1000L;

    // {{ Identification on the UI
    public String title() {
        final TitleBuffer buf = new TitleBuffer();
        buf.append(getDescription());
        if (isComplete()) {
            buf.append(" - Completed!");
        } else {
            if (getDueBy() != null) {
                buf.append(" due by ", getDueBy());
            }
        }
        return buf.toString();
    }

    // }}

    // {{ Description (property)
    private String description;

    @RegEx(validation = "\\w[@&:\\-\\,\\.\\+ \\w]*")
    // words, spaces and selected punctuation
    @MemberOrder(sequence = "1")
    // ordering within UI
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    // }}

    // {{ DueBy (property)
    private Date dueBy;

    @Optional
    // need not be set
    @MemberOrder(sequence = "2")
    public Date getDueBy() {
        return dueBy;
    }

    public void setDueBy(final Date dueBy) {
        this.dueBy = dueBy;
    }

    // proposed new value is validated before setting
    public String validateDueBy(final Date dueBy) {
        if (dueBy == null) {
            return null;
        }
        return isMoreThanOneWeekInPast(dueBy) ? "Due by date cannot be more than one week old" : null;
    }

    // }}

    // {{ Category (property)
    private Category category;

    @MemberOrder(sequence = "3")
    public Category getCategory() {
        return category;
    }

    public void setCategory(final Category category) {
        this.category = category;
    }

    // }}

    // {{ UserName (property)
    private String userName;

    @Hidden
    // not shown in the UI
    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    // }}

    // {{ Complete (property)
    private boolean complete;

    @Disabled
    // cannot be edited as a property
    @MemberOrder(sequence = "5")
    public boolean isComplete() {
        return complete;
    }

    public void setComplete(final boolean complete) {
        this.complete = complete;
    }

    // }}

    // {{ completed (action)
    @MemberOrder(sequence = "1")
    public ToDoItem completed() {
        setComplete(true);
        return this;
    }

    // disable action dependent on state of object
    public String disableCompleted() {
        return complete ? "Already completed" : null;
    }

    // }}

    // {{ notYetCompleted (action)
    @MemberOrder(sequence = "2")
    public ToDoItem notYetCompleted() {
        setComplete(false);
        return this;
    }

    // disable action dependent on state of object
    public String disableNotYetCompleted() {
        return !complete ? "Not yet completed" : null;
    }

    // }}

    // {{ clone (action)
    @Named("Clone")
    // the name of the action in the UI
    @MemberOrder(sequence = "3")
    // nb: method is not called "clone()" is inherited by java.lang.Object and
    // (a) has different semantics and (b) is in any case automatically ignored
    // by the framework
    public ToDoItem duplicate() {
        return toDoItems.newToDo(getDescription() + " - Copy", getCategory(), getDueBy());
    }

    // }}

    // {{ isDue (programmatic)
    @Ignore
    // excluded from the framework's metamodel
    public boolean isDue() {
        if (getDueBy() == null) {
            return false;
        }
        return !isMoreThanOneWeekInPast(getDueBy());
    }

    // }}

    // {{ SimilarItems (collection)
    @MemberOrder(sequence = "5")
    public List<ToDoItem> getSimilarItems() {
        return toDoItems.similarTo(this);
    }

    // }}

    // {{ compareTo (programmatic)
    /**
     * by complete flag, then due by date, then description
     */
    @Ignore
    // exclude from the framework's metamodel
    @Override
    public int compareTo(final ToDoItem other) {
        if (isComplete() && !other.isComplete()) {
            return +1;
        }
        if (!isComplete() && other.isComplete()) {
            return -1;
        }
        if (getDueBy() == null && other.getDueBy() != null) {
            return +1;
        }
        if (getDueBy() != null && other.getDueBy() == null) {
            return -1;
        }
        if (getDueBy() == null && other.getDueBy() == null || getDueBy().equals(this.getDescription())) {
            return getDescription().compareTo(other.getDescription());
        }
        return (int) (getDueBy().getMillisSinceEpoch() - other.getDueBy().getMillisSinceEpoch());
    }

    // }}

    // {{ helpers
    private static boolean isMoreThanOneWeekInPast(final Date dueBy) {
        return dueBy.getMillisSinceEpoch() < Clock.getTime() - ONE_WEEK_IN_MILLIS;
    }

    // }}

    // {{ filters (programmatic)
    public static Filter<ToDoItem> thoseDue() {
        return Filters.and(Filters.not(thoseComplete()), new Filter<ToDoItem>() {
            @Override
            public boolean accept(final ToDoItem t) {
                return t.isDue();
            }
        });
    }

    public static Filter<ToDoItem> thoseComplete() {
        return new Filter<ToDoItem>() {
            @Override
            public boolean accept(final ToDoItem t) {
                return t.isComplete();
            }
        };
    }

    public static Filter<ToDoItem> thoseOwnedBy(final String currentUser) {
        return new Filter<ToDoItem>() {
            @Override
            public boolean accept(final ToDoItem toDoItem) {
                return Objects.equal(toDoItem.getUserName(), currentUser);
            }

        };
    }

    public static Filter<ToDoItem> thoseSimilarTo(final ToDoItem toDoItem) {
        return new Filter<ToDoItem>() {
            @Override
            public boolean accept(final ToDoItem eachToDoItem) {
                return Objects.equal(toDoItem.getCategory(), eachToDoItem.getCategory()) && 
                       Objects.equal(toDoItem.getUserName(), eachToDoItem.getUserName()) &&
                       eachToDoItem != toDoItem;
            }

        };
    }

    // }}

    // {{ injected: ToDoItems
    private ToDoItems toDoItems;

    public void setToDoItems(final ToDoItems toDoItems) {
        this.toDoItems = toDoItems;
    }
    // }}

}
