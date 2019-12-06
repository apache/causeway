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
package org.apache.isis.applib.services.scratchpad;

import lombok.extern.log4j.Log4j2;

import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.commons.internal.collections._Maps;
import org.springframework.stereotype.Service;

/**
 * This service (API and implementation) provides a mechanism to interchange information between multiple objects invoked in the same
 * interaction.  Most commonly this will be as the result of invoking a {@link org.apache.isis.applib.annotation.Bulk}
 * action.
 *
 * <p>
 * This implementation has only one implementation (this class) in applib, so it is annotated with
 * {@link org.apache.isis.applib.annotation.DomainService}.  This means that it is automatically registered and
 * available for use; no further configuration is required.
 */
@Service
@RequestScoped
@Named("isisApplib.scratchpad")
@Log4j2
public class Scratchpad {

    /**
     * Provides a mechanism for each object being acted upon to pass
     * data to the next object.
     */
    private final Map<Object, Object> userData = _Maps.newHashMap();

    /**
     * Obtain user-data, as set by a previous object being acted upon.
     */
    public Object get(Object key) {
        return userData.get(key);
    }
    /**
     * Set user-data, for the use of a subsequent object being acted upon.
     */
    public void put(Object key, Object value) {
        userData.put(key, value);
    }

    /**
     * Clear any user data.
     */
    public void clear() {
        userData.clear();
    }

}
