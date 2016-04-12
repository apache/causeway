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

package org.apache.isis.applib.services.publish;

import org.apache.isis.applib.annotation.Programmatic;

/**
 * No longer in use.
 *
 * @deprecated
 */
@Deprecated
public class EventPayloadForObjectChanged<T> implements EventPayload {
    
    private final T changed;
    private ObjectStringifier stringifier;

    public EventPayloadForObjectChanged(T changed) {
        this.changed = changed;
    }

    /**
     * Injected by Isis runtime immediately after instantiation.
     */
    @Deprecated
    @Programmatic
    public void withStringifier(ObjectStringifier stringifier) {
        this.stringifier = stringifier;
    }

    @Deprecated
    @Programmatic
    public T getChanged() {
        return changed;
    }

    @Deprecated
    @Programmatic
    public String getClassName() {
        if(stringifier == null) {
            throw new IllegalStateException("ObjectStringifier has not been injected");
        }
        return stringifier.classNameOf(changed);
    }

    @Override
    public String toString() {
        if(stringifier == null) {
            throw new IllegalStateException("ObjectStringifier has not been injected");
        }
        return stringifier.toString(changed);
    }
}