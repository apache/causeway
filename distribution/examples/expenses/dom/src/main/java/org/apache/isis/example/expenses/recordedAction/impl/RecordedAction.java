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


package org.apache.isis.example.expenses.recordedAction.impl;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Immutable;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.When;
import org.apache.isis.example.expenses.recordedAction.Actor;
import org.apache.isis.example.expenses.recordedAction.RecordedActionContext;

import java.util.Date;


@Immutable(When.ONCE_PERSISTED)
public class RecordedAction extends AbstractDomainObject {

    // {{ Title
    /**
     * Defines the title that will be displayed on the user interface in order to identity this object.
     */
    public String title() {
        final StringBuffer t = new StringBuffer();
        t.append(getType()).append(" ").append(getName());
        return t.toString();
    }
    // }}

    // {{ Context field
    private RecordedActionContext context;

    /**
     * 
     */
    @Hidden
    public RecordedActionContext getContext() {
        return context;
    }

    /**
     * @see #getContext
     */
    public void setContext(final RecordedActionContext context) {
        this.context = context;
    }
    // }}

    // {{ Date field
    private Date date;

    /**
     * Date (incl. time) that the action was recorded.
     */
    @MemberOrder(sequence = "1")
    @Disabled
    public Date getDate() {
        return date;
    }

    /**
     * @see #getDate
     */
    public void setDate(final Date date) {
        this.date = date;
    }
    // }}

    // {{ Type field
    public static final String CHANGE = "Change";
    public static final String ACTION = "Action";

    private String type;

    /**
     * 
     */
    @MemberOrder(sequence = "2")
    @Disabled
    public String getType() {
        return type;
    }

    /**
     * @see #getType
     */
    public void setType(final String type) {
        this.type = type;
    }
    // }}

    // {{ Name field
    private String name;

    /**
     * Where the Type is 'Menu Action', this field holds the name of the action; where the type is 'Field Change'
     * the name is the name of the field changed.
     */
    @MemberOrder(sequence = "3")
    @Disabled
    public String getName() {
        return name;
    }

    /**
     * @see #getAction
     */
    public void setName(final String action) {
        this.name = action;
    }
    // }}

    // {{ Details field
    private String details;

    /**
     * Any details of the action e.g. the contents of a field 'before and after'
     */
    @MemberOrder(sequence = "5")
    @Disabled
    public String getDetails() {
        return details;
    }

    /**
     * @see #getDetails
     */
    public void setDetails(final String details) {
        this.details = details;
    }
    // }}

    // {{ User field
    private Actor actor;

    /**
     * 
     */
    @MemberOrder(sequence = "6")
    @Disabled
    public Actor getActor() {
        return actor;
    }

    /**
     * @see #getUser
     */
    public void setActor(final Actor actor) {
        this.actor = actor;
    }
    // }}

}
