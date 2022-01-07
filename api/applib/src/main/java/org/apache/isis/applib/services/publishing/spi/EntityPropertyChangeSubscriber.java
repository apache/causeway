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
package org.apache.isis.applib.services.publishing.spi;

import org.apache.isis.applib.annotations.DomainObject;
import org.apache.isis.commons.having.HasEnabling;

/**
 * SPI called for each pre-post change to a property of a domain entity during
 * a result of the transaction.  The callback is therefore quite fine-grained
 * and will be called  many (many) times for within any given transaction.
 *
 * <p>
 *     Only those properties of entities that have publishing enabled (using
 *  * {@link DomainObject#entityChangePublishing()}) are included.
 * </p>
 *
 *
 * @since 2.0 {@index}
 */
public interface EntityPropertyChangeSubscriber extends HasEnabling {

    /**
     * Receives a single property change event for changing entities (with
     * publishing enabled using {@link DomainObject#entityChangePublishing()})
     * as an instance of {@link EntityPropertyChange}.
     *
     * <p>
     *     The callback is called (multiple times) at the end of the
     *     transaction, during the pre-commit phase.
     * </p>
     */
    void onChanging(EntityPropertyChange entityPropertyChange);

}
