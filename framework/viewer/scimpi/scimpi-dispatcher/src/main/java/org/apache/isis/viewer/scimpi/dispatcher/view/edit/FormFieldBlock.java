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

package org.apache.isis.viewer.scimpi.dispatcher.view.edit;

import java.util.HashMap;
import java.util.Map;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.scimpi.dispatcher.view.field.InclusionList;
import org.apache.isis.viewer.scimpi.dispatcher.view.form.InputField;

public class FormFieldBlock extends InclusionList {
    private final Map<String, String> content = new HashMap<String, String>();
    private final Map<String, String> values = new HashMap<String, String>();

    public void replaceContent(final String field, final String htmlString) {
        content.put(field, htmlString);
    }

    public boolean hasContent(final String name) {
        return content.containsKey(name);
    }

    public String getContent(final String name) {
        return content.get(name);
    }

    public boolean isVisible(final String name) {
        return true;
    }

    public boolean isNullable(final String name) {
        return true;
    }

    public ObjectAdapter getCurrent(final String name) {
        return null;
    }

    public void value(final String field, final String value) {
        values.put(field, value);
    }

    public void setUpValues(final InputField[] inputFields) {
        for (final InputField inputField : inputFields) {
            final String name = inputField.getName();
            inputField.setValue(values.get(name));
        }
    }

}
