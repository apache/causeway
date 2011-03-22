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


package org.apache.isis.viewer.scimpi.dispatcher.view.field;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.viewer.scimpi.dispatcher.BlockContent;
import org.apache.isis.viewer.scimpi.dispatcher.view.form.InputField;

import com.google.common.collect.Lists;


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

    public List<ObjectAssociation> includedFields(List<ObjectAssociation> originalFields) {
        List<ObjectAssociation> includedFields = Lists.newArrayList();
        for (int i = 0; i < originalFields.size(); i++) {
            String id2 = originalFields.get(i).getId();
            if (includes(id2)) {
                includedFields.add(originalFields.get(i));
            }
        }

        return includedFields;
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

    public List<ObjectAction> includedActions(List<ObjectAction> originalActions) {
        List<ObjectAction> includedActions = Lists.newArrayList();
        for (int i = 0; i < originalActions.size(); i++) {
            String id2 = originalActions.get(i).getId();
            if (includes(id2)) {
                includedActions.add(originalActions.get(i));
            }
        }

        return includedActions;
    }
}

