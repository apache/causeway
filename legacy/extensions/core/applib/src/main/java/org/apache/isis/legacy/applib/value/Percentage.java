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

package org.apache.isis.legacy.applib.value;

import org.apache.isis.applib.annotation.Value;

/**
 * @deprecated
 */
@Value(semanticsProviderName = "org.apache.isis.core.metamodel.facets.value.percentage.PercentageValueSemanticsProvider")
@Deprecated
public class Percentage extends Magnitude<Percentage> {

    private static final long serialVersionUID = 1L;
    private final float value;

    public Percentage(final float value) {
        this.value = value;
    }

    public Percentage add(final float value) {
        return new Percentage((floatValue() + value));
    }

    public Percentage add(final Percentage value) {
        return add(value.floatValue());
    }

    /**
     * Returns this value as an double.
     */
    public double doubleValue() {
        return value;
    }

    /**
     * Returns this value as an float.
     */
    public float floatValue() {
        return value;
    }

    /**
     * Returns this value as an int.
     */
    public int intValue() {
        return (int) value;
    }

    /**
     */
    @Override
    public boolean isEqualTo(final Percentage magnitude) {
        return (magnitude).value == value;
    }

    @Override
    public boolean isLessThan(final Percentage magnitude) {
        return value < (magnitude).value;
    }

    /**
     * Returns this value as an long.
     */
    public long longValue() {
        return (long) value;
    }

    public Percentage multiply(final float value) {
        return new Percentage((floatValue() * value));
    }

    /**
     * Returns this value as an short.
     */
    public short shortValue() {
        return (short) value;
    }

    public Percentage subtract(final float value) {
        return add(-value);
    }

    public Percentage subtract(final Percentage value) {
        return add(-value.floatValue());
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        return other.getClass() == this.getClass() && equals((Percentage) other);
    }

    public boolean equals(final Percentage other) {
        return value == other.value;
    }

    @Override
    public int hashCode() {
        // multiply by 100 just in case the percentage is being stored as 0.0 to
        // 1.0
        return (int) (floatValue() * 100);
    }

    @Override
    public String toString() {
        return "" + value;
    }
}
