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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class FormFieldFx<T> implements FormField<T>, HasValidation {

    private static final long serialVersionUID = 1L;

    @Getter(onMethod_ = {@Override})
    protected final LabelPosition labelPosition;

    protected final BooleanProperty invalid = new SimpleBooleanProperty();
    protected final StringProperty errorMessage = new SimpleStringProperty();
    protected final StringProperty label = new SimpleStringProperty();

    @Override
    public String getErrorMessage() {
        return errorMessage.get();
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        this.errorMessage.set(errorMessage);
    }

    @Override
    public boolean isInvalid() {
        return invalid.get();
    }

    @Override
    public void setInvalid(boolean invalid) {
        this.invalid.set(invalid);
    }

    @Override
    public String getLabel() {
        return label.get();
    }

    @Override
    public void setLabel(String label) {
        this.label.set(label);
    }

    // -- JAVA FX SPECIFIC

    @Getter
    protected final Node uiLabel;

    @Getter
    protected final Node uiField;

    @Getter
    protected final Node uiErrorMessage;

    protected Pane uiFieldContainer;
    
    /**
     * so decorators can add customizations 
     */
    public Pane getUiFieldContainer() {
        if(uiFieldContainer==null) {
            uiFieldContainer = new VBox();
            uiFieldContainer.getChildren().add(uiField);
        }
        return uiFieldContainer;
    }

    


}
