/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.layoutmetadata;

import java.util.List;
import java.util.Map;

public class LayoutMetadata {
    private List<ColumnRepr> columns;
    
    private Map<String,ActionRepr> actions;
    
    public List<ColumnRepr> getColumns() {
        return columns;
    }
    public void setColumns(List<ColumnRepr> columns) {
        this.columns = columns;
        
    }
    public Map<String,ActionRepr> getActions() {
        return actions;
    }
    public void setActions(Map<String,ActionRepr> actions) {
        this.actions = actions;
    }
}