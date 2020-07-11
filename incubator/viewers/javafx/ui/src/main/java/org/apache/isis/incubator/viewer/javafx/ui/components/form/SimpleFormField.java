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
package org.apache.isis.incubator.viewer.javafx.ui.components.form;

import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.incubator.viewer.javafx.model.form.FormFieldFx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;

public class SimpleFormField<T> extends FormFieldFx<T> {

    private static final long serialVersionUID = 1L;

    public SimpleFormField(
            LabelPosition labelPosition, 
            Node uiField) {
        super(labelPosition, new Label(), uiField, new Label());
        
        ((Label)getUiLabel()).textProperty().bind(super.label);
        ((Label)getUiErrorMessage()).textProperty().bind(super.errorMessage);
    }

    private final ObjectProperty<T> valueProperty = new SimpleObjectProperty<T>();

    @Override
    public T generateModelValue() {
        // return the current internal (model) value
        return valueProperty.get();
    }

    @Override
    public void setPresentationValue(T newPresentationValue) {
        // update the internal (model) value and UI
        valueProperty.set(newPresentationValue);
        //TODO updateVisuals();
    }
    

}
