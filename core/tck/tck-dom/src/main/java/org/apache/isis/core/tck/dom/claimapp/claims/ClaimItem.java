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

package org.apache.isis.core.tck.dom.claimapp.claims;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.value.Date;
import org.apache.isis.applib.value.Money;

public class ClaimItem extends AbstractDomainObject {

    // {{ Title
    public String title() {
        return getDescription();
    }

    // }}

    // {{ DateIncurred
    private Date dateIncurred;

    @MemberOrder(sequence = "1")
    public Date getDateIncurred() {
        return dateIncurred;
    }

    public void setDateIncurred(final Date dateIncurred) {
        this.dateIncurred = dateIncurred;
    }

    // }}

    // {{ Description
    private String description;

    @MemberOrder(sequence = "2")
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    // }}

    // {{ Amount
    private Money amount;

    @MemberOrder(sequence = "3")
    public Money getAmount() {
        return amount;
    }

    public void setAmount(final Money price) {
        this.amount = price;
    }
    // }}

}
