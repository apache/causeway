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

import org.checkerframework.checker.nullness.qual.Nullable;

import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.incubator.viewer.javafx.model.util._fx;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;

public class FormPane extends GridPane {

    @RequiredArgsConstructor(staticName = "of")
    private static final class FieldAssembly {
        @Getter private final @NonNull FormPane parent;
        @Getter private final @NonNull LabelPosition labelPosition;
        @Getter private final @NonNull Node fieldComponent;
        private FlowPane associatedActionBar;
        public FlowPane getAssociatedActionBar() {
            if(associatedActionBar==null) {
                associatedActionBar = new FlowPane();
                _fx.toolbarLayoutPropertyAssociated(associatedActionBar);
                switch(labelPosition) {
                case NONE:
                case TOP:
                case RIGHT:
                    getParent().addRow(associatedActionBar);
                    break;
                case LEFT:
                case DEFAULT:
                default:
                    getParent().addRow(null, associatedActionBar);
                    break;
                }
            }
            return associatedActionBar;
        }
    }

    private final List<FieldAssembly> fields = _Lists.newArrayList();
    private int rowCount = 0;

    public FormPane() {
        super();
        val grid = grid();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
    }

    public FormPane addField(LabelPosition labelPosition, Node uiFormLabel, Node uiFormField) {

        fields.add(FieldAssembly.of(this, labelPosition, uiFormField));

        switch(labelPosition) {
        case NONE:
            addRow(uiFormField);
            break;
        case TOP:
            addRow(uiFormLabel);
            addRow(uiFormField);
            break;
        case RIGHT:
            addRow(uiFormField, uiFormLabel);
            break;
        case LEFT:
        case DEFAULT:
        default:
            addRow(uiFormLabel, uiFormField);
            break;
        }
        return this;
    }

    public FormPane addActionLink(Node uiButton) {
        val actionBarAssociatedWithField = _Lists.lastElementIfAny(fields).getAssociatedActionBar();
        _fx.add(actionBarAssociatedWithField, uiButton);
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

    private void addRow(@Nullable Node left, @NonNull Node right) {
        if(left!=null) {
            grid().add(left, 0, rowCount);
        }
        grid().add(right, 1, rowCount);
        ++rowCount;
    }






}
