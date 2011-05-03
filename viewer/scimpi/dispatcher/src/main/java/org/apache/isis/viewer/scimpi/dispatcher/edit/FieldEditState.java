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

package org.apache.isis.viewer.scimpi.dispatcher.edit;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;

public class FieldEditState {
    private final String entry;
    private String reason;
    private ObjectAdapter value;

    public FieldEditState(final String entry) {
        this.entry = entry;
    }

    public void setError(final String reason) {
        this.reason = reason;
    }

    public boolean isEntryValid() {
        return reason == null;
    }

    public String getEntry() {
        return entry;
    }

    public String getError() {
        return reason;
    }

    public ObjectAdapter getValue() {
        return value;
    }

    public void setValue(final ObjectAdapter value) {
        this.value = value;
    }

}
