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

package org.apache.isis.viewer.html.component;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public interface Form extends Component {

    void addField(ObjectSpecification type, String fieldLabel, String fieldDescription, String fieldId, String currentEntryTitle, int noLines, boolean wrap, int maxLength, int typicalLength, boolean required, String error);

    /*
     * REVIEW the form should be asked to create specific types, like see
     * HTMLForm.addForm()
     * 
     * void addCheckBox(....)
     * 
     * void addPasswordField(....)
     * 
     * void addMultilineField(....)
     */

    void addLookup(String fieldLabel, String fieldDescription, String fieldId, int selectedIndex, String[] options, String[] ids, boolean required, String errorMessage);

    void addReadOnlyField(String fieldLabel, String title, String fieldDescription);

    void addReadOnlyCheckbox(String fieldLabel, boolean isSet, String fieldDescription);
}
