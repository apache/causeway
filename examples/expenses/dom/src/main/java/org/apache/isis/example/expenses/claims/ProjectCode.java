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

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Bounded;
import org.apache.isis.applib.annotation.Immutable;
import org.apache.isis.applib.annotation.When;


@Bounded
@Immutable(When.ONCE_PERSISTED)
public class ProjectCode extends AbstractDomainObject {

    // {{ Title & Icon
    public String title() {
        final StringBuilder t = new StringBuilder();
        t.append(getCode()).append(" ").append(getDescription());
        return t.toString();
    }

    public String iconName() {
        return "Look Up";
    }

    // }}

    // {{ Code
    private String code;

    public String getCode() {
        return this.code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    // }}

    // {{ Description
    private String description;

    public String getDescription() {
        return this.description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    // }}

}
