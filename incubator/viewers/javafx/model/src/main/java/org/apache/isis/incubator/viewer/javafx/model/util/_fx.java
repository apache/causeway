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
package org.apache.isis.incubator.viewer.javafx.model.util;

import lombok.val;
import lombok.experimental.UtilityClass;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

@UtilityClass
public class _fx {

    // -- OBSERVABLES
    
    public static ObservableValue<String> newStringReadonly(String value) {
        val string = new ReadOnlyStringWrapper(value);
        return string;
    }
    
    // -- COMPONENT FACTORIES
    
    public static Label newLabel(Pane container, String label) {
        val component = new Label(label);
        container.getChildren().add(component);
        return component;
    }
    
    public static HBox newHBox(Pane container) {
        val component = new HBox();
        container.getChildren().add(component);
        return component;
    }
    
    public static VBox newVBox(Pane container) {
        val component = new VBox();
        container.getChildren().add(component);
        return component;
    }
    
    /**
     * @param <S> The type of the TableView generic type (i.e. S == TableView&lt;S&gt;)
     * @param <T> The type of the content in all cells in this TableColumn.
     * @param tableView
     * @param columnLabel
     * @param columnType
     * @return a new column as added to the {@code tableView}
     */
    public static <S, T> TableColumn<S, T> newColumn(TableView<S> tableView, String columnLabel, Class<T> columnType) {
        val column = new TableColumn<S, T>(columnLabel);
        tableView.getColumns().add(column);
        return column;
    }
    
    
    
    

}
