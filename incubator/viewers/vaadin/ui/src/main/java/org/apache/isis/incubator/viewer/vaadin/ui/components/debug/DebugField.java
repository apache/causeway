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
package org.apache.isis.incubator.viewer.vaadin.ui.components.debug;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextArea;

import org.apache.isis.viewer.common.model.debug.DebugUiModel;

import lombok.val;

public class DebugField extends CustomField<DebugUiModel> {

    private static final long serialVersionUID = 1L;
    
    private final Details details = new Details();
    private DebugUiModel model;

    public DebugField(String label) {
        super();
        setLabel(label);
        add(details);

        val formLayout = new FormLayout();
        details.addContent(formLayout);
        details.setSummaryText("Debug");
        details.addThemeVariants(DetailsVariant.SMALL);
    }

    @Override
    protected DebugUiModel generateModelValue() {
        return model;
    }

    @Override
    protected void setPresentationValue(DebugUiModel model) {
        this.model = model;
        
        details.setSummaryText(model.getSummaryText());

        details.getContent().findFirst()
        .map(FormLayout.class::cast)
        .ifPresent(formLayout->{
        
            formLayout.removeAll();
            
            model.getKeyValuePairs().forEach((k, v)->{
                val textArea = new TextArea();
                textArea.setLabel(k);
                textArea.setValue(v);
                textArea.setInvalid(true);
                formLayout.add(textArea);
            });
            
        });
        
    }

    
}
