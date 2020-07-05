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

import java.util.List;
import java.util.Optional;

import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.core.commons.internal.collections._Lists;

import lombok.NonNull;
import lombok.val;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;

public class FormPane extends GridPane {

    private final List<FormField> fields = _Lists.newArrayList();
    private int rowCount = 0;
    
    public FormPane() {
        super();
        val grid = grid();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
    }

    public FormPane addField(FormField formField) {
        
        fields.add(formField);
        
        val labelPosition = Optional.ofNullable(formField.getLabelPosition())
                    .orElse(LabelPosition.DEFAULT);
        switch(labelPosition) {
        case NONE:
            addRow(formField.getUiField());
            break;
        case TOP:
            addRow(formField.getUiLabel());
            addRow(formField.getUiField());
            break;
        case RIGHT:
            addRow(formField.getUiField(), formField.getUiLabel());
            break;
        case LEFT:
        case DEFAULT:
        default:
            addRow(formField.getUiLabel(), formField.getUiField());
            break;
        }
        return this;
    }

    
    // -- HELPER
    
    private GridPane grid() {
        return this;
    }
    
    private void addRow(@NonNull Node spanningNode) {
        grid().add(spanningNode, 0, rowCount, 2, 1);
        ++rowCount;
    }
    
    private void addRow(@NonNull Node left, @NonNull Node right) {
        grid().add(left, 0, rowCount);
        grid().add(right, 1, rowCount);
        ++rowCount;
    }

    
    

}
