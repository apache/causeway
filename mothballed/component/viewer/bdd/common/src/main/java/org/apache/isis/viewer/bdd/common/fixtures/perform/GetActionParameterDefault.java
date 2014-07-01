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
package org.apache.isis.viewer.bdd.common.fixtures.perform;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.ScenarioCell;

public class GetActionParameterDefault extends PerformAbstractTypeParams {

    private ObjectAdapter result;

    public GetActionParameterDefault(final Perform.Mode mode) {
        super("get action parameter default", Type.ACTION, NumParameters.ONE, mode);
    }

    @Override
    public void doHandle(final PerformContext performContext) throws ScenarioBoundValueException {

        final ObjectAdapter onAdapter = performContext.getOnAdapter();
        final ObjectMember nakedObjectMember = performContext.getObjectMember();
        final CellBinding arg0Binding = performContext.getPeer().getArg0Binding();
        final ScenarioCell arg0Cell = arg0Binding.getCurrentCell();
        int requestedParamNum = -1;
        try {
            requestedParamNum = Integer.valueOf(arg0Cell.getText());
        } catch (final NumberFormatException ex) {
            throw ScenarioBoundValueException.current(arg0Binding, ex.getMessage());
        }

        final ObjectAction noa = (ObjectAction) nakedObjectMember;
        final int parameterCount = noa.getParameterCount();
        if (requestedParamNum < 0 || requestedParamNum > parameterCount - 1) {
            throw ScenarioBoundValueException.current(arg0Binding, "(must be between 0 and " + (parameterCount - 1) + ")");
        }

        final ObjectAdapter[] defaults = noa.getDefaults(onAdapter);
        result = defaults[requestedParamNum];
    }

    @Override
    public ObjectAdapter getResult() {
        return result;
    }

}
