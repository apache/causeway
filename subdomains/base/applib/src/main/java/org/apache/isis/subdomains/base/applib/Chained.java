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
package org.apache.isis.subdomains.base.applib;

import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.subdomains.base.applib.with.WithInterval;
import org.apache.isis.subdomains.base.applib.with.WithIntervalContiguous;

/**
 * @since 2.0 {@index}
 */
public interface Chained<T extends Chained<T>> {


    /**
     * The object (usually an {@link WithInterval}, but not necessarily) that precedes this one, if any (not
     * necessarily contiguously)..
     *
     * <p>
     * Implementations where successive intervals are contiguous should instead implement
     * {@link WithIntervalContiguous}.
     */
    @Property(editing = Editing.DISABLED, hidden=Where.ALL_TABLES, optionality = Optionality.OPTIONAL)
    public T getPrevious();

    /**
     * The object (usually an {@link WithInterval}, but not necessarily) that succeeds this one, if any (not
     * necessarily contiguously).
     *
     * <p>
     * Implementations where successive intervals are contiguous should instead implement
     * {@link WithIntervalContiguous}.
     */
    @Property(editing = Editing.DISABLED, hidden=Where.ALL_TABLES, optionality = Optionality.OPTIONAL)
    public T getNext();


}
