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
package org.apache.isis.viewer.wicket.ui.components.widgets.select2.providers;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.metamodel.object.ManagedObject;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.isis.core.metamodel.util.Facets;
import org.apache.isis.viewer.commons.model.feature.ParameterUiModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;

import lombok.val;

public class ChoiceProviderForReferences
extends ChoiceProviderAbstractForScalarModel {

    private static final long serialVersionUID = 1L;

    static enum ChoiceProviderSort {
        NO_CHOICES,
        CHOICES,
        AUTO_COMPLETE,
        OBJECT_AUTO_COMPLETE;
        static ChoiceProviderSort valueOf(final ScalarModel scalarModel) {
            if (scalarModel.hasChoices()) {
                return ChoiceProviderSort.CHOICES;
            } else if(scalarModel.hasAutoComplete()) {
                return ChoiceProviderSort.AUTO_COMPLETE;
            } else {
                return ChoiceProviderSort.OBJECT_AUTO_COMPLETE;
            }
        }
    }

    private final ChoiceProviderSort choiceProviderSort;

    public ChoiceProviderForReferences(
            final ScalarModel scalarModel) {
        super(scalarModel);
        this.choiceProviderSort = ChoiceProviderSort.valueOf(scalarModel);
    }

    @Override
    protected Can<ObjectMemento> query(final String term) {
        switch(choiceProviderSort) {
        case CHOICES:
            return super.filter(term, queryAll());
        case AUTO_COMPLETE:
            return queryWithAutoComplete(term);
        case OBJECT_AUTO_COMPLETE:
            // fall through
        }
        val scalarTypeSpec = scalarModel().getScalarTypeSpec();
        val autoCompleteAdapters = Facets.autoCompleteExecute(scalarTypeSpec, term);
        return autoCompleteAdapters.map(getCommonContext()::mementoForSingle);
    }

    // -- HELPER

    private Can<ObjectMemento> queryAll() {
        return scalarModel().getChoices() // must not return detached entities
                .map(getCommonContext()::mementoForAnyCardinality);
    }

    private Can<ObjectMemento> queryWithAutoComplete(final String term) {
        val commonContext = getCommonContext();
        val scalarModel = scalarModel();
        val pendingArgs = scalarModel.isParameter()
                ? ((ParameterUiModel)scalarModel).getParameterNegotiationModel().getParamValues()
                : Can.<ManagedObject>empty();
        val pendingArgMementos = pendingArgs
                .map(commonContext::mementoForAnyCardinality);

        if(scalarModel.isParameter()) {
            // recover any pendingArgs
            val paramModel = (ParameterUiModel)scalarModel;

            paramModel
                .getParameterNegotiationModel()
                .setParamValues(
                        reconstructPendingArgs(paramModel, pendingArgMementos));
        }

        return scalarModel
                .getAutoComplete(term)
                .map(commonContext::mementoForSingle);
    }

    private Can<ManagedObject> reconstructPendingArgs(
            final ParameterUiModel parameterModel,
            final Can<ObjectMemento> pendingArgMementos) {

        val commonContext = super.getCommonContext();
        val pendingArgsList = _NullSafe.stream(pendingArgMementos)
            .map(commonContext::reconstructObject)
            .map(ManagedObject.class::cast)
            .collect(Can.toCan());

       return pendingArgsList;
    }


}
