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
package org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses;

import org.apache.isis.applib.AbstractDomainObject;

/**
 * @author Kevin
 * 
 */
public class NumericTestClass extends AbstractDomainObject {
    public String title() {
        return "numeric test";
    }

    // Basic value types
    // {{ IntValue
    private int intValue;

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(final int value) {
        this.intValue = value;
    }

    // }}

    // {{ ShortValue
    private short shortValue;

    public short getShortValue() {
        return shortValue;
    }

    public void setShortValue(final short value) {
        this.shortValue = value;
    }

    // }}

    // {{ LongValue
    private long longValue;

    public long getLongValue() {
        return longValue;
    }

    public void setLongValue(final long value) {
        this.longValue = value;
    }

    // }}

    // {{ FloatValue
    private Float floatValue;

    public Float getFloatValue() {
        return floatValue;
    }

    public void setFloatValue(final Float value) {
        this.floatValue = value;
    }

    // }}

    // {{ DoubleValue
    private double doubleValue;

    public double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(final double value) {
        this.doubleValue = value;
    }

    // }}

}
