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
public class ClaimStatus extends Status {

    public static final String NEW = "New";

    @Hidden
    public boolean isNew() {
        return getTitleString().equals(NEW);
    }

    public static final String SUBMITTED = "Submitted For Approval";

    @Hidden
    public boolean isSubmitted() {
        return getTitleString().equals(SUBMITTED);
    }

    public static final String RETURNED = "Returned To Claimant";

    @Hidden
    public boolean isReturned() {
        return getTitleString().equals(RETURNED);
    }

    public static final String TO_BE_PAID = "Ready to be paid";

    @Hidden
    public boolean isToBePaid() {
        return getTitleString().equals(TO_BE_PAID);
    }

    public static final String PAID = "Paid";

    @Hidden
    public boolean isPaid() {
        return getTitleString().equals(PAID);
    }

}
