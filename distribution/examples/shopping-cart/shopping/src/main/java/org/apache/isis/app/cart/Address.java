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


package org.apache.isis.app.cart;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.MultiLine;
import org.apache.isis.applib.annotation.TypicalLength;
import org.apache.isis.applib.annotation.When;


public class Address extends AbstractDomainObject {
    private String address;

    @Disabled(When.ONCE_PERSISTED)
    @MultiLine(numberOfLines = 6)
    @TypicalLength(50)
    public String getAddress() {
        resolve(address);
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
        objectChanged();
    }

    public String title() {
        if (address == null) {
            return "";
        } else {
            int breakAt = address.indexOf('\n');
            if (breakAt == -1) {
                return address;
            } else {
                return address.substring(0, breakAt);
            }
        }
    }
}

