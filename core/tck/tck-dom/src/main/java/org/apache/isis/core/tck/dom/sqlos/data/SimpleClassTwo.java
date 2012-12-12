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

/**
 * 
 */
package org.apache.isis.core.tck.dom.sqlos.data;

import org.apache.isis.applib.AbstractDomainObject;

/**
 * @author Kevin
 * 
 */
public class SimpleClassTwo extends AbstractDomainObject {
    
    public String title() {
        return text;
    }

    // {{ String type
    public String text;

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    // }}

    // {{ IntValue
    private int intValue;

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(final int value) {
        this.intValue = value;
    }

    // }}

    // {{ BooleanValue
    private boolean booleanValue;

    public boolean getBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(final boolean value) {
        this.booleanValue = value;
    }
    // }}

}
