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
package org.apache.isis.incubator.viewer.javafx.ui.components.debug;

import java.util.concurrent.atomic.LongAdder;

import org.apache.isis.incubator.viewer.javafx.model.util._fx;
import org.apache.isis.incubator.viewer.javafx.ui.components.field.CustomFieldFx;
import org.apache.isis.viewer.common.model.debug.DebugUiModel;

import lombok.val;

import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;

public class DebugField extends CustomFieldFx<DebugUiModel> {

    private DebugUiModel model;
    private final GridPane detailGrid;
    private final TitledPane detailPane;

    public DebugField(String label) {
        super();
        setLabel(label);
        val accordion = add(new Accordion());
        detailPane = _fx.newTitledPane(accordion, "Debug");
        detailGrid = _fx.formLayout(_fx.newGrid(detailPane));
    }


    @Override
    protected DebugUiModel generateModelValue() {
        return model;
    }

    @Override
    protected void setPresentationValue(DebugUiModel model) {
        this.model = model;
        detailPane.setText(model.getSummaryText());
        
        val rowIndex = new LongAdder();
        
        model.getKeyValuePairs().forEach((k, v)->{
            
            val row = rowIndex.intValue();
            
            _fx.addGridCell(detailGrid, new Label(k), 0, row);
            _fx.addGridCell(detailGrid, new TextArea(v), 1, row);
            
            rowIndex.increment();
        });
        
        
    }

    
}
