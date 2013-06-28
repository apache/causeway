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
package org.apache.isis.applib.services.eventbus;

import org.apache.isis.applib.util.ObjectContracts;

public abstract class ChangedEvent<S,T> {
    private final S source;
    private final T oldValue;
    private final T newValue;
    
    public ChangedEvent(S source, T oldValue, T newValue) {
        this.source = source;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public S getSource() {
        return source;
    }
    
    public T getOldValue() {
        return oldValue;
    }
    public T getNewValue() {
        return newValue;
    }
    
    @Override
    public String toString() {
        return ObjectContracts.toString(this, "source,oldValue,newValue");
    }
}