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
package org.apache.isis.commons.internal.debug.xray;

import java.util.Map;
import java.util.TreeMap;

import javax.swing.JPanel;

import org.apache.isis.commons.internal.base._Refs;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

public abstract class XrayDataModel {

    public abstract void render(JPanel panel);
    public abstract String getLabel();
    
    @Override
    public String toString() {
        return getLabel();
    }

    // -- PREDEFINED DATA MODELS
    
    @Getter
    @EqualsAndHashCode(callSuper = false)  
    @RequiredArgsConstructor
    public static class KeyValue extends XrayDataModel {
        
        private final Map<String, String> data = new TreeMap<>();
        private final String label;
        
        @Override
        public void render(JPanel panel) {
            String[] columnNames = {"Key", "Value"};
            Object[][] tableData = new Object[data.size()+1][columnNames.length];
            
            val rowIndex = _Refs.intRef(0);
            
            // header
            {
                val headRow = tableData[rowIndex.getValue()];
                for(int i=0; i<columnNames.length; ++i) {
                    headRow[i] = String.format("%S", columnNames[i]);
                }
                rowIndex.inc();
            }
            
            data.forEach((k, v)->{
                val row = tableData[rowIndex.getValue()];
                rowIndex.inc();
                row[0] = k;
                row[1] = v;
            });
                
            val table = _SwingUtil.newTable(tableData, columnNames); 
            
            panel.add(table);
            table.setFillsViewportHeight(true);
        }
    }
    
    
}
