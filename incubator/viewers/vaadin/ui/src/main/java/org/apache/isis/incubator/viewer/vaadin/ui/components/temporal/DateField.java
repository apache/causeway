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
package org.apache.isis.incubator.viewer.vaadin.ui.components.temporal;

import java.time.LocalDate;

import javax.annotation.Nullable;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;

public class DateField extends CustomField<LocalDate> {

    private static final long serialVersionUID = 1L;
    
    private final DatePicker datePicker = new DatePicker();
    private boolean isNull;
    
    public DateField(String label) {
        super();
        setLabel(label);
        add(datePicker);
    }
    
    @Override
    protected LocalDate generateModelValue() {
        return datePicker.getValue();
    }

    @Override
    protected void setPresentationValue(@Nullable LocalDate newValue) {
        
        this.isNull = newValue==null;
        datePicker.setValue(newValue);
        
        if(this.isNull) {
            datePicker.setPlaceholder("no date"); //TODO allow translation
        }
        
    }
   
    
}
