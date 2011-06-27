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
package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.property;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.AssertsValidity;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.ThatValidityAbstract;

public class ProposedClear extends ThatValidityAbstract {

    public ProposedClear(final AssertsValidity assertion) {
        super(assertion);
    }

    @Override
    public ObjectAdapter that(final PerformContext performContext) throws ScenarioBoundValueException {

        final ObjectAdapter onAdapter = performContext.getOnAdapter();
        final OneToOneAssociation otoa = (OneToOneAssociation) performContext.getObjectMember();

        final Consent validityConsent = otoa.isAssociationValid(onAdapter, null);

        if (!getAssertion().satisfiedBy(validityConsent)) {
            final CellBinding thatBinding = performContext.getPeer().getThatItBinding();
            throw ScenarioBoundValueException.current(thatBinding, getAssertion().getReason(validityConsent));
        }

        // can only return null.
        return null;
    }

}
