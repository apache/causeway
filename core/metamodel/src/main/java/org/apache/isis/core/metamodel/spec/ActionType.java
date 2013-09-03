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

package org.apache.isis.core.metamodel.spec;

import java.util.Arrays;
import java.util.List;

import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

public enum ActionType {
    DEBUG, EXPLORATION, PROTOTYPE, USER;

    public String getName() {
        return name();
    }

    public boolean matchesTypeOf(final ObjectAction action) {
        return action != null && action.getType().equals(this);
    }

    public boolean isDebug() {
        return this == DEBUG;
    }

    public boolean isExploration() {
        return this == EXPLORATION;
    }

    public boolean isPrototype() {
        return this == PROTOTYPE;
    }

    public boolean isUser() {
        return this == USER;
    }

    public static final List<ActionType> ALL = Arrays.asList(values());
}
