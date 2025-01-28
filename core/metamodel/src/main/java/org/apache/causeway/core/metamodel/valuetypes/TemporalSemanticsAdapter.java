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
package org.apache.causeway.core.metamodel.valuetypes;

import java.time.Duration;
import java.time.temporal.Temporal;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.value.semantics.TemporalCharacteristicsProvider;
import org.apache.causeway.applib.value.semantics.TemporalSupport;

public abstract class TemporalSemanticsAdapter<T, D extends Temporal>
extends ValueSemanticsAdapter<T, D, Duration>
implements
    TemporalSupport<T> {

    // -- TEMPORAL DECOMPOSITION

    @SuppressWarnings("unchecked")
    @Override
    public final Optional<TemporalDecomposition> decomposeTemporal(final @Nullable T value) {
        return ((TemporalSupport<D>)getDelegate()).decomposeTemporal(toDelegateValue(value));
    }

    // -- ORDER RELATION

    protected final static Duration ALMOST_A_SECOND = Duration.ofNanos(999_999_999);
    protected final static Duration ALMOST_A_MILLI_SECOND = Duration.ofNanos(999_999);

    @Override
    public final TemporalCharacteristic getTemporalCharacteristic() {
        return ((TemporalCharacteristicsProvider)getDelegate()).getTemporalCharacteristic();
    }

    @Override
    public final OffsetCharacteristic getOffsetCharacteristic() {
        return ((TemporalCharacteristicsProvider)getDelegate()).getOffsetCharacteristic();
    }

}
