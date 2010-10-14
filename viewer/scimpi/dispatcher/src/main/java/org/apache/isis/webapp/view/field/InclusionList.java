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


package org.apache.isis.webapp.view.field;

import java.util.HashSet;
import java.util.Set;

import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.webapp.BlockContent;
import org.apache.isis.webapp.view.form.InputField;


public class InclusionList implements BlockContent {
    private Set<String> includedList = new HashSet<String>();
    private Set<String> excludedList = new HashSet<String>();

    private boolean inIncludedList(String fieldName) {
        return includedList.size() == 0 || includedList.contains(fieldName) || includedList.contains("all");
    }

    private boolean inExcludedList(String fieldName) {
        return excludedList.contains(fieldName) || excludedList.contains("all");
    }

    public void include(String field) {
        includedList.add(field);
    }

    public void exclude(String field) {
        excludedList.add(field);
    }

    public ObjectAssociation[] includedFields(ObjectAssociation[] originalFields) {
        ObjectAssociation[] includedFields = new ObjectAssociation[originalFields.length];
        int j = 0;
        for (int i = 0; i < originalFields.length; i++) {
            String id2 = originalFields[i].getId();
            if (includes(id2)) {
                includedFields[j++] = originalFields[i];
            }
        }

        ObjectAssociation[] fields = new ObjectAssociation[j];
        System.arraycopy(includedFields, 0, fields, 0, j);
        return fields;
    }

    public void hideExcludedParameters(InputField[] inputFields) {
        for (int i = 0; i < inputFields.length; i++) {
            String id2 = inputFields[i].getName();
            if (!includes(id2)) {
                inputFields[i].setHidden(true);
            }
        }
    }

    public boolean includes(String id) {
        return inIncludedList(id) && !inExcludedList(id);
    }

    public ObjectAction[] includedActions(ObjectAction[] originalActions) {
        ObjectAction[] includedActions = new ObjectAction[originalActions.length];
        int j = 0;
        for (int i = 0; i < originalActions.length; i++) {
            String id2 = originalActions[i].getId();
            if (includes(id2)) {
                includedActions[j++] = originalActions[i];
            }
        }

        ObjectAction[] fields = new ObjectAction[j];
        System.arraycopy(includedActions, 0, fields, 0, j);
        return fields;
    }
}

