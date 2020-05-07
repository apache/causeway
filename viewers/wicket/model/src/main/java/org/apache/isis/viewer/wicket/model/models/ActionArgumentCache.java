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
package org.apache.isis.viewer.wicket.model.models;

import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.assertions._Assert;
import org.apache.isis.core.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.viewer.common.model.action.form.PendingParameterManager;
import org.apache.isis.viewer.common.model.feature.ParameterUiModel;
import org.apache.isis.viewer.wicket.model.mementos.ActionMemento;
import org.apache.isis.viewer.wicket.model.mementos.ActionParameterMemento;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
class ActionArgumentCache implements PendingParameterManager {
    
    @NonNull private final EntityModel entityModel;
    @NonNull private final ActionMemento actionMemento;
    @NonNull private final ObjectAction action;

    private final Map<Integer, ParameterUiModel> arguments = _Maps.newHashMap();
    
    public ActionArgumentCache copy() {
        val copy = new ActionArgumentCache(
                entityModel, 
                actionMemento, 
                action);
        primeArgumentModels();
        for (val argumentEntry : arguments.entrySet()) {
            putArgumentValue(argumentEntry.getKey(), argumentEntry.getValue().getValue());
        }
        return copy;
    }
    
    public Can<ManagedObject> snapshot() {
        
        val paramTypes = action.getParameterTypes();
        
        return streamParamNumbers()
        .mapToObj(paramIndex->{
            val actionArgumentModel = Optional.ofNullable(arguments.get(paramIndex));
            val adapter = actionArgumentModel
                    .map(ParameterUiModel::getValue)
                    .orElse(ManagedObject.empty(paramTypes.getElseFail(paramIndex)));
            return adapter;
        
        })
        .collect(Can.toCan());
    }
    
    public void resetTo(Can<ManagedObject> defaultsFixedPoint) {
        
        arguments.clear();
        
        streamParamUiModels()
        .forEach(actionArgumentModel -> {
            int paramIndex = actionArgumentModel.getNumber();
            val paramDefaultValue = defaultsFixedPoint.getElseFail(paramIndex);
            actionArgumentModel.setValue(paramDefaultValue);
        });
    }
    
    public Stream<ParameterUiModel> streamParamUiModels() {
        return streamParamNumbers()
                .mapToObj(paramIndex->
                    arguments.computeIfAbsent(paramIndex, this::createArgumentModel));
    }
    
    @Override
    public void setParameterValue(ObjectActionParameter actionParameter, ManagedObject newParamValue) {
        val actionParameterMemento = new ActionParameterMemento(actionParameter);
        val actionArgumentModel = computeIfAbsent(actionParameterMemento);
        actionArgumentModel.setValue(newParamValue);
    }
    
    @Override
    public void clearParameterValue(ObjectActionParameter actionParameter) {
        setParameterValue(actionParameter, null);
    }
    
    // //////////////////////////////////////
    
    private IntStream streamParamNumbers() {
        val paramCount = action.getParameterCount();
        return IntStream.range(0, paramCount);
    }

    private ParameterUiModel createArgumentModel(int paramIndex) {
        val param = action.getParameters().getElseFail(paramIndex);
        val paramMemento =  new ActionParameterMemento(param);
        val actionArgumentModel = new ScalarParameterModel(entityModel, paramMemento);
        return actionArgumentModel;
    }
    
    private void primeArgumentModels() {
        _Assert.assertEquals(
                action.getParameterCount(),
                (int)streamParamUiModels().count());
    }
    
    private ParameterUiModel computeIfAbsent(final ActionParameterMemento apm) {
        final int i = apm.getNumber();
        ParameterUiModel actionArgumentModel = arguments.get(i);
        if (actionArgumentModel == null) {
            actionArgumentModel = new ScalarParameterModel(entityModel, apm);
            final int number = actionArgumentModel.getNumber();
            arguments.put(number, actionArgumentModel);
        }
        return actionArgumentModel;
    }

    private void putArgumentValue(final int paramNum, final ManagedObject argumentAdapter) {
        val actionParam = action.getParameters().getElseFail(paramNum);
        setParameterValue(actionParam, argumentAdapter);
    }

    
}
