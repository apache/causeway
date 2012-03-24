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


package org.apache.isis.application.valueholder;

import org.apache.isis.application.BusinessObject;


public abstract class Magnitude extends BusinessValueHolder {
    private static final long serialVersionUID = 1L;

    protected Magnitude(final BusinessObject parent) {
        super(parent);
    }

    public boolean isBetween(final Magnitude minMagnitude, final Magnitude maxMagnitude) {
        this.ensureAtLeastPartResolved();
        return isGreaterThanOrEqualTo(minMagnitude) && isLessThanOrEqualTo(maxMagnitude);
    }

    public abstract boolean isEqualTo(final Magnitude magnitude);

    public boolean isGreaterThan(final Magnitude magnitude) {
        this.ensureAtLeastPartResolved();
        return magnitude.isLessThan(this);
    }

    public boolean isGreaterThanOrEqualTo(final Magnitude magnitude) {
        this.ensureAtLeastPartResolved();
        return !isLessThan(magnitude);
    }

    public abstract boolean isLessThan(final Magnitude magnitude);

    public boolean isLessThanOrEqualTo(final Magnitude magnitude) {
        this.ensureAtLeastPartResolved();
        return !isGreaterThan(magnitude);
    }

    public Magnitude max(final Magnitude magnitude) {
        this.ensureAtLeastPartResolved();
        return isGreaterThan(magnitude) ? this : magnitude;
    }

    public Magnitude min(final Magnitude magnitude) {
        this.ensureAtLeastPartResolved();
        return isLessThan(magnitude) ? this : magnitude;
    }

    /**
     * delegates the comparsion to the <code>isEqualTo</code> method if specified object is a
     * <code>Magnitude</code> else returns false.
     * 
     * @see BusinessValueHolder#isSameAs(BusinessValueHolder)
     */
    public final boolean isSameAs(BusinessValueHolder object) {
        this.ensureAtLeastPartResolved();
        if (object instanceof Magnitude) {
            return isEqualTo((Magnitude) object);
        } else {
            return false;
        }
    }
}
