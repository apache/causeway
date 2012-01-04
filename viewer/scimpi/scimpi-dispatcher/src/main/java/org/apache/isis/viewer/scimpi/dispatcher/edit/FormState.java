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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FormState {
    private final Map<String, FieldEditState> fields = new HashMap<String, FieldEditState>();
    private String error;
    private String formId;

    public FieldEditState createField(final String name, final String entry) {
        final FieldEditState fieldEditState = new FieldEditState(entry);
        fields.put(name, fieldEditState);
        return fieldEditState;
    }

    public boolean isValid() {
        final Iterator<FieldEditState> iterator = fields.values().iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().isEntryValid()) {
                return false;
            }
        }
        return error == null;
    }

    public FieldEditState getField(final String name) {
        return fields.get(name);
    }

    public void setError(final String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setForm(final String formId) {
        this.formId = formId;
    }

    public boolean isForForm(final String formId) {
        return this.formId == null || this.formId.equals(formId);
    }

}
