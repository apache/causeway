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
package org.apache.isis.incubator.viewer.javafx.ui.components.form.field;

import java.util.Objects;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public abstract class CustomFieldFx<T> extends Pane {

    protected final Label label = new Label();
    
    private final ObjectProperty<T> value = new SimpleObjectProperty<T>();
    
    public void setLabel(String label) {
        this.label.setText(label);
        getChildren().add(this.label);
    }

    
//    /**
//     * Sets the value of this object. If the new value is not equal to
//     * {@code getValue()}, fires a value change event. May throw
//     * {@code IllegalArgumentException} if the value is not acceptable.
//     * <p>
//     * <i>Implementation note:</i> the implementing class should document
//     * whether null values are accepted or not, and override
//     * {@link #getEmptyValue()} if the empty value is not {@code null}.
//     *
//     * @param newValue
//     *            the new value
//     * @throws IllegalArgumentException
//     *             if the value is invalid
//     */
    public void setValue(T newValue) {
        if(Objects.equals(value.getValue(), newValue)) {
            return; // ignore
        }
        value.setValue(newValue);
        setPresentationValue(newValue);
    }
    
    protected <N extends Node> N add(N node) {
        getChildren().add(node);
        return node;
    }
    
    /**
     * This method should return the value of the field, based on value of the internal fields.
     *
     * @return new value of the field.
     */
    protected abstract T generateModelValue();

    
    /**
     * This method should be implemented to set the value of the fields contained
     * in this custom field according to the value of the parameter.
     * It can also be use to show the value to the user in some way, 
     * like placing it in an element contained on the field.
     *
     * @param value - the new presentation value.
     */
    protected abstract void setPresentationValue(T value);
    
    

}
