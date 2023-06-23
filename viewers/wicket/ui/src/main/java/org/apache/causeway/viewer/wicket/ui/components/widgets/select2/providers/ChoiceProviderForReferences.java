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
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.commons.model.pop.UiParameter;
import org.apache.causeway.viewer.commons.model.pop.UiPop;
import org.apache.causeway.viewer.commons.model.pop.UiPop.ChoiceProviderSort;
import org.apache.causeway.viewer.wicket.model.models.PopModel;

import lombok.val;

public class ChoiceProviderForReferences
extends ChoiceProviderAbstractForPopModel {

    private static final long serialVersionUID = 1L;

    private final UiPop.ChoiceProviderSort choiceProviderSort;

    public ChoiceProviderForReferences(
            final PopModel popModel) {
        super(popModel);
        this.choiceProviderSort = ChoiceProviderSort.valueOf(popModel);
    }

    @Override
    protected Can<ObjectMemento> query(final String term) {
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

    // -- HELPER

    private Can<ObjectMemento> queryAll() {
        return popModel().getChoices() // must not return detached entities
                .map(ManagedObject::getMementoElseFail);
    }

    private Can<ObjectMemento> queryWithAutoCompleteUsingObjectSpecification(final String term) {
        val autoCompleteAdapters = Facets
                .autoCompleteExecute(popModel().getScalarTypeSpec(), term);
        return autoCompleteAdapters
                .map(ManagedObject::getMementoElseFail);
    }

    private Can<ObjectMemento> queryWithAutoComplete(final String term) {
        val popModel = popModel();
        val pendingArgs = popModel.isParameter()
                ? ((UiParameter)popModel).getParameterNegotiationModel().getParamValues()
                : Can.<ManagedObject>empty();
        val pendingArgMementos = pendingArgs
                .map(ManagedObject::getMementoElseFail);

        if(popModel.isParameter()) {
            // recover any pendingArgs
            val paramModel = (UiParameter)popModel;

            paramModel
                .getParameterNegotiationModel()
                .setParamValues(
                        reconstructPendingArgs(paramModel, pendingArgMementos));
        }

        return popModel
                .getAutoComplete(term)
                .map(ManagedObject::getMementoElseFail);
    }

    private Can<ManagedObject> reconstructPendingArgs(
            final UiParameter parameterModel,
            final Can<ObjectMemento> pendingArgMementos) {

        val pendingArgsList = _NullSafe.stream(pendingArgMementos)
            .map(getObjectManager()::demementify)
            .collect(Can.toCan());

       return pendingArgsList;
    }


}
