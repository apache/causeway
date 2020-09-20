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

import java.io.Serializable;

/**
 * @deprecated
 */
@Deprecated
public abstract class Magnitude<T extends Magnitude<T>> implements Serializable {
    private static final long serialVersionUID = 1L;

    public boolean isBetween(final T minMagnitude, final T maxMagnitude) {
        return isGreaterThanOrEqualTo(minMagnitude) && isLessThanOrEqualTo(maxMagnitude);
    }

    public abstract boolean isEqualTo(final T magnitude);

    public boolean isGreaterThan(final T magnitude) {
        return magnitude.isLessThan(thisAsT());
    }

    public boolean isGreaterThanOrEqualTo(final T magnitude) {
        return !isLessThan(magnitude);
    }

    public abstract boolean isLessThan(final T magnitude);

    public boolean isLessThanOrEqualTo(final T magnitude) {
        return !isGreaterThan(magnitude);
    }

    public T max(final T magnitude) {
        return isGreaterThan(magnitude) ? thisAsT() : magnitude;
    }

    public T min(final T magnitude) {
        return isLessThan(magnitude) ? thisAsT() : magnitude;
    }

    @SuppressWarnings("unchecked")
    private T thisAsT() {
        return (T) this;
    }

}
