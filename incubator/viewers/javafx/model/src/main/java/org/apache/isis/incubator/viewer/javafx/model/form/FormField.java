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
package org.apache.isis.incubator.viewer.javafx.model.form;

import org.apache.isis.applib.annotation.LabelPosition;

/**
 * 
 * Heavily inspired by com.vaadin.flow.component.customfield.CustomField
 *
 * @param <T>
 */
public interface FormField<T> {

    LabelPosition getLabelPosition();
    
    /**
     * This method should return the value of the field, based on value of the internal fields.
     *
     * @return new value of the field.
     */
    T generateModelValue();
    
    /**
     * This method should be implemented to set the value of the fields contained
     * in this custom field according to the value of the parameter.
     * It can also be use to show the value to the user in some way, 
     * like placing it in an element contained on the field.
     *
     * @param newPresentationValue The new presentation value.
     */
    void setPresentationValue(T value);
    
    /**
     * Gets the label for the field.
     */
    String getLabel();

    /**
     * Sets the label for the field.
     */
    void setLabel(String label);

}
