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
package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.action;

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.ScenarioCell;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.ThatSubcommandAbstract;

public class ArgumentSetValid extends ThatSubcommandAbstract {

    public ArgumentSetValid() {
        super("is valid for", "is valid", "valid");
    }

    // TODO: a lot of duplication with InvokeAction; simplify somehow?
    @Override
    public ObjectAdapter that(final PerformContext performContext) throws ScenarioBoundValueException {

        final ObjectAdapter onAdapter = performContext.getOnAdapter();
        final ObjectMember nakedObjectMember = performContext.getObjectMember();
        final CellBinding onMemberBinding = performContext.getPeer().getOnMemberBinding();
        final List<ScenarioCell> argumentCells = performContext.getArgumentCells();

        final ObjectAction nakedObjectAction = (ObjectAction) nakedObjectMember;
        final int parameterCount = nakedObjectAction.getParameterCount();
        final boolean isContributedOneArgAction = nakedObjectAction.isContributed() && parameterCount == 1;

        if (isContributedOneArgAction) {
            return null;
        }

        // lookup arguments
        final ObjectAdapter[] proposedArguments = performContext.getPeer().getAdapters(onAdapter, nakedObjectAction, onMemberBinding, argumentCells);

        // validate arguments
        final Consent argSetValid = nakedObjectAction.isProposedArgumentSetValid(onAdapter, proposedArguments);
        if (argSetValid.isVetoed()) {
            throw ScenarioBoundValueException.current(onMemberBinding, argSetValid.getReason());
        }

        // execute
        return null;
    }

}
