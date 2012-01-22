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

public enum Persistability {
    /**
     * Marks a class as being persistable, but only by under application program
     * control.
     */
    PROGRAM_PERSISTABLE("Program Persistable", true),
    /**
     * Marks a class as transient - such an object cannot be persisted.
     */
    TRANSIENT("Transient", false),
    /**
     * Marks a class as being persistable by the user (or under application
     * program control).
     */
    USER_PERSISTABLE("User Persistable", true);

    private final String name;
    private final boolean persistable;

    private Persistability(final String name, final boolean persistable) {
        this.name = name;
        this.persistable = persistable;
    }

    public boolean isPersistable() {
        return persistable;
    }

    @Override
    public String toString() {
        return name;
    }
}
