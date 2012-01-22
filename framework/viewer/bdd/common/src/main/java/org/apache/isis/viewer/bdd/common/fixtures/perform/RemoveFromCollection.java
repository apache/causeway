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
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionRemoveFromFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.ScenarioCell;

public class RemoveFromCollection extends PerformAbstractTypeParams {

    private ObjectAdapter result;

    public RemoveFromCollection(final Perform.Mode mode) {
        super("remove from collection", Type.COLLECTION, NumParameters.ONE, mode);
    }

    @Override
    public void doHandle(final PerformContext performContext) throws ScenarioBoundValueException {

        final ObjectAdapter onAdapter = performContext.getOnAdapter();
        final ObjectMember nakedObjectMember = performContext.getObjectMember();
        final CellBinding onMemberBinding = performContext.getPeer().getOnMemberBinding();
        @SuppressWarnings("unused")
        final ScenarioCell onMemberCell = onMemberBinding.getCurrentCell();

        final List<ScenarioCell> argumentCells = performContext.getArgumentCells();

        final OneToManyAssociation otma = (OneToManyAssociation) nakedObjectMember;

        // safe since guaranteed by superclass
        final CellBinding arg0Binding = performContext.getPeer().getArg0Binding();
        final ScenarioCell arg0Cell = argumentCells.get(0);
        final String toRemove = arg0Cell.getText();

        final CollectionRemoveFromFacet removeFromFacet = nakedObjectMember.getFacet(CollectionRemoveFromFacet.class);
        if (removeFromFacet == null) {
            throw ScenarioBoundValueException.current(onMemberBinding, "(cannot remove from collection)");
        }

        final ObjectAdapter toRemoveAdapter = performContext.getPeer().getAliasRegistry().getAliased(toRemove);
        if (toRemoveAdapter == null) {
            throw ScenarioBoundValueException.current(arg0Binding, "(unknown alias)");
        }

        // validate argument
        otma.createValidateAddInteractionContext(getSession(), InteractionInvocationMethod.BY_USER, onAdapter, toRemoveAdapter);
        final Consent validToRemove = otma.isValidToRemove(onAdapter, toRemoveAdapter);
        if (validToRemove.isVetoed()) {
            throw ScenarioBoundValueException.current(onMemberBinding, validToRemove.getReason());
        }

        // remove
        removeFromFacet.remove(onAdapter, toRemoveAdapter);

    }

    @Override
    public ObjectAdapter getResult() {
        return result;
    }

}
