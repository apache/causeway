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

import org.apache.isis.viewer.scimpi.dispatcher.view.field.InclusionList;
import org.apache.isis.viewer.scimpi.dispatcher.view.form.InputField;

public class EditFieldBlock extends InclusionList {
    private Map<String, String> content = new HashMap<String, String>();
    private Map<String, String> values = new HashMap<String, String>();

    public void replaceContent(String field, String htmlString) {
        content.put(field, htmlString);
    }

    public boolean hasContent(String name) {
        return content.containsKey(name);
    }

    public String getContent(String name) {
        return content.get(name);
    }

    public boolean isVisible(String name) {
        return true;
    }

    public void value(String field, String value) {
        values.put(field, value);
    }
    
    public void setUpValues(InputField[] inputFields) {
        for (int i = 0; i < inputFields.length; i++) {
            String name = inputFields[i].getName();
            inputFields[i].setValue(values.get(name));
        }
    }

    
}


