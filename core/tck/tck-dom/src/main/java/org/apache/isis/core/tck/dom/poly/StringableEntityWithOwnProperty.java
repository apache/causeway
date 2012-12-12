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

package org.apache.isis.core.tck.dom.poly;

import org.apache.isis.applib.AbstractDomainObject;

public class StringableEntityWithOwnProperty extends AbstractDomainObject implements Stringable {

    public String title() {
        return string;
    }

    // {{ String type
    private String string;
    @Override
    public String getString() {
        return string;
    }
    public void setString(final String string) {
        this.string = string;
    }
    // }}

    // {{ Special
    private String special;
    public String getSpecial() {
        return special;
    }
    public void setSpecial(final String special) {
        this.special = special;
    }
    // }}

}
