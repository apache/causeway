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


package org.apache.isis.example.expenses.claims;

import org.apache.isis.applib.annotation.Bounded;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Immutable;
import org.apache.isis.applib.annotation.When;


@Bounded
@Immutable(When.ONCE_PERSISTED)
public class ExpenseItemStatus extends Status {

    public static final String NEW_INCOMPLETE = "New - Incomplete";
    public static final String NEW_COMPLETE = "New - Complete";
    public static final String REJECTED = "Rejected";
    public static final String APPROVED = "Approved";
    public static final String QUERIED = "Queried";

    @Hidden
    public boolean isNewIncomplete() {
        return getTitleString().equals(NEW_INCOMPLETE);
    }

    @Hidden
    public boolean isNewComplete() {
        return getTitleString().equals(NEW_COMPLETE);
    }

    @Hidden
    public boolean isApproved() {
        return getTitleString().equals(APPROVED);
    }

    @Hidden
    public boolean isRejected() {
        return getTitleString().equals(REJECTED);
    }

    @Hidden
    public boolean isQueried() {
        return getTitleString().equals(QUERIED);
    }

}
