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
import org.apache.causeway.viewer.commons.model.scalar.UiParameter;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;

public class ChoiceProviderDefault
extends ChoiceProviderAbstractForScalarModel {

    private static final long serialVersionUID = 1L;

    public ChoiceProviderDefault(
            final UiAttributeWkt attributeModel) {
        super(attributeModel);
    }

    @Override
    protected Can<ObjectMemento> queryAll() {
        return attributeModel().getChoices() // must not return detached entities
                .map(ManagedObject::getMementoElseFail);
    }

    @Override
    protected Can<ObjectMemento> queryWithAutoCompleteUsingObjectSpecification(final String term) {
        var autoCompleteAdapters = Facets
                .autoCompleteExecute(attributeModel().getElementType(), term);
        return autoCompleteAdapters
                .map(ManagedObject::getMementoElseFail);
    }

    @Override
    protected Can<ObjectMemento> queryWithAutoComplete(final String term) {
        var attributeModel = attributeModel();
        var pendingArgs = attributeModel.isParameter()
                ? ((UiParameter)attributeModel).getParameterNegotiationModel().getParamValues()
                : Can.<ManagedObject>empty();
        var pendingArgMementos = pendingArgs
                .map(ManagedObject::getMementoElseFail);

        if(attributeModel.isParameter()) {
            // recover any pendingArgs
            var paramModel = (UiParameter)attributeModel;

            paramModel
                .getParameterNegotiationModel()
                .setParamValues(
                        reconstructPendingArgs(paramModel, pendingArgMementos));
        }

        return attributeModel
                .getAutoComplete(term)
                .map(ManagedObject::getMementoElseFail);
    }

    // -- HELPER

    private Can<ManagedObject> reconstructPendingArgs(
            final UiParameter parameterModel,
            final Can<ObjectMemento> pendingArgMementos) {

        var pendingArgsList = _NullSafe.stream(pendingArgMementos)
            .map(getObjectManager()::demementify)
            .collect(Can.toCan());

       return pendingArgsList;
    }

}
