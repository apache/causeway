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
package org.apache.causeway.viewer.wicket.ui.components.widgets.select2.providers;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.causeway.viewer.commons.model.scalar.UiScalar;
import org.apache.causeway.viewer.commons.model.scalar.UiScalar.ChoiceProviderSort;
import org.apache.causeway.viewer.wicket.model.models.ScalarModel;

import lombok.Getter;
import lombok.experimental.Accessors;

public abstract class ChoiceProviderAbstractForScalarModel
extends ChoiceProviderAbstract {

    private static final long serialVersionUID = 1L;

    @Getter @Accessors(fluent = true)
    private final ScalarModel scalarModel;
    private final UiScalar.ChoiceProviderSort choiceProviderSort;

    protected ChoiceProviderAbstractForScalarModel(final ScalarModel scalarModel) {
        super();
        this.scalarModel = scalarModel;
        this.choiceProviderSort = ChoiceProviderSort.valueOf(scalarModel);
    }

    @Override
    protected final boolean isRequired() {
        return scalarModel().isRequired();
    }

    @Override
    protected final Can<ObjectMemento> query(final String term) {
        switch(choiceProviderSort) {
        case CHOICES:
            return super.filter(term, queryAll());
        case AUTO_COMPLETE:
            return queryWithAutoComplete(term);
        case OBJECT_AUTO_COMPLETE:
            return queryWithAutoCompleteUsingObjectSpecification(term);
        case NO_CHOICES:
        default:
            // fall through
        }
        return Can.empty();
    }

    protected abstract Can<ObjectMemento> queryAll();
    protected abstract Can<ObjectMemento> queryWithAutoComplete(String term);
    protected abstract Can<ObjectMemento> queryWithAutoCompleteUsingObjectSpecification(String term);

}
