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

import com.google.common.collect.Lists;

import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.viewer.scimpi.dispatcher.BlockContent;
import org.apache.isis.viewer.scimpi.dispatcher.view.form.InputField;

public class InclusionList implements BlockContent {
    private final Set<String> includedList = new HashSet<String>();
    private final Set<String> excludedList = new HashSet<String>();

    private boolean inIncludedList(final String fieldName) {
        return includedList.size() == 0 || includedList.contains(fieldName) || includedList.contains("all");
    }

    private boolean inExcludedList(final String fieldName) {
        return excludedList.contains(fieldName) || excludedList.contains("all");
    }

    public void include(final String field) {
        includedList.add(field);
    }

    public void exclude(final String field) {
        excludedList.add(field);
    }

    public List<ObjectAssociation> includedFields(final List<ObjectAssociation> originalFields) {
        final List<ObjectAssociation> includedFields = Lists.newArrayList();
        for (int i = 0; i < originalFields.size(); i++) {
            final String id2 = originalFields.get(i).getId();
            if (includes(id2)) {
                includedFields.add(originalFields.get(i));
            }
        }

        return includedFields;
    }

    public void hideExcludedParameters(final InputField[] inputFields) {
        for (final InputField inputField : inputFields) {
            final String id2 = inputField.getName();
            if (!includes(id2)) {
                inputField.setHidden(true);
            }
        }
    }

    public boolean includes(final String id) {
        return inIncludedList(id) && !inExcludedList(id);
    }

    public List<ObjectAction> includedActions(final List<ObjectAction> originalActions) {
        final List<ObjectAction> includedActions = Lists.newArrayList();
        for (int i = 0; i < originalActions.size(); i++) {
            final String id2 = originalActions.get(i).getId();
            if (includes(id2)) {
                includedActions.add(originalActions.get(i));
            }
        }

        return includedActions;
    }
}
