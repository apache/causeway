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
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.common.model.feature.ParameterUiModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;

import lombok.val;

public class ObjectAdapterMementoProviderForReferenceParamOrPropertyAutoComplete
extends ObjectAdapterMementoProviderAbstract {

    private static final long serialVersionUID = 1L;

    public ObjectAdapterMementoProviderForReferenceParamOrPropertyAutoComplete(final ScalarModel scalarModel) {
        super(scalarModel);
        if(!scalarModel.hasAutoComplete()) {
            throw _Exceptions.illegalArgument("Cannot create auto-complete provider, "
                    + "when model has no auto-complete %s", scalarModel);
        }
    }

    @Override
    protected Can<ObjectMemento> obtainMementos(final String term) {

        val commonContext = getCommonContext();
        val scalarModel = getScalarModel();
        val pendingArgs = scalarModel.isParameter()
                ? ((ParameterUiModel)scalarModel).getParameterNegotiationModel().getParamValues()
                : Can.<ManagedObject>empty();
        val pendingArgMementos = pendingArgs
                .map(commonContext::mementoForParameter);

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
                .map(commonContext::mementoFor);
    }

    // -- HELPER

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
