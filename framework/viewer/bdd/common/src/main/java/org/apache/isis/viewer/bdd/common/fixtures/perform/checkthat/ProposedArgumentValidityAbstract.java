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
package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat;

import org.apache.isis.core.commons.lang.StringUtils;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.ScenarioCell;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;

public abstract class ProposedArgumentValidityAbstract extends ThatValidityAbstract {

    public ProposedArgumentValidityAbstract(final AssertsValidity assertion) {
        super(assertion);
    }

    @Override
    public ObjectAdapter that(final PerformContext performContext) throws ScenarioBoundValueException {

        final ObjectMember nakedObjectMember = performContext.getObjectMember();
        final CellBinding thatBinding = performContext.getPeer().getThatItBinding();
        final CellBinding arg0Binding = performContext.getPeer().getArg0Binding();

        // check we have an argument to validate (if one is required)
        if (!arg0Binding.isFound()) {
            throw ScenarioBoundValueException.current(thatBinding, "(requires argument)");
        }

        final ScenarioCell arg0Cell = arg0Binding.getCurrentCell();
        final String toValidate = arg0Cell.getText();
        if (StringUtils.isNullOrEmpty(toValidate)) {
            throw ScenarioBoundValueException.current(arg0Binding, "(required)");
        }

        final ObjectAdapter toValidateAdapter = performContext.getPeer().getAdapter(null, nakedObjectMember.getSpecification(), arg0Binding, arg0Cell);
        final Consent validityConsent = determineConsent(performContext, toValidateAdapter);
        if (!getAssertion().satisfiedBy(validityConsent)) {
            throw ScenarioBoundValueException.current(getAssertion().colorBinding(arg0Binding, thatBinding), getAssertion().getReason(validityConsent));
        }

        return toValidateAdapter;
    }

    protected abstract Consent determineConsent(final PerformContext performContext, ObjectAdapter toValidateAdapter);

}
