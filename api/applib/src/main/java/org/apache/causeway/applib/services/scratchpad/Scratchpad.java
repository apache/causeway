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
package org.apache.causeway.applib.services.scratchpad;

import org.springframework.beans.factory.DisposableBean;

/**
 * This service (API and implementation) provides a mechanism to interchange information
 * between multiple objects invoked in the same interaction.
 *
 * @since 1.x {@index}
 */
public interface Scratchpad
        extends DisposableBean {

    /**
     * Obtain user-data, as set by a previous object being acted upon.
     *
     * <p>
     *     The key value should obey the general contract for hash maps.
     * </p>
     */
    public Object get(Object key);

    /**
     * Set user-data, for the use of a subsequent object being acted upon.
     *
     * <p>
     *     The key value should obey the general contract for hash maps.
     * </p>
     */
    public void put(Object key, Object value);

}
