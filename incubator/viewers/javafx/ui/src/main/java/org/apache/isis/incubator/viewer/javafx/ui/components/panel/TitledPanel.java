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
package org.apache.isis.incubator.viewer.javafx.ui.components.panel;

import org.apache.isis.incubator.viewer.javafx.model.util._fx;

import lombok.Getter;

import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

@Getter
public class TitledPanel extends VBox {

    private final Label uiLabel = new Label();
    private final HBox uiActionBar = new HBox();
    
    public TitledPanel(String label) {
        super();
        _fx.add(this, uiActionBar);
        _fx.add(uiActionBar, uiLabel);
        uiLabel.setText(label);
        _fx.h3(uiLabel);
        _fx.toolbarLayout(uiActionBar);
        
        setBorder(new Border(new BorderStroke(
                Color.ORANGE, 
                BorderStrokeStyle.DASHED, 
                new CornerRadii(3), 
                new BorderWidths(1))));
        
    }
    
}
