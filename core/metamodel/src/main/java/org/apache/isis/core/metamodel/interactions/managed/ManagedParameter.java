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
package org.apache.isis.core.metamodel.interactions.managed;

import java.util.Optional;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;

import lombok.NonNull;
import lombok.val;

public interface ManagedParameter extends ManagedValue, ManagedFeature {

    int getParamNr();
    @Override
    ObjectActionParameter getMetaModel();
    ParameterNegotiationModel getNegotiationModel();

    /**
     * @param params
     * @return non-empty if not usable/editable (meaning if read-only)
     */
    default Optional<InteractionVeto> checkUsability(final @NonNull Can<ManagedObject> params) {

        try {
            val head = getNegotiationModel().getHead();

            val usabilityConsent =
                    getMetaModel()
                    .isUsable(head, params, InteractionInitiatedBy.USER);

            return usabilityConsent.isVetoed()
                    ? Optional.of(InteractionVeto.readonly(usabilityConsent))
                    : Optional.empty();

        } catch (final Exception ex) {

            return Optional.of(InteractionVeto
                    .readonly(
                            new Veto(ex.getLocalizedMessage())));

        }

    }

}
