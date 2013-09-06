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

package org.apache.isis.viewer.scimpi.dispatcher.view.form;

import org.apache.isis.core.commons.util.ToString;

public class InputField {
    public static final int REFERENCE = 1;
    public static final int TEXT = 2;
    public static final int PASSWORD = 3;
    public static final int CHECKBOX = 4;
    public static final int HTML = 5;

    private int type;

    private String label;
    private String description = "";
    private String helpReference;
    private String errorText;
    private final String name;
    private String dataType;

    private int maxLength = 0;
    private int width;
    private int height = 1;
    private boolean isWrapped = false;

    private boolean isRequired = true;
    private boolean isEditable = true;
    private boolean isHidden = false;

    private String[] optionsText;
    private String[] optionValues;

    private String value;
    private String html;

    public InputField(final String name) {
        this.name = name;
    }

    public String getErrorText() {
        return errorText;
    }

    public String getDescription() {
        return description;
    }

    public String getDataType() {
        return dataType;
    }
    
    public String getHelpReference() {
        return helpReference;
    }

    public String getLabel() {
        return label;
    }

    public String getName() {
        return name;
    }

    public boolean isEditable() {
        return isEditable;
    }

    public String getValue() {
        return value;
    }

    public String getHtml() {
        return html;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public String[] getOptionsText() {
        return optionsText;
    }

    public String[] getOptionValues() {
        return optionValues;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isWrapped() {
        return isWrapped;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public int getType() {
        return type;
    }

    public void setErrorText(final String errorText) {
        this.errorText = errorText;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setHelpReference(final String helpReference) {
        this.helpReference = helpReference;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public void setEditable(final boolean isEditable) {
        this.isEditable = isEditable;
        isRequired = isRequired && isEditable;
    }

    public void setValue(final String entryText) {
        this.value = entryText;
    }

    public void setHtml(final String html) {
        this.html = html;
    }

    public void setHidden(final boolean isHidden) {
        this.isHidden = isHidden;
    }

    public void setWidth(final int width) {
        this.width = width;
    }

    public void setOptions(final String[] optionsText, final String[] optionValues) {
        this.optionsText = optionsText;
        this.optionValues = optionValues;
    }

    public void setHeight(final int height) {
        this.height = height;
    }

    public void setWrapped(final boolean isWrapped) {
        this.isWrapped = isWrapped;
    }

    public void setRequired(final boolean isRequired) {
        this.isRequired = isRequired;
    }

    public void setMaxLength(final int maxLength) {
        this.maxLength = maxLength;
    }

    public void setType(final int type) {
        this.type = type;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
    
    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append("name", name);
        String typeName;
        switch (type) {
        case CHECKBOX:
            typeName = "checkbox";
            break;
        case REFERENCE:
            typeName = "reference";
            break;
        case TEXT:
            typeName = "text";
            break;
        default:
            typeName = "unset";
            break;
        }
        str.append("type", typeName);
        str.append("datatype", dataType);
        str.append("editable", isEditable);
        str.append("hidden", isHidden);
        str.append("required", isRequired);
        return str.toString();
    }

}
