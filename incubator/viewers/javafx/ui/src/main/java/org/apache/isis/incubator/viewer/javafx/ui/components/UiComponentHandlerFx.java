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
package org.apache.isis.incubator.viewer.javafx.ui.components;

import org.apache.isis.viewer.common.model.binding.BindingConverter;
import org.apache.isis.viewer.common.model.components.UiComponentFactory;

import javafx.scene.Node;
import javafx.util.StringConverter;
import lombok.NonNull;

public interface UiComponentHandlerFx 
extends UiComponentFactory.Handler<Node> {

    /**
     * Converts given {@code bindingConverter} to {@link StringConverter} from JavaFX Universe 
     * @param <T>
     * @param bindingConverter
     * @return
     */
    default <T> StringConverter<T> toJavaFxStringConverter(
            final @NonNull BindingConverter<T> bindingConverter) {

        return new StringConverter<T>() {

            @Override
            public String toString(T value) {
                return bindingConverter.toString(value);
            }

            @Override
            public T fromString(String stringifiedValue) {
                return bindingConverter.fromString(stringifiedValue);
            }
            
        };
    }
    
}
