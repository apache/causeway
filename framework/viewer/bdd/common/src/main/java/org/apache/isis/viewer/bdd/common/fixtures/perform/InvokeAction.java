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

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.ScenarioCell;

public class InvokeAction extends PerformAbstractTypeParams {

    private ObjectAdapter result;

    public InvokeAction(final Perform.Mode mode) {
        super("invoke action", Type.ACTION, NumParameters.UNLIMITED, mode);
    }

    @Override
    public void doHandle(final PerformContext performContext) throws ScenarioBoundValueException {

        final ObjectAdapter onAdapter = performContext.getOnAdapter();
        final ObjectMember objectMember = performContext.getObjectMember();
        final CellBinding onMemberBinding = performContext.getPeer().getOnMemberBinding();
        final List<ScenarioCell> argumentCells = performContext.getArgumentCells();

        final ObjectAction objectAction = (ObjectAction) objectMember;

        final int parameterCount = objectAction.getParameterCount();
        final boolean isContributedOneArgAction = objectAction.isContributed() && parameterCount == 1;

        ObjectAdapter[] proposedArguments;
        if (!isContributedOneArgAction) {

            // lookup arguments
            proposedArguments = performContext.getPeer().getAdapters(onAdapter, objectAction, onMemberBinding, argumentCells);

            // validate arguments
            final Consent argSetValid = objectAction.isProposedArgumentSetValid(onAdapter, proposedArguments);
            if (argSetValid.isVetoed()) {
                throw ScenarioBoundValueException.current(onMemberBinding, argSetValid.getReason());
            }
        } else {
            proposedArguments = new ObjectAdapter[] { onAdapter };
        }

        // execute
        result = objectAction.execute(onAdapter, proposedArguments);

        // all OK.
    }

    @Override
    public ObjectAdapter getResult() {
        return result;
    }

}
