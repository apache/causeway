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

import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.incubator.viewer.javafx.model.util._fx;

import lombok.Getter;

import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class TitledPanel extends VBox {

    @Getter private final Label uiLabel;
    private FlowPane uiActionBar;
    
    public TitledPanel(String label) {
        super();
        if(!_Strings.isEmpty(label)) {
            uiLabel = _fx.newLabel(getUiActionBar(), label);
            _fx.h3(uiLabel);    
        } else {
            uiLabel = null;
        }
        super.setFillWidth(true);
        
        _fx.backround(this, Color.ALICEBLUE);
    }
    
    public FlowPane getUiActionBar() {
        if(uiActionBar==null) {
            uiActionBar = _fx.newFlowPane(this);
            _fx.toolbarLayout(uiActionBar);
        }
        return uiActionBar;
    }
    
}
