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
package org.apache.isis.core.runtimeservices.scratchpad;

import java.util.Map;

import javax.annotation.Priority;
import javax.inject.Named;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.InteractionScope;
import org.apache.isis.applib.services.scratchpad.Scratchpad;
import org.apache.isis.commons.internal.collections._Maps;

@Service
@Named("isis.runtimeservices.Scratchpad")
@Priority(PriorityPrecedence.EARLY)
@Qualifier("Default")
@InteractionScope
//@Log4j2
public class ScratchpadDefault implements Scratchpad {

    /**
     * Provides a mechanism for each object being acted upon to pass
     * data to the next object.
     */
    private final Map<Object, Object> userData = _Maps.newHashMap();

    /**
     * Obtain user-data, as set by a previous object being acted upon.
     */
    @Override
    public Object get(Object key) {
        return userData.get(key);
    }

    /**
     * Set user-data, for the use of a subsequent object being acted upon.
     */
    @Override
    public void put(Object key, Object value) {
        userData.put(key, value);
    }

    /**
     * Clear any user data.
     */
    @Override
    public void destroy() throws Exception {
        userData.clear();
    }
}
