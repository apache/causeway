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

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.example.expenses.recordedAction.Actor;
import org.apache.isis.example.expenses.recordedAction.RecordActionService;
import org.apache.isis.example.expenses.recordedAction.RecordedActionContext;
import org.apache.isis.example.expenses.services.UserFinder;

import java.util.Date;


public class RecordActionServiceImpl extends AbstractFactoryAndRepository implements RecordActionService {

    // {{ Injected Services

    // {{ Injected: UserFinder
    private UserFinder userFinder;

    /**
     * This property is not persisted, nor displayed to the user.
     */
    protected UserFinder getUserFinder() {
        return this.userFinder;
    }

    /**
     * Injected by the application container.
     */
    public void setUserFinder(final UserFinder userFinder) {
        this.userFinder = userFinder;
    }

    // }}

    // }}

    private void recordAction(final RecordedActionContext context, final String type, final String action, final String details) {

        final RecordedAction ra = newTransientInstance(RecordedAction.class);
        ra.setContext(context);
        ra.setType(type);
        ra.setName(action);
        ra.setDetails(details);
        ra.setActor((Actor) getUserFinder().currentUserAsObject());
        ra.setDate(new Date());
        persist(ra);
    }

    @Hidden
    public void recordMenuAction(final RecordedActionContext context, final String action, final String details) {

        recordAction(context, RecordedAction.ACTION, action, details);
    }

    @Hidden
    public void recordFieldChange(
            final RecordedActionContext context,
            final String fieldName,
            final Object previousContents,
            final Object newContents) {

        String fromValue;
        if (previousContents == null) {
            fromValue = "null";
        } else {
            fromValue = previousContents.toString();
        }

        String toValue;
        if (newContents == null) {
            toValue = "null";
        } else {
            toValue = newContents.toString();
        }

        if (fromValue.equals(toValue)) {
            return;
        }

        final String details = "From: " + fromValue + " to: " + toValue;
        recordAction(context, RecordedAction.CHANGE, fieldName, details);
    }

}
